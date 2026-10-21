package com.safezone.app.service;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Locale;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class StorageService {
    private static final int MAX_PHOTO_BYTES = 5 * 1024 * 1024;
    private static final Executor IO = Executors.newCachedThreadPool();

    private final ContentResolver resolver;
    private FirebaseStorage storage;

    public StorageService(Context context) {

        this.resolver = context.getApplicationContext().getContentResolver();
    }

    private FirebaseStorage storage() {
        if (storage == null) storage = FirebaseStorage.getInstance();
        return storage;
    }

    public Task<String> uploadReportPhoto(Uri photo, String uid) {
        return upload(photo, "report_photos/" + uid);
    }

    public Task<String> uploadDismissalProof(Uri photo, String uid) {
        return upload(photo, "dismissal_proofs/" + uid);
    }

    public Task<String> uploadWantedPersonPhoto(Uri photo, String uid) {
        return upload(photo, "wanted_photos/" + uid);
    }

    private static final class Payload {
        byte[] bytes;
        String name;
    }

    private Task<String> upload(Uri photo, String pathPrefix) {
        try {
            storage();
        } catch (RuntimeException e) {
            return Tasks.forException(DataError.storageNotConfigured());
        }
        return Tasks.call(IO, () -> {
            Payload p = new Payload();
            p.bytes = readAll(photo);
            p.name = displayName(photo);
            if (p.bytes.length > MAX_PHOTO_BYTES) {
                throw DataError.tooLarge();
            }
            return p;
        }).onSuccessTask(p -> {
            String ext = extensionFor(p.name, p.bytes);
            String path = pathPrefix + "/" + System.currentTimeMillis() + "." + ext;
            StorageReference ref = storage().getReference().child(path);
            StorageMetadata meta = new StorageMetadata.Builder()
                    .setContentType(contentTypeFor(ext)).build();
            return ref.putBytes(p.bytes, meta)
                    .onSuccessTask(snap -> ref.getDownloadUrl())
                    .continueWith(t -> t.getResult().toString());
        });
    }

    private byte[] readAll(Uri uri) throws Exception {
        try (InputStream in = resolver.openInputStream(uri)) {
            if (in == null) throw new IllegalStateException("Couldn't read the selected photo.");
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            byte[] buf = new byte[8192];
            int n;
            while ((n = in.read(buf)) > 0) out.write(buf, 0, n);
            return out.toByteArray();
        }
    }

    private String displayName(Uri uri) {
        if ("file".equals(uri.getScheme()) && uri.getLastPathSegment() != null) {
            return uri.getLastPathSegment();
        }
        try (Cursor c = resolver.query(uri, new String[]{OpenableColumns.DISPLAY_NAME}, null, null, null)) {
            if (c != null && c.moveToFirst()) {
                String n = c.getString(0);
                if (n != null) return n;
            }
        } catch (Exception ignored) { }
        return "";
    }

    static String extensionFor(String fileName, byte[] bytes) {
        String fromName = fileName.contains(".")
                ? fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase(Locale.ROOT) : "";
        if (!fromName.isEmpty()) return fromName;

        if (bytes.length > 2 && (bytes[0] & 0xFF) == 0xFF && (bytes[1] & 0xFF) == 0xD8) return "jpg";
        if (bytes.length > 3 && (bytes[0] & 0xFF) == 0x89 && bytes[1] == 0x50
                && bytes[2] == 0x4E && bytes[3] == 0x47) return "png";
        return "jpg";
    }

    static String contentTypeFor(String ext) {
        switch (ext) {
            case "png": return "image/png";
            case "heic": return "image/heic";
            case "webp": return "image/webp";
            default: return "image/jpeg";
        }
    }
}
