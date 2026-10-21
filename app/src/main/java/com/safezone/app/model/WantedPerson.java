package com.safezone.app.model;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

public final class WantedPerson {
    public final String id;

    public final String name;
    public final String alias;
    public final String wantedFor;
    public final String caseReference;
    public final String lastSeenArea;
    public final boolean armedAndDangerous;
    public final String photoUrl;
    public final Date publishedAt;

    private WantedPerson(Builder b) {
        this.id = b.id;
        this.name = b.name;
        this.alias = b.alias;
        this.wantedFor = b.wantedFor;
        this.caseReference = b.caseReference;
        this.lastSeenArea = b.lastSeenArea;
        this.armedAndDangerous = b.armedAndDangerous;
        this.photoUrl = b.photoUrl;
        this.publishedAt = b.publishedAt;
    }

    public String displayName() {
        return alias != null ? name + " (" + alias + ")" : name;
    }

    public static WantedPerson fromFirestore(DocumentSnapshot doc) {
        Map<String, Object> data = doc.getData();
        if (data == null) throw new IllegalStateException("Document has no data: " + doc.getId());
        Timestamp ts = doc.getTimestamp("publishedAt");
        return new Builder()
                .id(doc.getId())
                .name(str(data, "name", "Unknown"))
                .alias(str(data, "alias", null))
                .wantedFor(str(data, "wantedFor", ""))
                .caseReference(str(data, "caseReference", null))
                .lastSeenArea(str(data, "lastSeenArea", "Nelson Mandela Bay"))
                .armedAndDangerous(Boolean.TRUE.equals(data.get("armedAndDangerous")))
                .photoUrl(str(data, "photoUrl", null))
                .publishedAt(ts != null ? ts.toDate() : new Date())
                .build();
    }

    private static String str(Map<String, Object> data, String key, String fallback) {
        Object v = data.get(key);
        return v instanceof String ? (String) v : fallback;
    }

    public Map<String, Object> toFirestoreMap() {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("name", name);
        m.put("alias", alias);
        m.put("wantedFor", wantedFor);
        m.put("caseReference", caseReference);
        m.put("lastSeenArea", lastSeenArea);
        m.put("armedAndDangerous", armedAndDangerous);
        m.put("photoUrl", photoUrl);

        m.put("publishedAt", FieldValue.serverTimestamp());
        return m;
    }

    public static final class Builder {
        private String id, name, alias, wantedFor, caseReference, lastSeenArea, photoUrl;
        private boolean armedAndDangerous = false;
        private Date publishedAt;

        public Builder id(String v) { id = v; return this; }
        public Builder name(String v) { name = v; return this; }
        public Builder alias(String v) { alias = v; return this; }
        public Builder wantedFor(String v) { wantedFor = v; return this; }
        public Builder caseReference(String v) { caseReference = v; return this; }
        public Builder lastSeenArea(String v) { lastSeenArea = v; return this; }
        public Builder armedAndDangerous(boolean v) { armedAndDangerous = v; return this; }
        public Builder photoUrl(String v) { photoUrl = v; return this; }
        public Builder publishedAt(Date v) { publishedAt = v; return this; }

        public WantedPerson build() {
            if (id == null || name == null || wantedFor == null
                    || lastSeenArea == null || publishedAt == null) {
                throw new IllegalStateException(
                        "id, name, wantedFor, lastSeenArea and publishedAt are required");
            }
            return new WantedPerson(this);
        }
    }
}
