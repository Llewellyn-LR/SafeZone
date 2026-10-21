package com.safezone.app.ui.common;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.StringRes;

import com.safezone.app.R;
import com.safezone.app.util.EmergencyNumbers;

public final class Sections {
    private Sections() {}

    public static void addSection(ViewGroup parent, @StringRes int title, @StringRes int body) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_section, parent, false);
        ((TextView) v.findViewById(R.id.sectionTitle)).setText(title);
        ((TextView) v.findViewById(R.id.sectionBody)).setText(body);
        parent.addView(v);
    }

    public static void addDialRow(ViewGroup parent, @StringRes int label, EmergencyNumbers.Entry number) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_dial_row, parent, false);
        ((TextView) v.findViewById(R.id.dialLabel)).setText(label);
        ((TextView) v.findViewById(R.id.dialNumber)).setText(number.display);
        v.setContentDescription(parent.getContext().getString(label) + ", " + number.display);
        v.setOnClickListener(x -> Dialer.dial(x, number.dial, number.display));
        parent.addView(v);
    }
}
