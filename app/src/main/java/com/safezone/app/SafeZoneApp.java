package com.safezone.app;

import android.app.Application;

import com.google.firebase.FirebaseApp;

import org.osmdroid.config.Configuration;
import org.osmdroid.config.IConfigurationProvider;

import java.io.File;

public class SafeZoneApp extends Application {

    private static boolean firebaseAvailable;
    private static SafeZoneApp instance;

    public static SafeZoneApp get() { return instance; }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        try {
            firebaseAvailable = FirebaseApp.initializeApp(this) != null
                    || !FirebaseApp.getApps(this).isEmpty();
        } catch (Exception e) {
            firebaseAvailable = false;
        }

        IConfigurationProvider cfg = Configuration.getInstance();
        cfg.load(this, getSharedPreferences("osmdroid", MODE_PRIVATE));
        cfg.setUserAgentValue("com.safezone.app");
        File base = new File(getCacheDir(), "osmdroid");
        File tiles = new File(base, "tiles");

        tiles.mkdirs();
        cfg.setOsmdroidBasePath(base);
        cfg.setOsmdroidTileCache(tiles);
    }

    public static boolean isFirebaseAvailable() {
        return firebaseAvailable;
    }
}
