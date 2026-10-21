package com.safezone.app.model;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

public final class CrimeReport {
    public final String id;
    public final ReportCategory category;
    public final String area;
    public final String description;
    public final boolean hasPhoto;
    public final Date submittedAt;
    public final ReportStatus status;

    public final String reporterUid;

    public final Double latitude;
    public final Double longitude;

    public final String photoUrl;

    public final String rejectionReason;

    public final String proofUrl;

    public CrimeReport(String id, ReportCategory category, String area, String description,
                       boolean hasPhoto, Date submittedAt, ReportStatus status,
                       String reporterUid, Double latitude, Double longitude, String photoUrl,
                       String rejectionReason, String proofUrl) {
        this.id = id; this.category = category; this.area = area;
        this.description = description; this.hasPhoto = hasPhoto;
        this.submittedAt = submittedAt; this.status = status;
        this.reporterUid = reporterUid; this.latitude = latitude; this.longitude = longitude;
        this.photoUrl = photoUrl;
        this.rejectionReason = rejectionReason; this.proofUrl = proofUrl;
    }

    public CrimeReport(String id, ReportCategory category, String area, String description,
                       boolean hasPhoto, Date submittedAt, ReportStatus status,
                       String reporterUid, Double latitude, Double longitude, String photoUrl) {
        this(id, category, area, description, hasPhoto, submittedAt, status,
                reporterUid, latitude, longitude, photoUrl, null, null);
    }

    public boolean isGbv() { return category == ReportCategory.GBV; }

    public boolean canMarkSolved() {
        return isGbv() && (status == ReportStatus.PENDING_REVIEW || status == ReportStatus.FOLLOW_UP);
    }

    public boolean canDismiss() { return isGbv() && status == ReportStatus.PENDING_REVIEW; }

    public boolean awaitingReporterConfirmation() {
        return isGbv() && status == ReportStatus.AWAITING_CONFIRMATION;
    }

    public String displayStatusLabel() {
        return isGbv() && status == ReportStatus.REJECTED ? "Rejected" : status.label;
    }

    public CrimeReport(String id, ReportCategory category, String area, String description,
                       boolean hasPhoto, Date submittedAt, ReportStatus status,
                       String reporterUid, Double latitude, Double longitude) {
        this(id, category, area, description, hasPhoto, submittedAt, status,
                reporterUid, latitude, longitude, null);
    }

    public CrimeReport(String id, ReportCategory category, String area, String description,
                       boolean hasPhoto, Date submittedAt) {
        this(id, category, area, description, hasPhoto, submittedAt,
                ReportStatus.PENDING_REVIEW, null, null, null);
    }

    public static CrimeReport fromFirestore(DocumentSnapshot doc) {
        Map<String, Object> data = doc.getData();
        if (data == null) throw new IllegalStateException("Document has no data: " + doc.getId());
        Timestamp ts = doc.getTimestamp("submittedAt");
        return new CrimeReport(
                doc.getId(),
                ReportCategory.fromWireValue(str(data, "category", null)),
                str(data, "area", "Unknown area"),
                str(data, "description", ""),
                Boolean.TRUE.equals(data.get("hasPhoto")),
                ts != null ? ts.toDate() : new Date(),
                ReportStatus.fromWireValue(str(data, "status", null)),
                str(data, "reporterUid", null),
                dbl(data, "latitude"),
                dbl(data, "longitude"),
                str(data, "photoUrl", null),
                str(data, "rejectionReason", null),
                str(data, "proofUrl", null));
    }

    public Map<String, Object> toFirestoreMap() {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("category", category.wireValue);
        m.put("area", area);
        m.put("description", description);
        m.put("hasPhoto", hasPhoto);
        m.put("status", status.wireValue);
        m.put("reporterUid", reporterUid);
        m.put("latitude", latitude);
        m.put("longitude", longitude);
        m.put("photoUrl", photoUrl);
        if (rejectionReason != null) m.put("rejectionReason", rejectionReason);
        if (proofUrl != null) m.put("proofUrl", proofUrl);
        m.put("submittedAt", FieldValue.serverTimestamp());
        return m;
    }

    public CrimeReport copyWith(ReportStatus newStatus) {
        return new CrimeReport(id, category, area, description, hasPhoto, submittedAt,
                newStatus != null ? newStatus : status, reporterUid, latitude, longitude, photoUrl,
                rejectionReason, proofUrl);
    }

    private static String str(Map<String, Object> d, String k, String fallback) {
        Object v = d.get(k);
        return v instanceof String ? (String) v : fallback;
    }

    private static Double dbl(Map<String, Object> d, String k) {
        Object v = d.get(k);
        return v instanceof Number ? ((Number) v).doubleValue() : null;
    }
}
