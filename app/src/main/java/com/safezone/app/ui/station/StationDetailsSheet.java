package com.safezone.app.ui.station;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.safezone.app.R;
import com.safezone.app.model.PoliceStation;
import com.safezone.app.ui.common.Dialer;
import com.safezone.app.ui.common.Ui;
import com.safezone.app.util.EmergencyNumbers;
import com.safezone.app.util.GeoUtils;
import com.safezone.app.util.LatLng;

public final class StationDetailsSheet {
    private StationDetailsSheet() {}

    public static void show(Context ctx, PoliceStation s, LatLng user) {
        BottomSheetDialog dialog = new BottomSheetDialog(ctx);
        View v = LayoutInflater.from(ctx).inflate(R.layout.sheet_station_details, null);

        int navy = ContextCompat.getColor(ctx, R.color.navy);
        android.graphics.drawable.GradientDrawable bg = new android.graphics.drawable.GradientDrawable();
        bg.setColor(ContextCompat.getColor(ctx, R.color.surface));
        float r = Ui.dp(ctx, 24);
        bg.setCornerRadii(new float[]{r, r, r, r, 0, 0, 0, 0});
        v.setBackground(bg);
        v.findViewById(R.id.handle).setBackground(
                Ui.rounded(ctx, ContextCompat.getColor(ctx, R.color.outline_grey), 2));
        v.findViewById(R.id.dot).setBackground(Ui.rounded(ctx, navy, 2));

        ((TextView) v.findViewById(R.id.name)).setText(s.name);
        TextView address = v.findViewById(R.id.address);
        boolean hasAddress = s.address != null && !s.address.trim().isEmpty();
        String details = hasAddress ? (s.hasPhone() ? s.address + "\n" + s.phone : s.address) : s.phone;
        address.setText(details);
        address.setVisibility(details == null || details.isEmpty() ? View.GONE : View.VISIBLE);

        TextView distance = v.findViewById(R.id.distance);
        if (user != null && s.hasCoordinates()) {
            double km = GeoUtils.distanceKm(user, new LatLng(s.latitude, s.longitude));
            distance.setText(ctx.getString(R.string.stations_distance, km));
        } else {
            distance.setVisibility(View.GONE);
        }

        View call = v.findViewById(R.id.call);
        call.setVisibility(s.hasPhone() ? View.VISIBLE : View.GONE);
        if (s.hasPhone()) {
            call.setContentDescription("Call " + s.name + " on " + s.phone);
            call.setOnClickListener(x -> Dialer.dial(x, s.phone, s.phone));
        }
        View directions = v.findViewById(R.id.directions);
        directions.setVisibility(s.hasCoordinates() ? View.VISIBLE : View.GONE);
        if (s.hasCoordinates()) {
            directions.setContentDescription("Directions to " + s.name);
            directions.setOnClickListener(x -> Dialer.directions(x, s.name, s.latitude, s.longitude));
        }

        View emergency = v.findViewById(R.id.emergency);
        emergency.setContentDescription(ctx.getString(R.string.stations_emergency_cd,
                EmergencyNumbers.SAPS_EMERGENCY.display));
        emergency.setOnClickListener(x -> Dialer.dial(x, EmergencyNumbers.SAPS_EMERGENCY.dial,
                EmergencyNumbers.SAPS_EMERGENCY.display));

        ((TextView) v.findViewById(R.id.listing)).setText(
                ctx.getString(R.string.station_sheet_listing, s.verifiedOn));

        dialog.setContentView(v);
        FrameLayout sheet = dialog.findViewById(com.google.android.material.R.id.design_bottom_sheet);
        if (sheet != null) sheet.setBackgroundResource(android.R.color.transparent);
        dialog.show();
    }
}
