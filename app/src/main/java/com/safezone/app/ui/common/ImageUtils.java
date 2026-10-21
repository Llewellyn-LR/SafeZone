package com.safezone.app.ui.common;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;

import androidx.exifinterface.media.ExifInterface;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public final class ImageUtils {
    private ImageUtils() {}

    public static File prepareForUpload(Context ctx, Uri src, int maxWidth, int quality)
            throws IOException {
        BitmapFactory.Options bounds = new BitmapFactory.Options();
        bounds.inJustDecodeBounds = true;
        try (InputStream in = ctx.getContentResolver().openInputStream(src)) {
            BitmapFactory.decodeStream(in, null, bounds);
        }
        if (bounds.outWidth <= 0) throw new IOException("Unreadable image");

        int sample = 1;
        while (bounds.outWidth / (sample * 2) >= maxWidth) sample *= 2;
        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inSampleSize = sample;
        Bitmap bmp;
        try (InputStream in = ctx.getContentResolver().openInputStream(src)) {
            bmp = BitmapFactory.decodeStream(in, null, opts);
        }
        if (bmp == null) throw new IOException("Unreadable image");

        int rotation = 0;
        try (InputStream in = ctx.getContentResolver().openInputStream(src)) {
            int o = new ExifInterface(in).getAttributeInt(
                    ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            if (o == ExifInterface.ORIENTATION_ROTATE_90) rotation = 90;
            else if (o == ExifInterface.ORIENTATION_ROTATE_180) rotation = 180;
            else if (o == ExifInterface.ORIENTATION_ROTATE_270) rotation = 270;
        } catch (Exception ignored) { }

        Matrix m = new Matrix();
        if (rotation != 0) m.postRotate(rotation);
        int rotatedW = (rotation == 90 || rotation == 270) ? bmp.getHeight() : bmp.getWidth();
        if (rotatedW > maxWidth) {
            float s = maxWidth / (float) rotatedW;
            m.postScale(s, s);
        }
        Bitmap out = Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(), bmp.getHeight(), m, true);

        File dir = new File(ctx.getCacheDir(), "images");

        dir.mkdirs();
        File file = File.createTempFile("wanted_", ".jpg", dir);
        try (FileOutputStream fos = new FileOutputStream(file)) {
            out.compress(Bitmap.CompressFormat.JPEG, quality, fos);
        }
        return file;
    }
}
