package com.safezone.app.ui.admin;

import android.content.Context;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.safezone.app.R;
import com.safezone.app.model.CrimeReport;
import com.safezone.app.model.HotspotSeverity;
import com.safezone.app.ui.common.Ui;

import java.util.function.Consumer;

final class ApproveReportDialog {
    private ApproveReportDialog() {}

    static final class Decision {
        final HotspotSeverity severity;
        final Double latitude, longitude;
        Decision(HotspotSeverity s, Double lat, Double lng) { severity = s; latitude = lat; longitude = lng; }
    }

    static void show(Context ctx, CrimeReport report, Consumer<Decision> onDecision) {
        boolean hasLocation = report.latitude != null && report.longitude != null;
        View v = LayoutInflater.from(ctx).inflate(R.layout.dialog_approve_report, null);

        final HotspotSeverity[] selected = {HotspotSeverity.MEDIUM};
        ChipGroup group = v.findViewById(R.id.severityGroup);
        for (HotspotSeverity s : HotspotSeverity.values()) {
            int color = ContextCompat.getColor(ctx, s.colorRes);
            Chip chip = new Chip(ctx, null,
                    com.google.android.material.R.attr.chipStyle);
            chip.setText(s.label);
            chip.setCheckable(true);
            chip.setChecked(s == selected[0]);
            chip.setCheckedIconVisible(true);
            chip.setChipBackgroundColor(new ColorStateList(
                    new int[][]{{android.R.attr.state_checked}, {}},
                    new int[]{Ui.withAlpha(color, 0.18f), 0x00000000}));
            chip.setTextColor(new ColorStateList(
                    new int[][]{{android.R.attr.state_checked}, {}},
                    new int[]{color, ContextCompat.getColor(ctx, R.color.text_primary)}));
            chip.setCheckedIconTint(ColorStateList.valueOf(color));
            chip.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
            chip.setOnClickListener(x -> {
                selected[0] = s;
                for (int i = 0; i < group.getChildCount(); i++) {
                    ((Chip) group.getChildAt(i)).setChecked(group.getChildAt(i) == chip);
                }
            });
            group.addView(chip);
        }

        View manual = v.findViewById(R.id.manualLocation);
        manual.setVisibility(hasLocation ? View.GONE : View.VISIBLE);
        TextInputEditText lat = v.findViewById(R.id.latField);
        TextInputEditText lng = v.findViewById(R.id.lngField);
        if (report.latitude != null) lat.setText(String.valueOf(report.latitude));
        if (report.longitude != null) lng.setText(String.valueOf(report.longitude));

        AlertDialog dialog = new MaterialAlertDialogBuilder(ctx)
                .setTitle("Approve report")
                .setView(v)
                .create();
        v.findViewById(R.id.cancel).setOnClickListener(x -> dialog.dismiss());
        v.findViewById(R.id.publish).setOnClickListener(x -> {
            Double la = hasLocation ? report.latitude : parse(lat);
            Double lo = hasLocation ? report.longitude : parse(lng);
            if (la == null || lo == null) {
                Toast.makeText(ctx, "Enter valid latitude and longitude.", Toast.LENGTH_SHORT).show();
                return;
            }
            dialog.dismiss();
            onDecision.accept(new Decision(selected[0], la, lo));
        });
        dialog.show();
    }

    @Nullable
    private static Double parse(TextInputEditText f) {
        try {
            return Double.parseDouble(String.valueOf(f.getText()).trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
