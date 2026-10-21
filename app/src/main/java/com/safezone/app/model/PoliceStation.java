package com.safezone.app.model;

public final class PoliceStation {
    public final String name, address, phone, sourceUrl, verifiedOn;
    public final double latitude, longitude;

    public PoliceStation(String name, String address, String phone,
                         double latitude, double longitude, String sourceUrl, String verifiedOn) {
        this.name = name; this.address = address; this.phone = phone;
        this.latitude = latitude; this.longitude = longitude;
        this.sourceUrl = sourceUrl; this.verifiedOn = verifiedOn;
    }

    public boolean hasPhone() { return notBlank(phone); }

    public boolean hasCoordinates() {
        return coordinatesInRange() && !(latitude == 0 && longitude == 0);
    }

    public boolean isVerified() {
        return notBlank(name) && notBlank(sourceUrl) && notBlank(verifiedOn)
                && coordinatesInRange()
                && (hasPhone() || hasCoordinates());
    }

    private boolean coordinatesInRange() {
        return latitude >= -90 && latitude <= 90 && longitude >= -180 && longitude <= 180;
    }

    private static boolean notBlank(String s) { return s != null && !s.trim().isEmpty(); }
}
