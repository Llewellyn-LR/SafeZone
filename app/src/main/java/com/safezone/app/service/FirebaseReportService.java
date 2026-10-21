package com.safezone.app.service;

import android.content.Context;
import android.net.Uri;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.WriteBatch;
import com.safezone.app.model.CrimeHotspot;
import com.safezone.app.model.CrimeReport;
import com.safezone.app.model.HotspotSeverity;
import com.safezone.app.model.ReportCategory;
import com.safezone.app.model.ReportStatus;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class FirebaseReportService extends ReportService {
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final StorageService storage;
    private final FirebaseAuth.AuthStateListener authListener;
    private ListenerRegistration reportsReg;
    private List<CrimeReport> myReports = new ArrayList<>();

    public FirebaseReportService(Context context) {
        storage = new StorageService(context);
        authListener = auth -> {
            FirebaseUser u = auth.getCurrentUser();
            subscribeToMyReports(u != null ? u.getUid() : null);
        };
        FirebaseAuth.getInstance().addAuthStateListener(authListener);
    }

    @Override
    public List<CrimeReport> getMyReports() { return Collections.unmodifiableList(myReports); }

    private void subscribeToMyReports(String uid) {
        if (reportsReg != null) reportsReg.remove();
        reportsReg = null;
        if (uid == null) {
            myReports = new ArrayList<>();
            notifyListeners();
            return;
        }
        reportsReg = db.collection("reports")
                .whereEqualTo("reporterUid", uid)

                .addSnapshotListener((snap, error) -> {
                    if (error != null) {

                        android.util.Log.e("FirebaseReportService", "my reports query failed", error);
                        return;
                    }
                    if (snap == null) return;
                    List<CrimeReport> out = new ArrayList<>();
                    for (DocumentSnapshot d : snap.getDocuments()) out.add(CrimeReport.fromFirestore(d));
                    Collections.sort(out, (x, y) -> y.submittedAt.compareTo(x.submittedAt));
                    myReports = out;
                    notifyListeners();
                });
    }

    @Override
    public void submitReport(ReportCategory category, String area, String description,
                             Uri photo, Double latitude, Double longitude,
                             SubmitCallback cb) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String uid = user != null ? user.getUid() : null;
        if (photo != null && uid == null) {
            cb.onError(DataError.unauthenticated());
            return;
        }

        Task<String> upload = photo != null
                ? storage.uploadReportPhoto(photo, uid) : Tasks.<String>forResult(null);
        upload.onSuccessTask(photoUrl -> {
            CrimeReport report = new CrimeReport("", category, area, description, photoUrl != null,
                    new Date(), ReportStatus.PENDING_REVIEW, uid, latitude, longitude, photoUrl);
            return db.collection("reports").add(report.toFirestoreMap());
        }).addOnSuccessListener((DocumentReference ref) -> cb.onSuccess(new CrimeReport(
                        ref.getId(), category, area, description, photo != null, new Date(),
                        ReportStatus.PENDING_REVIEW, uid, latitude, longitude, null)))
                .addOnFailureListener(e -> cb.onError(DataError.from(e)));
    }

    public Subscription watchPendingReports(DataListener<List<CrimeReport>> listener) {
        ListenerRegistration reg = db.collection("reports")
                .whereIn("status", Arrays.asList(
                        ReportStatus.PENDING_REVIEW.wireValue, ReportStatus.FOLLOW_UP.wireValue))

                .addSnapshotListener((snap, error) -> {
                    if (error != null) {
                        android.util.Log.e("FirebaseReportService", "watchPendingReports failed", error);
                        listener.onError(DataError.from(error));
                        return;
                    }
                    if (snap == null) return;
                    List<CrimeReport> out = new ArrayList<>();
                    for (DocumentSnapshot d : snap.getDocuments()) out.add(CrimeReport.fromFirestore(d));
                    Collections.sort(out, (x, y) -> x.submittedAt.compareTo(y.submittedAt));
                    listener.onData(out);
                });
        return reg::remove;
    }

    public Task<Void> approveReport(CrimeReport report, HotspotSeverity severity,
                                    Double latitude, Double longitude) {

        if (!report.category.isPublishable()) {
            return Tasks.forException(new IllegalArgumentException(
                    "GBV reports are kept private and can't be published to the map."));
        }

        Double lat = latitude != null ? latitude : report.latitude;
        Double lng = longitude != null ? longitude : report.longitude;
        if (lat == null || lng == null) {
            return Tasks.forException(new IllegalArgumentException(
                    "This report has no location on file — enter coordinates manually to approve it."));
        }
        WriteBatch batch = db.batch();
        DocumentReference hotspotRef = db.collection("hotspots").document();
        batch.set(hotspotRef, new CrimeHotspot(hotspotRef.getId(), report.category.label,
                report.area, lat, lng, severity, "Just now", report.description, true, report.category)
                .toFirestoreMap());
        batch.update(db.collection("reports").document(report.id),
                "status", ReportStatus.PUBLISHED.wireValue);
        return TaskErrors.mapFailures(batch.commit());
    }

    public Task<Void> rejectReport(CrimeReport report) {
        if (report.isGbv()) {
            return Tasks.forException(new IllegalArgumentException(
                    "GBV reports are dismissed with a reason, or marked solved."));
        }
        return TaskErrors.mapFailures(db.collection("reports").document(report.id)
                .update("status", ReportStatus.REJECTED.wireValue));
    }

    public Task<Void> markSolved(CrimeReport report) {
        if (!report.canMarkSolved()) {
            return Tasks.forException(new IllegalStateException(
                    "This report can't be marked solved right now."));
        }
        Map<String, Object> m = new HashMap<>();
        m.put("status", ReportStatus.AWAITING_CONFIRMATION.wireValue);
        m.put("solvedAt", FieldValue.serverTimestamp());
        return TaskErrors.mapFailures(db.collection("reports").document(report.id).update(m));
    }

    public Task<Void> dismissReport(CrimeReport report, String reason, Uri proof, String moderatorUid) {
        if (!report.canDismiss()) {
            return Tasks.forException(new IllegalStateException(
                    "Only a new GBV report can be dismissed."));
        }
        final String why = reason == null ? "" : reason.trim();
        if (why.isEmpty()) {
            return Tasks.forException(new IllegalArgumentException(
                    "A reason is required to dismiss a report."));
        }
        if (proof != null && moderatorUid == null) return Tasks.forException(DataError.unauthenticated());
        Task<String> upload = proof != null
                ? storage.uploadDismissalProof(proof, moderatorUid) : Tasks.<String>forResult(null);
        return TaskErrors.mapFailures(upload.onSuccessTask(proofUrl -> {
            Map<String, Object> m = new HashMap<>();
            m.put("status", ReportStatus.REJECTED.wireValue);
            m.put("rejectionReason", why);
            if (proofUrl != null) m.put("proofUrl", proofUrl);
            m.put("dismissedAt", FieldValue.serverTimestamp());
            return db.collection("reports").document(report.id).update(m);
        }));
    }

    @Override
    public void respondToSolved(CrimeReport report, boolean solved, ActionCallback cb) {
        if (!report.awaitingReporterConfirmation()) {
            cb.onError(new IllegalStateException("This report isn't waiting for your confirmation."));
            return;
        }
        Map<String, Object> m = new HashMap<>();
        m.put("status", (solved ? ReportStatus.SOLVED : ReportStatus.FOLLOW_UP).wireValue);
        m.put(solved ? "confirmedAt" : "reopenedAt", FieldValue.serverTimestamp());
        db.collection("reports").document(report.id).update(m)
                .addOnSuccessListener(x -> cb.onSuccess())
                .addOnFailureListener(e -> cb.onError(DataError.from(e)));
    }

    @Override
    public void dispose() {
        FirebaseAuth.getInstance().removeAuthStateListener(authListener);
        if (reportsReg != null) reportsReg.remove();
    }
}
