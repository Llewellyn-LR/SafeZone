package com.safezone.app.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class GeoUtilsTest {
    @Test
    public void returnsZeroForIdenticalPoints() {
        LatLng p = new LatLng(-33.9608, 25.6022);
        assertEquals(0, GeoUtils.distanceKm(p, p), 0.0001);
    }

    @Test
    public void computesAKnownRealWorldDistanceWithinTolerance() {

        LatLng cbd = new LatLng(-33.9608, 25.6022);
        LatLng motherwell = new LatLng(-33.8215, 25.5860);
        double result = GeoUtils.distanceKm(cbd, motherwell);
        assertTrue(result > 14);
        assertTrue(result < 20);
    }

    @Test
    public void isSymmetricRegardlessOfArgumentOrder() {
        LatLng a = new LatLng(-33.89, 25.56);
        LatLng b = new LatLng(-33.93, 25.57);
        assertEquals(GeoUtils.distanceKm(a, b), GeoUtils.distanceKm(b, a), 0.0001);
    }

    @Test
    public void increasesMonotonicallyWithLatitudeOffset() {
        LatLng origin = new LatLng(-33.90, 25.57);
        LatLng near = new LatLng(-33.91, 25.57);
        LatLng far = new LatLng(-33.95, 25.57);
        assertTrue(GeoUtils.distanceKm(origin, far) > GeoUtils.distanceKm(origin, near));
    }
}
