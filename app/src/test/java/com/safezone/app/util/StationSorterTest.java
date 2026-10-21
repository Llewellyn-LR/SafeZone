package com.safezone.app.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.safezone.app.model.PoliceStation;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class StationSorterTest {
    private static PoliceStation fake(String name, double lat, double lng) {
        return new PoliceStation(name, "n/a", "000", lat, lng, "test", "test");
    }

    private static List<PoliceStation> stations() {
        List<PoliceStation> l = new ArrayList<>();
        l.add(fake("Test Station Far", -33.80, 25.40));
        l.add(fake("Test Station Near", -33.94, 25.57));
        l.add(fake("Test Station Mid", -33.90, 25.60));
        return l;
    }

    @Test
    public void sortsNearestFirstWhenLocationIsKnown() {
        List<StationSorter.Entry> out = StationSorter.sort(stations(), new LatLng(-33.935, 25.570));
        assertEquals("Test Station Near", out.get(0).station.name);
        assertEquals("Test Station Mid", out.get(1).station.name);
        assertEquals("Test Station Far", out.get(2).station.name);
        assertTrue(out.get(0).distanceKm < out.get(2).distanceKm);
    }

    @Test
    public void fallsBackToAlphabeticalWithoutALocation() {
        List<StationSorter.Entry> out = StationSorter.sort(stations(), null);
        assertEquals("Test Station Far", out.get(0).station.name);
        assertEquals("Test Station Mid", out.get(1).station.name);
        assertEquals("Test Station Near", out.get(2).station.name);
        assertEquals(-1, out.get(0).distanceKm, 0.0001);
    }

    @Test
    public void emptyListSortsToEmpty() {
        assertTrue(StationSorter.sort(new ArrayList<PoliceStation>(), null).isEmpty());
    }

    @Test
    public void aStationNeedsAPhoneSourceDateAndCoordinatesToBeShown() {
        assertTrue(new PoliceStation("A", "", "041 000 0000", -33.9, 25.6, "https://x", "2026-01-01").isVerified());
        assertFalse(new PoliceStation("A", "", "", -33.9, 25.6, "https://x", "2026-01-01").isVerified());
        assertFalse(new PoliceStation("A", "", "1", -33.9, 25.6, "", "2026-01-01").isVerified());
        assertFalse(new PoliceStation("A", "", "1", -33.9, 25.6, "https://x", "").isVerified());

        PoliceStation callOnly = new PoliceStation("A", "", "1", 0, 0, "https://x", "2026-01-01");
        assertTrue(callOnly.isVerified());
        assertFalse(callOnly.hasCoordinates());

        assertFalse(new PoliceStation("A", "", "", 0, 0, "https://x", "2026-01-01").isVerified());

        PoliceStation mapOnly = new PoliceStation("A", "", "", -33.9, 25.6, "https://x", "2026-01-01");
        assertTrue(mapOnly.isVerified());
        assertFalse(mapOnly.hasPhone());

        assertFalse(new PoliceStation("A", "", "1", 95, 25.6, "https://x", "2026-01-01").isVerified());
    }

    @Test
    public void stationsWithoutCoordinatesComeLastAndHaveNoDistance() {
        List<PoliceStation> l = stations();
        l.add(new PoliceStation("Test Station Callonly", "n/a", "000", 0, 0, "test", "test"));
        List<StationSorter.Entry> out = StationSorter.sort(l, new LatLng(-33.935, 25.570));
        StationSorter.Entry last = out.get(out.size() - 1);
        assertEquals("Test Station Callonly", last.station.name);
        assertEquals(-1, last.distanceKm, 0.0001);
        assertEquals("Test Station Near", out.get(0).station.name);
    }
}
