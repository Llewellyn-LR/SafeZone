package com.safezone.app.model;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;

import androidx.annotation.Nullable;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

public final class CrimeHotspot {
    public final String id, title, area, timeAgo, description;
    public final double latitude, longitude;
    public final HotspotSeverity severity;
    public final boolean verified;

    @Nullable public final ReportCategory category;

    @Nullable public final Date createdAt;

    public static final long ALERT_LIFETIME_MS = 24L * 60 * 60 * 1000;

    public CrimeHotspot(String id, String title, String area, double latitude, double longitude,
                        HotspotSeverity severity, String timeAgo, String description,
                        boolean verified, @Nullable ReportCategory category) {
        this(id, title, area, latitude, longitude, severity, timeAgo, description, verified,
                category, null);
    }

    public CrimeHotspot(String id, String title, String area, double latitude, double longitude,
                        HotspotSeverity severity, String timeAgo, String description,
                        boolean verified, @Nullable ReportCategory category,
                        @Nullable Date createdAt) {
        this.createdAt = createdAt;
        this.id = id; this.title = title; this.area = area;
        this.latitude = latitude; this.longitude = longitude;
        this.severity = severity; this.timeAgo = timeAgo;
        this.description = description; this.verified = verified;
        this.category = category;
    }

    public CrimeHotspot(String id, String title, String area, double latitude, double longitude,
                        HotspotSeverity severity, String timeAgo, String description,
                        boolean verified) {
        this(id, title, area, latitude, longitude, severity, timeAgo, description, verified, null);
    }

    public CrimeHotspot(String id, String title, String area, double latitude, double longitude,
                        HotspotSeverity severity, String timeAgo, String description) {
        this(id, title, area, latitude, longitude, severity, timeAgo, description, true);
    }

    public static CrimeHotspot fromFirestore(DocumentSnapshot doc) {
        Map<String, Object> data = doc.getData();
        if (data == null) throw new IllegalStateException("Document has no data: " + doc.getId());
        Timestamp createdAt = doc.getTimestamp("createdAt");

        String rawCategory = str(data, "category", null);
        return new CrimeHotspot(
                doc.getId(),
                str(data, "title", "Untitled report"),
                str(data, "area", "Unknown area"),
                num(data, "latitude"),
                num(data, "longitude"),
                HotspotSeverity.fromWireValue(str(data, "severity", null)),
                createdAt != null ? timeAgo(createdAt.toDate()) : "Recently",
                str(data, "description", ""),
                Boolean.TRUE.equals(data.get("verified")),
                rawCategory == null ? null : ReportCategory.fromWireValue(rawCategory),
                createdAt != null ? createdAt.toDate() : null);
    }

    public boolean isExpired(long nowMillis) {
        return createdAt != null && nowMillis - createdAt.getTime() >= ALERT_LIFETIME_MS;
    }

    public static java.util.List<CrimeHotspot> withoutExpired(java.util.List<CrimeHotspot> all,
                                                              long nowMillis) {
        java.util.List<CrimeHotspot> out = new java.util.ArrayList<>();
        for (CrimeHotspot h : all) if (!h.isExpired(nowMillis)) out.add(h);
        return out;
    }

    public boolean isPubliclyVisible() {
        if (category != null) return category.isPublishable();
        return !ReportCategory.GBV.label.equals(title);
    }

    public static java.util.List<CrimeHotspot> publicOnly(java.util.List<CrimeHotspot> all) {
        java.util.List<CrimeHotspot> out = new java.util.ArrayList<>();
        for (CrimeHotspot h : all) if (h.isPubliclyVisible()) out.add(h);
        return out;
    }

    public Map<String, Object> toFirestoreMap() {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("title", title);
        m.put("area", area);
        m.put("latitude", latitude);
        m.put("longitude", longitude);
        m.put("severity", severity.wireValue);
        m.put("description", description);
        m.put("verified", verified);
        if (category != null) m.put("category", category.wireValue);
        m.put("createdAt", FieldValue.serverTimestamp());
        return m;
    }

    public static String timeAgo(Date dt) {
        return timeAgo(System.currentTimeMillis() - dt.getTime());
    }

    public static String timeAgo(long diffMillis) {
        long minutes = diffMillis / 60_000L;
        long hours = diffMillis / 3_600_000L;
        long days = diffMillis / 86_400_000L;
        if (minutes < 1) return "Just now";
        if (minutes < 60) return minutes + " min ago";
        if (hours < 24) return hours + " hr" + (hours == 1 ? "" : "s") + " ago";
        return days + " day" + (days == 1 ? "" : "s") + " ago";
    }

    private static String str(Map<String, Object> d, String k, String fallback) {
        Object v = d.get(k);
        return v instanceof String ? (String) v : fallback;
    }

    private static double num(Map<String, Object> d, String k) {
        Object v = d.get(k);
        return v instanceof Number ? ((Number) v).doubleValue() : 0;
    }
}
