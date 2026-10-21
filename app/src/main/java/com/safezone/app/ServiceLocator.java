package com.safezone.app;

import android.content.Context;

import com.safezone.app.service.AuthService;
import com.safezone.app.service.FirebaseAuthService;
import com.safezone.app.service.FirebaseHotspotService;
import com.safezone.app.service.FirebaseReportService;
import com.safezone.app.service.FirebaseWantedPersonService;
import com.safezone.app.service.HotspotService;
import com.safezone.app.service.LocationService;
import com.safezone.app.service.MockAuthService;
import com.safezone.app.service.MockHotspotService;
import com.safezone.app.service.MockReportService;
import com.safezone.app.service.MockWantedPersonService;
import com.safezone.app.service.NearbyFilter;
import com.safezone.app.service.NotificationService;
import com.safezone.app.service.ReportService;
import com.safezone.app.service.WantedPersonService;

public final class ServiceLocator {
    private static AuthService auth;
    private static HotspotService hotspots;
    private static ReportService reports;
    private static NotificationService notifications;
    private static WantedPersonService wantedPersons;
    private static FirebaseWantedPersonService firebaseWantedPersons;
    private static final LocationService LOCATION = new LocationService();
    private static NearbyFilter nearby;

    private ServiceLocator() {}

    private static boolean fb() { return SafeZoneApp.isFirebaseAvailable(); }

    public static synchronized AuthService auth() {
        if (auth == null) auth = fb() ? new FirebaseAuthService() : new MockAuthService();
        return auth;
    }

    public static synchronized HotspotService hotspots() {
        if (hotspots == null) hotspots = fb() ? new FirebaseHotspotService() : new MockHotspotService();
        return hotspots;
    }

    public static synchronized ReportService reports() {
        if (reports == null) reports = fb() ? new FirebaseReportService(SafeZoneApp.get()) : new MockReportService();
        return reports;
    }

    public static synchronized FirebaseReportService firebaseReports() {
        return (FirebaseReportService) reports();
    }

    public static synchronized NotificationService notifications() {
        if (notifications == null) notifications = new NotificationService(fb());
        return notifications;
    }

    public static LocationService location() { return LOCATION; }

    public static synchronized NearbyFilter nearby() {
        if (nearby == null) nearby = new NearbyFilter(SafeZoneApp.get());
        return nearby;
    }

    public static synchronized WantedPersonService wantedPersons(Context ctx) {
        if (wantedPersons == null) {
            wantedPersons = fb() ? firebaseWantedPersons(ctx) : new MockWantedPersonService();
        }
        return wantedPersons;
    }

    public static synchronized FirebaseWantedPersonService firebaseWantedPersons(Context ctx) {
        if (firebaseWantedPersons == null) {
            firebaseWantedPersons = new FirebaseWantedPersonService(ctx.getApplicationContext());
        }
        return firebaseWantedPersons;
    }
}
