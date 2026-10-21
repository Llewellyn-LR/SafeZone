package com.safezone.app.ui.home;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.safezone.app.R;
import com.safezone.app.SafeZoneApp;
import com.safezone.app.ServiceLocator;
import com.safezone.app.service.LocationService;
import com.safezone.app.ui.alerts.AlertsFragment;
import com.safezone.app.ui.map.MapFragment;
import com.safezone.app.ui.profile.ProfileFragment;
import com.safezone.app.ui.report.ReportFragment;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.function.Consumer;

public class MainActivity extends AppCompatActivity
        implements HomeFragment.TabNavigator, LocationService.PermissionHost {

    private static final int[] IDS = {R.id.nav_home, R.id.nav_map, R.id.nav_report,
            R.id.nav_alerts, R.id.nav_profile};

    private static final class PermRequest {
        final String[] perms;
        final Consumer<Boolean> callback;
        PermRequest(String[] p, Consumer<Boolean> c) { perms = p; callback = c; }
    }

    private final Deque<PermRequest> queue = new ArrayDeque<>();
    private boolean requestInFlight;
    private ActivityResultLauncher<String[]> permissionLauncher;
    private BottomNavigationView nav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        permissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestMultiplePermissions(), result -> {
                    boolean any = false;
                    for (Boolean granted : result.values()) any |= Boolean.TRUE.equals(granted);
                    PermRequest done = queue.poll();
                    requestInFlight = false;
                    if (done != null) done.callback.accept(any);
                    launchNext();
                });

        nav = findViewById(R.id.bottomNav);
        nav.setOnItemSelectedListener(item -> {
            for (int i = 0; i < IDS.length; i++) {
                if (IDS[i] == item.getItemId()) { showTab(i); return true; }
            }
            return false;
        });
        if (savedInstanceState == null) showTab(0);

        initNotifications();
    }

    private void initNotifications() {
        if (!SafeZoneApp.isFirebaseAvailable()) return;
        if (Build.VERSION.SDK_INT >= 33 && ContextCompat.checkSelfPermission(this,
                Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            enqueue(new String[]{Manifest.permission.POST_NOTIFICATIONS},
                    granted -> ServiceLocator.notifications().init(granted));
        } else {
            ServiceLocator.notifications().init(true);
        }
    }

    @Override
    public void requestLocationPermission(Consumer<Boolean> result) {
        enqueue(new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION}, result);
    }

    private void enqueue(String[] perms, Consumer<Boolean> cb) {
        queue.add(new PermRequest(perms, cb));
        if (!requestInFlight) launchNext();
    }

    private void launchNext() {
        PermRequest next = queue.peek();
        if (next == null) return;
        requestInFlight = true;
        permissionLauncher.launch(next.perms);
    }

    @Override
    public void navigateToTab(int index) {
        nav.setSelectedItemId(IDS[index]);
    }

    private void showTab(int index) {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction()
                .setReorderingAllowed(true)
                .setCustomAnimations(R.anim.tab_in, R.anim.tab_out);
        for (int i = 0; i < IDS.length; i++) {
            Fragment f = getSupportFragmentManager().findFragmentByTag("tab" + i);
            if (f == null) {
                f = create(i);
                ft.add(R.id.container, f, "tab" + i);
            }
            if (i == index) ft.show(f); else ft.hide(f);
        }
        ft.commit();
    }

    private Fragment create(int i) {
        switch (i) {
            case 0: return new HomeFragment();
            case 1: return new MapFragment();
            case 2: return new ReportFragment();
            case 3: return new AlertsFragment();
            default: return new ProfileFragment();
        }
    }
}
