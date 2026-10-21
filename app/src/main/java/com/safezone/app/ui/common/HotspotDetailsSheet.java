package com.safezone.app.ui.common;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.safezone.app.R;
import com.safezone.app.model.CrimeHotspot;

public final class HotspotDetailsSheet {
    private HotspotDetailsSheet() {}

    public static void show(Context ctx, CrimeHotspot h) {
        BottomSheetDialog dialog = new BottomSheetDialog(ctx);
        View v = LayoutInflater.from(ctx).inflate(R.layout.sheet_hotspot_details, null);

        int severityColor = ContextCompat.getColor(ctx, h.severity.colorRes);
        int secondary = ContextCompat.getColor(ctx, R.color.text_secondary);
        int amber = ContextCompat.getColor(ctx, R.color.amber);

        v.setBackground(topRounded(ctx, ContextCompat.getColor(ctx, R.color.surface), 24));
        v.findViewById(R.id.handle).setBackground(
                Ui.rounded(ctx, ContextCompat.getColor(ctx, R.color.outline_grey), 2));
        v.findViewById(R.id.dot).setBackground(Ui.circle(severityColor));

        TextView sev = v.findViewById(R.id.severity);
        sev.setText(h.severity.label);
        sev.setTextColor(ContextCompat.getColor(ctx, h.severity.textColorRes));

        View pill = v.findViewById(R.id.unverifiedPill);
        pill.setBackground(Ui.rounded(ctx, Ui.withAlpha(secondary, 0.1f), 20));
        pill.setVisibility(h.verified ? View.GONE : View.VISIBLE);

        ((TextView) v.findViewById(R.id.title)).setText(h.title);
        ((TextView) v.findViewById(R.id.meta)).setText(h.area + " · " + h.timeAgo);
        ((TextView) v.findViewById(R.id.description)).setText(h.description);

        View warning = v.findViewById(R.id.unverifiedWarning);
        warning.setBackground(Ui.rounded(ctx, Ui.withAlpha(amber, 0.08f), 10));
        warning.setVisibility(h.verified ? View.GONE : View.VISIBLE);

        dialog.setContentView(v);

        FrameLayout sheet = dialog.findViewById(com.google.android.material.R.id.design_bottom_sheet);
        if (sheet != null) sheet.setBackgroundResource(android.R.color.transparent);
        dialog.show();
    }

    private static android.graphics.drawable.GradientDrawable topRounded(Context c, int color, float dp) {
        android.graphics.drawable.GradientDrawable d = new android.graphics.drawable.GradientDrawable();
        d.setColor(color);
        float r = Ui.dp(c, dp);
        d.setCornerRadii(new float[]{r, r, r, r, 0, 0, 0, 0});
        return d;
    }
}
