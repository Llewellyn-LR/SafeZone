package com.safezone.app.util;

import com.safezone.app.model.CrimeHotspot;

import java.util.ArrayList;
import java.util.List;

public final class RadiusFilter {
    private RadiusFilter() {}

    public static final int ALL_STEP = 11;
    public static final int DEFAULT_STEP = 5;

    public static double kmForStep(int step) {
        return step >= ALL_STEP ? Double.POSITIVE_INFINITY : Math.max(1, step);
    }

    public static boolean isAll(double radiusKm) {
        return Double.isInfinite(radiusKm);
    }

    public static String label(double radiusKm) {
        return isAll(radiusKm) ? "Whole Nelson Mandela Bay"
                : "Alert radius: " + Math.round(radiusKm) + " km";
    }

    public static boolean within(LatLng user, LatLng point, double radiusKm) {
        return isAll(radiusKm) || GeoUtils.distanceKm(user, point) <= radiusKm;
    }

    public static List<CrimeHotspot> filter(List<CrimeHotspot> all, LatLng user, double radiusKm) {
        if (user == null || isAll(radiusKm)) return new ArrayList<>(all);
        List<CrimeHotspot> out = new ArrayList<>();
        for (CrimeHotspot h : all) {
            if (within(user, new LatLng(h.latitude, h.longitude), radiusKm)) out.add(h);
        }
        return out;
    }
}
