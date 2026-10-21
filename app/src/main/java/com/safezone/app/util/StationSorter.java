package com.safezone.app.util;

import com.safezone.app.model.PoliceStation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public final class StationSorter {
    private StationSorter() {}

    public static final class Entry {
        public final PoliceStation station;

        public final double distanceKm;
        Entry(PoliceStation station, double distanceKm) {
            this.station = station;
            this.distanceKm = distanceKm;
        }
    }

    public static List<Entry> sort(List<PoliceStation> stations, LatLng user) {
        List<Entry> out = new ArrayList<>();
        for (PoliceStation s : stations) {
            double d = user == null || !s.hasCoordinates() ? -1
                    : GeoUtils.distanceKm(user, new LatLng(s.latitude, s.longitude));
            out.add(new Entry(s, d));
        }
        Comparator<Entry> byName = Comparator.comparing((Entry e) -> e.station.name.toLowerCase());
        Collections.sort(out, user == null
                ? byName
                : Comparator.comparing((Entry e) -> e.distanceKm < 0)
                .thenComparingDouble(e -> e.distanceKm)
                .thenComparing(byName));
        return out;
    }
}
