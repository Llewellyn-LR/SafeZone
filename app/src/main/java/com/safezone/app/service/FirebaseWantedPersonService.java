package com.safezone.app.service;

import android.content.Context;
import android.net.Uri;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.safezone.app.model.WantedPerson;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class FirebaseWantedPersonService implements WantedPersonService {
    private final FirebaseFirestore db;
    private final StorageService storage;

    public FirebaseWantedPersonService(Context context) {
        this.db = FirebaseFirestore.getInstance();
        this.storage = new StorageService(context);
    }

    @Override
    public Subscription watchWantedPersons(DataListener<List<WantedPerson>> listener) {
        ListenerRegistration reg = db.collection("wanted_persons")
                .orderBy("publishedAt", Query.Direction.DESCENDING)
                .limit(100)
                .addSnapshotListener((snap, error) -> {
                    if (error != null) { listener.onError(DataError.from(error)); return; }
                    if (snap == null) return;
                    List<WantedPerson> out = new ArrayList<>();
                    for (com.google.firebase.firestore.DocumentSnapshot d : snap.getDocuments()) {
                        out.add(WantedPerson.fromFirestore(d));
                    }
                    listener.onData(out);
                });
        return reg::remove;
    }

    public Task<Void> publishWantedPerson(String name, String alias, String wantedFor,
                                          String caseReference, String lastSeenArea,
                                          boolean armedAndDangerous, Uri photo,
                                          String moderatorUid) {
        Task<String> urlTask = photo != null
                ? storage.uploadWantedPersonPhoto(photo, moderatorUid)
                : Tasks.<String>forResult(null);

        return TaskErrors.mapFailures(urlTask.onSuccessTask(photoUrl -> {
            WantedPerson person = new WantedPerson.Builder()
                    .id("").name(name).alias(alias).wantedFor(wantedFor)
                    .caseReference(caseReference).lastSeenArea(lastSeenArea)
                    .armedAndDangerous(armedAndDangerous).photoUrl(photoUrl)
                    .publishedAt(new Date()).build();
            return db.collection("wanted_persons").add(person.toFirestoreMap());
        }).continueWith(t -> {
            if (!t.isSuccessful()) throw t.getException();
            return null;
        }));
    }

    public Task<Void> deleteWantedPerson(String id) {
        return TaskErrors.mapFailures(db.collection("wanted_persons").document(id).delete());
    }
}
