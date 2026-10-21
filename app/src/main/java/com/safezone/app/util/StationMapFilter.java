package com.safezone.app.util;

import com.safezone.app.model.PoliceStation;

import java.util.ArrayList;
import java.util.List;

public final class StationMapFilter {
    private StationMapFilter() {}

    public static List<PoliceStation> pinnable(List<PoliceStation> all, LatLng user, double radiusKm) {
        List<PoliceStation> out = new ArrayList<>();
        for (PoliceStation s : all) {
            if (!s.isVerified() || !s.hasCoordinates()) continue;
            if (user != null && !RadiusFilter.within(user, new LatLng(s.latitude, s.longitude), radiusKm)) continue;
            out.add(s);
        }
        return out;
    }
}
