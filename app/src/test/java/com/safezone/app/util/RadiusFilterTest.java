package com.safezone.app.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.safezone.app.model.CrimeHotspot;
import com.safezone.app.model.HotspotSeverity;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class RadiusFilterTest {
    private static final LatLng USER = new LatLng(-33.9350, 25.5700);

    private static CrimeHotspot at(String id, double lat, double lng) {
        return new CrimeHotspot(id, "t", "a", lat, lng, HotspotSeverity.LOW, "now", "");
    }

    private static List<CrimeHotspot> sample() {
        List<CrimeHotspot> l = new ArrayList<>();
        l.add(at("near", -33.9340, 25.5710));
        l.add(at("mid", -33.9608, 25.6022));
        l.add(at("far", -33.8010, 25.4180));
        return l;
    }

    @Test
    public void stepsOneToTenAreKilometres() {
        assertEquals(1, RadiusFilter.kmForStep(1), 0.0001);
        assertEquals(10, RadiusFilter.kmForStep(10), 0.0001);
        assertFalse(RadiusFilter.isAll(RadiusFilter.kmForStep(10)));
    }

    @Test
    public void lastStepMeansTheWholeMetro() {
        assertTrue(RadiusFilter.isAll(RadiusFilter.kmForStep(RadiusFilter.ALL_STEP)));
    }

    @Test
    public void smallRadiusKeepsOnlyNearbyHotspots() {
        List<CrimeHotspot> out = RadiusFilter.filter(sample(), USER, 1);
        assertEquals(1, out.size());
        assertEquals("near", out.get(0).id);
    }

    @Test
    public void tenKilometresStillExcludesTheMetroEdge() {
        assertEquals(2, RadiusFilter.filter(sample(), USER, 10).size());
    }

    @Test
    public void wholeMetroIncludesHotspotsAtTheFarEdge() {
        List<CrimeHotspot> out = RadiusFilter.filter(sample(), USER, RadiusFilter.kmForStep(RadiusFilter.ALL_STEP));
        assertEquals(3, out.size());
    }

    @Test
    public void unknownLocationFiltersNothing() {
        assertEquals(3, RadiusFilter.filter(sample(), null, 1).size());
    }

    @Test
    public void labelsMatchTheChoice() {
        assertEquals("Alert radius: 5 km", RadiusFilter.label(5));
        assertEquals("Whole Nelson Mandela Bay", RadiusFilter.label(Double.POSITIVE_INFINITY));
    }
}
