package com.safezone.app.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.safezone.app.model.PoliceStation;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class StationMapFilterTest {
    private static PoliceStation fake(String name, String phone, double lat, double lng, String source) {
        return new PoliceStation(name, "n/a", phone, lat, lng, source, "test");
    }

    private static List<PoliceStation> stations() {
        List<PoliceStation> l = new ArrayList<>();
        l.add(fake("Test Near", "000", -33.94, 25.57, "test"));
        l.add(fake("Test Far", "000", -33.77, 25.40, "test"));
        l.add(fake("Test Callonly", "000", 0, 0, "test"));
        l.add(fake("Test Unsourced", "000", -33.94, 25.57, ""));
        return l;
    }

    private static final LatLng USER = new LatLng(-33.935, 25.570);

    @Test
    public void callOnlyAndUnverifiedStationsAreNeverPinned() {
        List<PoliceStation> out = StationMapFilter.pinnable(stations(), null, Double.POSITIVE_INFINITY);
        assertEquals(2, out.size());
        assertEquals("Test Near", out.get(0).name);
        assertEquals("Test Far", out.get(1).name);
    }

    @Test
    public void radiusLimitsThePinsAroundTheResident() {
        List<PoliceStation> out = StationMapFilter.pinnable(stations(), USER, 5);
        assertEquals(1, out.size());
        assertEquals("Test Near", out.get(0).name);
    }

    @Test
    public void wholeMetroKeepsEveryPinnableStation() {
        assertEquals(2, StationMapFilter.pinnable(stations(), USER, Double.POSITIVE_INFINITY).size());
    }

    @Test
    public void emptyInputGivesEmptyOutput() {
        assertTrue(StationMapFilter.pinnable(new ArrayList<PoliceStation>(), USER, 5).isEmpty());
    }
}
