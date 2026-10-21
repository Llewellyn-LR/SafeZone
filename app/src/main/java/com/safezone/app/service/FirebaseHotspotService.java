package com.safezone.app.service;

import android.os.Handler;
import android.os.Looper;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.safezone.app.model.CrimeHotspot;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class FirebaseHotspotService implements HotspotService {
    private static final long RECHECK_MS = 60_000L;

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    public Subscription watchHotspots(DataListener<List<CrimeHotspot>> listener) {
        final Handler main = new Handler(Looper.getMainLooper());
        final List<CrimeHotspot> latest = new ArrayList<>();
        final boolean[] active = {true};

        final Runnable[] recheck = new Runnable[1];
        recheck[0] = () -> {
            if (!active[0]) return;
            listener.onData(CrimeHotspot.withoutExpired(latest, System.currentTimeMillis()));
            main.postDelayed(recheck[0], RECHECK_MS);
        };

        Date cutoff = new Date(System.currentTimeMillis() - CrimeHotspot.ALERT_LIFETIME_MS);
        ListenerRegistration reg = db.collection("hotspots")
                .whereGreaterThan("createdAt", new Timestamp(cutoff))
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .limit(100)
                .addSnapshotListener((snap, error) -> {
                    if (error != null) { listener.onError(DataError.from(error)); return; }
                    if (snap == null) return;
                    latest.clear();
                    for (DocumentSnapshot d : snap.getDocuments()) {
                        latest.add(CrimeHotspot.fromFirestore(d));
                    }
                    List<CrimeHotspot> visible = CrimeHotspot.publicOnly(latest);
                    latest.clear();
                    latest.addAll(visible);
                    main.removeCallbacks(recheck[0]);
                    recheck[0].run();
                });
        return () -> {
            active[0] = false;
            main.removeCallbacks(recheck[0]);
            reg.remove();
        };
    }
}
