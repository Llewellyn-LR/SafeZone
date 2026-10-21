package com.safezone.app.service;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Handler;
import android.os.Looper;

import androidx.core.content.ContextCompat;
import androidx.core.location.LocationManagerCompat;

import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.tasks.CancellationTokenSource;
import com.safezone.app.util.LatLng;
import com.safezone.app.util.MapConstants;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

public class LocationService {

    public interface PermissionHost {
        void requestLocationPermission(Consumer<Boolean> result);
    }

    public interface Callback { void onLocation(LatLng location); }

    private static final long TIME_LIMIT_MS = 8000;

    private volatile boolean lastWasFallback = true;

    public boolean wasLastFallback() { return lastWasFallback; }

    public void getCurrentLocation(Context ctx, PermissionHost host, Callback cb) {
        final Context app = ctx.getApplicationContext();
        AtomicBoolean done = new AtomicBoolean(false);
        Callback once = l -> {
            if (done.compareAndSet(false, true)) {
                lastWasFallback = l == MapConstants.FALLBACK_USER_LOCATION;
                cb.onLocation(l);
            }
        };
        try {
            LocationManager lm = (LocationManager) app.getSystemService(Context.LOCATION_SERVICE);
            if (lm == null || !LocationManagerCompat.isLocationEnabled(lm)) {
                once.onLocation(MapConstants.FALLBACK_USER_LOCATION);
                return;
            }
            if (hasPermission(app)) {
                fetch(app, once);
            } else {
                host.requestLocationPermission(granted -> {
                    if (Boolean.TRUE.equals(granted)) fetch(app, once);
                    else once.onLocation(MapConstants.FALLBACK_USER_LOCATION);
                });
            }
        } catch (Exception e) {
            once.onLocation(MapConstants.FALLBACK_USER_LOCATION);
        }
    }

    private static boolean hasPermission(Context c) {
        return ContextCompat.checkSelfPermission(c, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(c, Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
    }

    @SuppressWarnings("MissingPermission")
    private static void fetch(Context app, Callback once) {
        Handler main = new Handler(Looper.getMainLooper());
        CancellationTokenSource cts = new CancellationTokenSource();

        Runnable timeout = () -> {
            cts.cancel();
            once.onLocation(MapConstants.FALLBACK_USER_LOCATION);
        };
        main.postDelayed(timeout, TIME_LIMIT_MS);
        try {
            LocationServices.getFusedLocationProviderClient(app)
                    .getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, cts.getToken())
                    .addOnCompleteListener(t -> {
                        main.removeCallbacks(timeout);
                        if (t.isSuccessful() && t.getResult() != null) {
                            once.onLocation(new LatLng(t.getResult().getLatitude(),
                                    t.getResult().getLongitude()));
                        } else {
                            once.onLocation(MapConstants.FALLBACK_USER_LOCATION);
                        }
                    });
        } catch (Exception e) {
            main.removeCallbacks(timeout);
            once.onLocation(MapConstants.FALLBACK_USER_LOCATION);
        }
    }
}
