package com.safezone.app.service;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;

public class ModeratorService {
    public Task<Boolean> isModerator(String uid) {
        return FirebaseFirestore.getInstance().collection("moderators").document(uid).get()
                .continueWith(t -> t.getResult().exists());
    }
}
