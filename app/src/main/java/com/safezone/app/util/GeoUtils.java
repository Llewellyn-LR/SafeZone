package com.safezone.app.util;

public final class GeoUtils {
    private GeoUtils() {}

    public static double distanceKm(LatLng a, LatLng b) {
        final double earthRadiusKm = 6371.0;
        double dLat = deg2rad(b.latitude - a.latitude);
        double dLon = deg2rad(b.longitude - a.longitude);
        double h = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(deg2rad(a.latitude)) * Math.cos(deg2rad(b.latitude))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(h), Math.sqrt(1 - h));
        return earthRadiusKm * c;
    }

    private static double deg2rad(double deg) {
        return deg * (Math.PI / 180);
    }
}
