package com.safezone.app.ui.home;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.ColorRes;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.appbar.MaterialToolbar;
import com.safezone.app.R;
import com.safezone.app.ServiceLocator;
import com.safezone.app.model.AppUser;
import com.safezone.app.model.CrimeHotspot;
import com.safezone.app.service.AuthService;
import com.safezone.app.service.DataListener;
import com.safezone.app.service.Subscription;
import com.safezone.app.ui.anim.Pulse;
import com.safezone.app.ui.common.StreamErrorView;
import com.safezone.app.ui.common.Ui;
import com.safezone.app.ui.help.HelpActivity;
import com.safezone.app.ui.sos.SosActivity;
import com.safezone.app.ui.station.StationsActivity;
import com.safezone.app.ui.wanted.WantedPersonsListActivity;
import com.safezone.app.service.NearbyFilter;
import com.safezone.app.util.RadiusFilter;

import java.util.List;

public class HomeFragment extends Fragment {
    private View sosButton;
    private ObjectAnimator sosPulse;

    public interface TabNavigator { void navigateToTab(int index); }

    private Subscription subscription;
    private List<CrimeHotspot> latestAlerts;
    private final NearbyFilter.Listener nearbyListener = () -> { if (latestAlerts != null && isAdded()) showAlerts(latestAlerts); };
    private ViewGroup alertsContainer;
    private TextView greeting;
    private final AuthService.Listener authListener = this::updateGreeting;

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater i, @Nullable ViewGroup c, @Nullable Bundle s) {
        return i.inflate(R.layout.fragment_home, c, false);
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle s) {
        Context ctx = requireContext();
        MaterialToolbar tb = v.findViewById(R.id.toolbar);
        tb.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.action_help) startActivity(new Intent(ctx, HelpActivity.class));
            else navigate(3);
            return true;
        });
        sosButton = v.findViewById(R.id.sosButton);
        sosButton.setOnClickListener(x -> startActivity(new Intent(ctx, SosActivity.class)));
        sosPulse = Pulse.heartbeat(sosButton);

        greeting = v.findViewById(R.id.greeting);
        updateGreeting();
        ServiceLocator.auth().addListener(authListener);
        ServiceLocator.nearby().addListener(nearbyListener);

        bindTile(v.findViewById(R.id.tileReport), R.drawable.ic_report, "Report",
                R.color.coral, x -> navigate(2));
        bindTile(v.findViewById(R.id.tileMap), R.drawable.ic_map, "Map",
                R.color.navy, x -> navigate(1));
        bindTile(v.findViewById(R.id.tileStation), R.drawable.ic_local_police, "Station",
                R.color.green, x -> startActivity(new Intent(ctx, StationsActivity.class)));
        bindTile(v.findViewById(R.id.tileWanted), R.drawable.ic_person_search, "Wanted",
                R.color.amber, x -> startActivity(new Intent(ctx, WantedPersonsListActivity.class)));

        alertsContainer = v.findViewById(R.id.alertsContainer);
        showLoading();
        subscription = ServiceLocator.hotspots().watchHotspots(new DataListener<List<CrimeHotspot>>() {
            @Override public void onData(List<CrimeHotspot> data) { latestAlerts = data; showAlerts(data); }
            @Override public void onError(Exception e) { showError(); }
        });
    }

    @Override public void onResume() { super.onResume(); if (!isHidden()) Pulse.resume(sosPulse); }

    @Override public void onPause() { Pulse.pause(sosPulse); super.onPause(); }

    @Override public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (hidden) Pulse.pause(sosPulse); else Pulse.resume(sosPulse);
    }

    @Override
    public void onDestroyView() {
        if (sosButton != null) Pulse.cancel(sosPulse, sosButton);
        sosPulse = null;
        sosButton = null;
        if (subscription != null) subscription.cancel();
        ServiceLocator.auth().removeListener(authListener);
        ServiceLocator.nearby().removeListener(nearbyListener);
        super.onDestroyView();
    }

    private void navigate(int tab) {
        ((TabNavigator) requireActivity()).navigateToTab(tab);
    }

    private void updateGreeting() {
        AppUser user = ServiceLocator.auth().getCurrentUser();
        String name = (user == null || user.isGuest) ? "there" : user.name;
        greeting.setText("Hi, " + name);
    }

    private void bindTile(View tile, @DrawableRes int icon, String label,
                          @ColorRes int colorRes, View.OnClickListener click) {
        int color = ContextCompat.getColor(requireContext(), colorRes);
        ImageView iv = tile.findViewById(R.id.qaIcon);
        iv.setImageResource(icon);
        iv.setImageTintList(ColorStateList.valueOf(color));
        iv.setBackground(Ui.circle(Ui.withAlpha(color, 0.12f)));
        ((TextView) tile.findViewById(R.id.qaLabel)).setText(label);
        tile.setOnClickListener(click);
    }

    private void showLoading() {
        alertsContainer.removeAllViews();
        getLayoutInflater().inflate(R.layout.view_loading, alertsContainer, true);
    }

    private void showError() {
        alertsContainer.removeAllViews();
        StreamErrorView e = new StreamErrorView(requireContext());
        e.setMessage("Couldn't load recent alerts.");
        alertsContainer.addView(e);
    }

    private void showAlerts(List<CrimeHotspot> raw) {
        alertsContainer.removeAllViews();

        List<CrimeHotspot> all = ServiceLocator.nearby().apply(raw);
        int n = Math.min(3, all.size());
        if (n == 0) {
            TextView t = new TextView(requireContext());
            t.setText(raw.isEmpty() ? "No alerts to show right now."
                    : "No alerts within your chosen radius. Change it on the Map tab.");
            t.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_secondary));
            alertsContainer.addView(t);
            return;
        }
        for (int i = 0; i < n; i++) {
            CrimeHotspot h = all.get(i);
            View card = getLayoutInflater().inflate(R.layout.item_alert_preview, alertsContainer, false);
            card.findViewById(R.id.severityBar).setBackground(Ui.rounded(requireContext(),
                    ContextCompat.getColor(requireContext(), h.severity.colorRes), 4));
            ((TextView) card.findViewById(R.id.alertTitle)).setText(h.title);
            ((TextView) card.findViewById(R.id.alertArea)).setText(h.area);
            ((TextView) card.findViewById(R.id.alertTime)).setText(h.timeAgo);
            alertsContainer.addView(card);
        }
    }
}
