package com.safezone.app.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

public class CrimeHotspotTest {
    @Test
    public void severityRoundTripsEveryWireValue() {
        for (HotspotSeverity s : HotspotSeverity.values()) {
            assertEquals(s, HotspotSeverity.fromWireValue(s.wireValue));
        }
    }

    @Test
    public void severityFallsBackToMediumForUnknownOrNull() {
        assertEquals(HotspotSeverity.MEDIUM, HotspotSeverity.fromWireValue("not-a-real-value"));
        assertEquals(HotspotSeverity.MEDIUM, HotspotSeverity.fromWireValue(null));
    }

    @Test
    public void severityHasADistinctLabelEach() {
        Set<String> labels = new HashSet<>();
        for (HotspotSeverity s : HotspotSeverity.values()) labels.add(s.label);
        assertEquals(HotspotSeverity.values().length, labels.size());
    }

    @Test
    public void storesAllConstructorFieldsAsGiven() {
        CrimeHotspot h = new CrimeHotspot("h1", "Test incident", "Test Area", -33.9, 25.6,
                HotspotSeverity.HIGH, "5 min ago", "A test description.", false);
        assertEquals("h1", h.id);
        assertEquals(HotspotSeverity.HIGH, h.severity);
        assertFalse(h.verified);
    }

    @Test
    public void defaultsVerifiedToTrueWhenOmitted() {
        CrimeHotspot h = new CrimeHotspot("h2", "Test incident", "Test Area", -33.9, 25.6,
                HotspotSeverity.LOW, "now", "");
        assertTrue(h.verified);
    }

    @Test
    public void timeAgoUsesTheSameWordingAsTheDartHelper() {
        assertEquals("Just now", CrimeHotspot.timeAgo(30_000L));
        assertEquals("5 min ago", CrimeHotspot.timeAgo(5 * 60_000L));
        assertEquals("1 hr ago", CrimeHotspot.timeAgo(60 * 60_000L));
        assertEquals("2 hrs ago", CrimeHotspot.timeAgo(2 * 3_600_000L));
        assertEquals("1 day ago", CrimeHotspot.timeAgo(24 * 3_600_000L));
        assertEquals("3 days ago", CrimeHotspot.timeAgo(3 * 86_400_000L));
    }

    @Test
    public void categoryIsNullForHotspotsCreatedWithoutOne() {
        CrimeHotspot h = new CrimeHotspot("h3", "Old hotspot", "Area", -33.9, 25.6,
                HotspotSeverity.LOW, "now", "", true);
        assertNull(h.category);
    }

    @Test
    public void storesTheCategoryItWasGiven() {
        CrimeHotspot h = new CrimeHotspot("h4", "Vehicle crime", "Area", -33.9, 25.6,
                HotspotSeverity.HIGH, "now", "", true, ReportCategory.VEHICLE_CRIME);
        assertEquals(ReportCategory.VEHICLE_CRIME, h.category);
    }

    @Test
    public void gbvIsTheOnlyUnpublishableCategory() {
        for (ReportCategory c : ReportCategory.values()) {
            assertEquals(c != ReportCategory.GBV, c.isPublishable());
        }
    }

    @Test
    public void gbvHotspotsAreNeverPubliclyVisible() {
        CrimeHotspot tagged = new CrimeHotspot("g1", "x", "Area", -33.9, 25.6,
                HotspotSeverity.HIGH, "now", "", true, ReportCategory.GBV);
        CrimeHotspot legacy = new CrimeHotspot("g2", ReportCategory.GBV.label, "Area", -33.9, 25.6,
                HotspotSeverity.HIGH, "now", "", true);
        CrimeHotspot ok = new CrimeHotspot("o1", "Robbery", "Area", -33.9, 25.6,
                HotspotSeverity.HIGH, "now", "", true, ReportCategory.ROBBERY);
        assertFalse(tagged.isPubliclyVisible());
        assertFalse(legacy.isPubliclyVisible());
        assertTrue(ok.isPubliclyVisible());
        assertEquals(1, CrimeHotspot.publicOnly(java.util.Arrays.asList(tagged, legacy, ok)).size());
    }

    @Test
    public void alertsExpireAfter24Hours() {
        long now = 1_000_000_000_000L;
        java.util.Date fresh = new java.util.Date(now - 23 * 3_600_000L);
        java.util.Date old = new java.util.Date(now - 24 * 3_600_000L);
        CrimeHotspot a = new CrimeHotspot("a", "Robbery", "X", 0, 0, HotspotSeverity.HIGH,
                "", "", true, ReportCategory.ROBBERY, fresh);
        CrimeHotspot b = new CrimeHotspot("b", "Robbery", "X", 0, 0, HotspotSeverity.HIGH,
                "", "", true, ReportCategory.ROBBERY, old);
        CrimeHotspot unknownAge = new CrimeHotspot("c", "Robbery", "X", 0, 0, HotspotSeverity.HIGH,
                "", "", true, ReportCategory.ROBBERY, null);
        assertFalse(a.isExpired(now));
        assertTrue(b.isExpired(now));
        assertFalse(unknownAge.isExpired(now));
        assertEquals(2, CrimeHotspot.withoutExpired(java.util.Arrays.asList(a, b, unknownAge), now).size());
    }
}
