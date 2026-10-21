package com.safezone.app.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import com.safezone.app.model.CrimeHotspot;

import org.junit.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

public class MockHotspotServiceTest {
    private List<CrimeHotspot> first() {
        AtomicReference<List<CrimeHotspot>> ref = new AtomicReference<>();
        new MockHotspotService(Scheduler.IMMEDIATE).watchHotspots(new DataListener<List<CrimeHotspot>>() {
            @Override public void onData(List<CrimeHotspot> d) { ref.set(d); }
            @Override public void onError(Exception e) { throw new AssertionError(e); }
        });
        return ref.get();
    }

    @Test
    public void watchHotspotsEmitsANonEmptyList() {
        assertFalse(first().isEmpty());
    }

    @Test
    public void watchHotspotsEmitsHotspotsWithUniqueIds() {
        List<CrimeHotspot> hotspots = first();
        Set<String> ids = new HashSet<>();
        for (CrimeHotspot h : hotspots) ids.add(h.id);
        assertEquals(hotspots.size(), ids.size());
    }
}
