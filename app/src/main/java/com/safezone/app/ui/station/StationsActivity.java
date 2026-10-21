package com.safezone.app.ui.station;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.safezone.app.R;
import com.safezone.app.ServiceLocator;
import com.safezone.app.model.PoliceStation;
import com.safezone.app.service.AssetStationService;
import com.safezone.app.service.StationService;
import com.safezone.app.ui.common.Dialer;
import com.safezone.app.util.EmergencyNumbers;
import com.safezone.app.util.LatLng;
import com.safezone.app.util.StationSorter;

import java.util.ArrayList;
import java.util.List;

public class StationsActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_stations);
        ((MaterialToolbar) findViewById(R.id.toolbar)).setNavigationOnClickListener(v -> finish());

        StationService service = new AssetStationService(this);
        List<PoliceStation> stations = service.getStations();

        LatLng user = ServiceLocator.nearby().getLocation();
        boolean realLocation = user != null && !ServiceLocator.location().wasLastFallback();
        List<StationSorter.Entry> sorted = StationSorter.sort(stations, realLocation ? user : null);

        TextView notice = findViewById(R.id.notice);
        if (!stations.isEmpty() && !realLocation) {
            notice.setText(R.string.stations_no_location);
            notice.setVisibility(View.VISIBLE);
        }
        findViewById(R.id.empty).setVisibility(stations.isEmpty() ? View.VISIBLE : View.GONE);

        View emergency = findViewById(R.id.emergency);
        emergency.setContentDescription(getString(R.string.stations_emergency_cd,
                EmergencyNumbers.SAPS_EMERGENCY.display));
        emergency.setOnClickListener(v -> Dialer.dial(v, EmergencyNumbers.SAPS_EMERGENCY.dial,
                EmergencyNumbers.SAPS_EMERGENCY.display));

        RecyclerView rv = findViewById(R.id.recycler);
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setAdapter(new Adapter(sorted));
    }

    private static final class Adapter extends RecyclerView.Adapter<Adapter.VH> {
        private final List<StationSorter.Entry> items = new ArrayList<>();
        Adapter(List<StationSorter.Entry> l) { items.addAll(l); }

        @NonNull @Override
        public VH onCreateViewHolder(@NonNull ViewGroup p, int t) {
            return new VH(LayoutInflater.from(p.getContext()).inflate(R.layout.item_station, p, false));
        }

        @Override
        public void onBindViewHolder(@NonNull VH h, int pos) {
            StationSorter.Entry e = items.get(pos);
            PoliceStation s = e.station;
            h.name.setText(s.name);
            boolean hasAddress = s.address != null && !s.address.isEmpty();
            String details = hasAddress ? (s.hasPhone() ? s.address + "\n" + s.phone : s.address) : s.phone;
            h.address.setText(details);
            h.address.setVisibility(details == null || details.isEmpty() ? View.GONE : View.VISIBLE);
            android.content.Context c = h.itemView.getContext();
            if (e.distanceKm >= 0) {
                h.distance.setText(c.getString(R.string.stations_distance, e.distanceKm));
                h.distance.setVisibility(View.VISIBLE);
            } else if (!s.hasCoordinates()) {
                h.distance.setText(R.string.stations_no_coordinates);
                h.distance.setVisibility(View.VISIBLE);
            } else {
                h.distance.setVisibility(View.GONE);
            }

            h.call.setVisibility(s.hasPhone() ? View.VISIBLE : View.GONE);
            if (s.hasPhone()) {
                h.call.setContentDescription("Call " + s.name + " on " + s.phone);
                h.call.setOnClickListener(x -> Dialer.dial(x, s.phone, s.phone));
            }
            h.directions.setVisibility(s.hasCoordinates() ? View.VISIBLE : View.GONE);
            if (s.hasCoordinates()) {
                h.directions.setContentDescription("Directions to " + s.name);
                h.directions.setOnClickListener(x -> Dialer.directions(x, s.name, s.latitude, s.longitude));
            }
        }

        @Override public int getItemCount() { return items.size(); }

        static final class VH extends RecyclerView.ViewHolder {
            final TextView name, address, distance;
            final com.google.android.material.button.MaterialButton call;
            final View directions;
            VH(@NonNull View v) {
                super(v);
                name = v.findViewById(R.id.name);
                address = v.findViewById(R.id.address);
                distance = v.findViewById(R.id.distance);
                call = v.findViewById(R.id.call);
                directions = v.findViewById(R.id.directions);
            }
        }
    }
}
