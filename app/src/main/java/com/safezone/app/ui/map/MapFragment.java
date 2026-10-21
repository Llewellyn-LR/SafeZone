package com.safezone.app.ui.map;

import android.graphics.Paint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.slider.Slider;
import com.safezone.app.R;
import com.safezone.app.ServiceLocator;
import com.safezone.app.model.CrimeHotspot;
import com.safezone.app.model.HotspotSeverity;
import com.safezone.app.model.PoliceStation;
import com.safezone.app.service.AssetStationService;
import com.safezone.app.ui.station.StationDetailsSheet;
import com.safezone.app.util.StationMapFilter;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.safezone.app.service.DataListener;
import com.safezone.app.service.LocationService;
import com.safezone.app.service.NearbyFilter;
import com.safezone.app.util.RadiusFilter;
import org.osmdroid.util.BoundingBox;
import com.safezone.app.service.Subscription;
import com.safezone.app.ui.common.HotspotDetailsSheet;
import com.safezone.app.ui.common.StreamErrorView;
import com.safezone.app.ui.common.Ui;
import com.safezone.app.util.GeoUtils;
import com.safezone.app.util.LatLng;
import com.safezone.app.util.MapConstants;

import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.CustomZoomButtonsController;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Overlay;
import org.osmdroid.views.overlay.Polygon;

import java.util.ArrayList;
import java.util.List;

public class MapFragment extends Fragment {
    private MapView map;
    private View progress, content;
    private StreamErrorView errorView;
    private TextView radiusLabel, countLabel;
    private Subscription subscription;

    private List<CrimeHotspot> hotspots;
    private boolean hasError;
    private LatLng userLocation = MapConstants.FALLBACK_USER_LOCATION;
    private boolean locationResolved;
    private MapViewModel vm;
    private final List<Overlay> dynamicOverlays = new ArrayList<>();
    private List<PoliceStation> stations = new ArrayList<>();
    private View legendStationRow;

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater i, @Nullable ViewGroup c, @Nullable Bundle s) {
        return i.inflate(R.layout.fragment_map, c, false);
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle s) {
        vm = new ViewModelProvider(this).get(MapViewModel.class);
        progress = v.findViewById(R.id.progress);
        content = v.findViewById(R.id.content);
        errorView = v.findViewById(R.id.errorView);
        radiusLabel = v.findViewById(R.id.radiusLabel);
        countLabel = v.findViewById(R.id.countLabel);

        map = v.findViewById(R.id.map);
        map.setTileSource(TileSourceFactory.MAPNIK);
        map.setMultiTouchControls(true);
        map.getZoomController().setVisibility(CustomZoomButtonsController.Visibility.NEVER);
        map.getController().setZoom(11.5);
        map.getController().setCenter(
                new GeoPoint(MapConstants.NMB_CENTER.latitude, MapConstants.NMB_CENTER.longitude));

        int ctxPad = Ui.dp(requireContext(), 0);
        v.findViewById(R.id.legendDotHigh).setBackground(dot(R.color.coral));
        v.findViewById(R.id.legendDotMedium).setBackground(dot(R.color.amber));
        v.findViewById(R.id.legendDotLow).setBackground(dot(R.color.green));
        v.findViewById(R.id.legendStation).setBackground(
                Ui.rounded(requireContext(), ContextCompat.getColor(requireContext(), R.color.navy), 2));
        legendStationRow = v.findViewById(R.id.legendStationRow);

        stations = StationMapFilter.pinnable(new AssetStationService(requireContext()).getStations(),
                null, Double.POSITIVE_INFINITY);

        MaterialSwitch stationSwitch = v.findViewById(R.id.stationSwitch);
        stationSwitch.setChecked(nearby().getShowStations());
        stationSwitch.setOnCheckedChangeListener((b, checked) -> {
            nearby().setShowStations(checked);
            refresh();
        });

        Slider slider = v.findViewById(R.id.radiusSlider);
        slider.setValue(nearby().getStep());
        slider.addOnChangeListener((sl, value, fromUser) -> {
            nearby().setStep(Math.round(value));
            refresh();
        });

        vm.location().observe(getViewLifecycleOwner(), loc -> {
            if (loc == null) return;
            userLocation = loc;
            locationResolved = true;
            nearby().setLocation(loc);
            refresh();
        });

        if (vm.markLocationRequested()) {
            final MapViewModel viewModel = vm;
            ServiceLocator.location().getCurrentLocation(requireContext(),
                    (LocationService.PermissionHost) requireActivity(), viewModel::postLocation);
        }

        subscription = ServiceLocator.hotspots().watchHotspots(new DataListener<List<CrimeHotspot>>() {
            @Override public void onData(List<CrimeHotspot> data) {
                hotspots = data;
                hasError = false;
                refresh();
            }
            @Override public void onError(Exception e) {
                hasError = true;
                refresh();
            }
        });
        refresh();
    }

    private android.graphics.drawable.Drawable dot(int colorRes) {
        return Ui.circle(ContextCompat.getColor(requireContext(), colorRes));
    }

    private void refresh() {
        if (!isAdded() || map == null) return;
        if (hasError) {
            progress.setVisibility(View.GONE);
            content.setVisibility(View.GONE);
            errorView.setVisibility(View.VISIBLE);
            return;
        }
        errorView.setVisibility(View.GONE);
        if (hotspots == null || !locationResolved) {
            progress.setVisibility(View.VISIBLE);
            content.setVisibility(View.GONE);
            return;
        }
        progress.setVisibility(View.GONE);
        content.setVisibility(View.VISIBLE);

        List<CrimeHotspot> visible = inRadius(hotspots);
        List<PoliceStation> shownStations = nearby().getShowStations()
                ? StationMapFilter.pinnable(stations, userLocation, nearby().getRadiusKm())
                : new ArrayList<PoliceStation>();
        legendStationRow.setVisibility(nearby().getShowStations() ? View.VISIBLE : View.GONE);
        radiusLabel.setText(RadiusFilter.label(nearby().getRadiusKm()));
        String count = visible.size() + " incident" + (visible.size() == 1 ? "" : "s") + " nearby";
        if (nearby().getShowStations()) {
            count += " · " + shownStations.size() + " station" + (shownStations.size() == 1 ? "" : "s");
        }
        countLabel.setText(count);
        rebuildOverlays(visible, shownStations);
    }

    private List<CrimeHotspot> inRadius(List<CrimeHotspot> all) {
        List<CrimeHotspot> out = new ArrayList<>();
        for (CrimeHotspot h : all) {
            double d = GeoUtils.distanceKm(userLocation, new LatLng(h.latitude, h.longitude));
            if (d <= nearby().getRadiusKm()) out.add(h);
        }
        return out;
    }

    private void rebuildOverlays(List<CrimeHotspot> visible, List<PoliceStation> shownStations) {
        for (Overlay o : dynamicOverlays) map.getOverlays().remove(o);
        dynamicOverlays.clear();

        int navy = ContextCompat.getColor(requireContext(), R.color.navy);
        GeoPoint me = new GeoPoint(userLocation.latitude, userLocation.longitude);

        Polygon circle = new Polygon(map);
        circle.setPoints(Polygon.pointsAsCircle(me, RadiusFilter.isAll(nearby().getRadiusKm())
                ? 1 : nearby().getRadiusKm() * 1000));
        circle.getFillPaint().setColor(Ui.withAlpha(navy, 0.08f));
        circle.getOutlinePaint().setColor(Ui.withAlpha(navy, 0.4f));
        circle.getOutlinePaint().setStrokeWidth(Ui.dp(requireContext(), 1.5f));
        circle.getOutlinePaint().setStyle(Paint.Style.STROKE);
        circle.setInfoWindow(null);
        if (!RadiusFilter.isAll(nearby().getRadiusKm())) add(circle);

        Marker user = new Marker(map);
        user.setPosition(me);
        user.setIcon(PinFactory.userDot(requireContext(), navy));
        user.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER);
        user.setInfoWindow(null);
        add(user);

        for (CrimeHotspot h : visible) {
            Marker m = new Marker(map);
            m.setPosition(new GeoPoint(h.latitude, h.longitude));
            m.setIcon(PinFactory.hotspotPin(requireContext(),
                    ContextCompat.getColor(requireContext(), h.severity.colorRes), h.verified,
                    h.category, h.severity == HotspotSeverity.HIGH));
            m.setAnchor(PinFactory.ANCHOR_X, PinFactory.ANCHOR_Y);
            m.setInfoWindow(null);
            m.setOnMarkerClickListener((marker, mv) -> {
                HotspotDetailsSheet.show(requireContext(), h);
                return true;
            });
            add(m);
        }

        int insertAt = map.getOverlays().indexOf(user) + 1;
        for (PoliceStation st : shownStations) {
            Marker m = new Marker(map);
            m.setPosition(new GeoPoint(st.latitude, st.longitude));
            m.setIcon(PinFactory.stationPin(requireContext(), navy));
            m.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER);
            m.setInfoWindow(null);
            m.setOnMarkerClickListener((marker, mv) -> {
                StationDetailsSheet.show(requireContext(), st, userLocation);
                return true;
            });
            map.getOverlays().add(insertAt++, m);
            dynamicOverlays.add(m);
        }
        map.invalidate();
        if (RadiusFilter.isAll(nearby().getRadiusKm())) fitAll(me, visible, shownStations);
    }

    private void fitAll(GeoPoint me, List<CrimeHotspot> visible, List<PoliceStation> shownStations) {
        List<GeoPoint> pts = new ArrayList<>();
        pts.add(me);
        for (CrimeHotspot h : visible) pts.add(new GeoPoint(h.latitude, h.longitude));
        for (PoliceStation st : shownStations) pts.add(new GeoPoint(st.latitude, st.longitude));
        if (pts.size() < 2) return;
        final BoundingBox box = BoundingBox.fromGeoPoints(pts);
        map.post(() -> {
            if (map != null) map.zoomToBoundingBox(box, true, Ui.dp(requireContext(), 64));
        });
    }

    private NearbyFilter nearby() { return ServiceLocator.nearby(); }

    private void add(Overlay o) {
        map.getOverlays().add(o);
        dynamicOverlays.add(o);
    }

    @Override public void onResume() { super.onResume(); if (map != null) map.onResume(); }
    @Override public void onPause() { if (map != null) map.onPause(); super.onPause(); }

    @Override
    public void onDestroyView() {
        if (subscription != null) subscription.cancel();
        if (map != null) map.onDetach();
        map = null;
        super.onDestroyView();
    }
}
