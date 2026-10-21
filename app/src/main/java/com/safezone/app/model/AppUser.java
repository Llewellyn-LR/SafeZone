package com.safezone.app.model;

public final class AppUser {
    public final String uid;
    public final String name;
    public final String email;
    public final boolean isGuest;

    public AppUser(String uid, String name, String email, boolean isGuest) {
        this.uid = uid;
        this.name = name;
        this.email = email;
        this.isGuest = isGuest;
    }

    public static final AppUser GUEST = new AppUser(null, "Guest", "", true);

    public static AppUser guestWithUid(String uid) {
        return new AppUser(uid, "Guest", "", true);
    }

    public static AppUser fromFirebase(String uid, String name, String email) {
        return new AppUser(uid, name, email, false);
    }
}
