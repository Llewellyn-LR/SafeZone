package com.safezone.app.service;

import android.content.Context;
import android.content.SharedPreferences;

import com.safezone.app.model.CrimeHotspot;
import com.safezone.app.util.LatLng;
import com.safezone.app.util.RadiusFilter;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public final class NearbyFilter {
    public interface Listener { void onChanged(); }

    private static final String PREFS = "safezone_prefs";
    private static final String KEY_STEP = "radius_step";
    private static final String KEY_SHOW_STATIONS = "map_show_stations";

    private final SharedPreferences prefs;
    private final List<Listener> listeners = new CopyOnWriteArrayList<>();
    private LatLng location;

    public NearbyFilter(Context context) {
        prefs = context.getApplicationContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    public void addListener(Listener l) { listeners.add(l); }
    public void removeListener(Listener l) { listeners.remove(l); }
    private void changed() { for (Listener l : listeners) l.onChanged(); }

    public int getStep() { return prefs.getInt(KEY_STEP, RadiusFilter.DEFAULT_STEP); }

    public void setStep(int step) {
        if (step == getStep()) return;
        prefs.edit().putInt(KEY_STEP, step).apply();
        changed();
    }

    public boolean getShowStations() { return prefs.getBoolean(KEY_SHOW_STATIONS, true); }

    public void setShowStations(boolean show) {
        if (show == getShowStations()) return;
        prefs.edit().putBoolean(KEY_SHOW_STATIONS, show).apply();
        changed();
    }

    public double getRadiusKm() { return RadiusFilter.kmForStep(getStep()); }
    public LatLng getLocation() { return location; }

    public void setLocation(LatLng l) {
        location = l;
        changed();
    }

    public List<CrimeHotspot> apply(List<CrimeHotspot> all) {
        return RadiusFilter.filter(all, location, getRadiusKm());
    }
}
