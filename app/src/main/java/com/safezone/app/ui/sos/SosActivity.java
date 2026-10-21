package com.safezone.app.ui.sos;

import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.card.MaterialCardView;
import com.safezone.app.R;
import com.safezone.app.ui.common.Dialer;
import com.safezone.app.ui.common.Sections;
import com.safezone.app.util.EmergencyNumbers;

public class SosActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_text_screen);
        MaterialToolbar tb = findViewById(R.id.toolbar);
        tb.setTitle(R.string.sos_title);
        tb.setNavigationOnClickListener(v -> finish());

        ViewGroup c = findViewById(R.id.content);

        MaterialCardView warning = (MaterialCardView) getLayoutInflater()
                .inflate(R.layout.item_section, c, false);
        warning.setStrokeColor(ContextCompat.getColor(this, R.color.coral));
        warning.setStrokeWidth(Math.round(2 * getResources().getDisplayMetrics().density));
        ((TextView) warning.findViewById(R.id.sectionTitle)).setVisibility(android.view.View.GONE);
        ((TextView) warning.findViewById(R.id.sectionBody)).setText(R.string.sos_disclaimer);
        ((TextView) warning.findViewById(R.id.sectionBody)).setTextSize(15);
        ((TextView) warning.findViewById(R.id.sectionBody)).setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
        c.addView(warning);

        Sections.addDialRow(c, R.string.number_saps_emergency, EmergencyNumbers.SAPS_EMERGENCY);

        Sections.addSection(spaced(c), R.string.sos_gbv_title, R.string.sos_gbv_body);
        Sections.addDialRow(c, R.string.number_gbv, EmergencyNumbers.GBV_COMMAND_CENTRE);
        Sections.addDialRow(c, R.string.number_gbv_call_back, EmergencyNumbers.GBV_CALL_ME_BACK);

        Sections.addSection(spaced(c), R.string.sos_tip_title, R.string.number_crime_stop);
        Sections.addDialRow(c, R.string.number_crime_stop, EmergencyNumbers.CRIME_STOP);

        com.google.android.material.button.MaterialButton exit = new com.google.android.material.button.MaterialButton(
                this, null, com.google.android.material.R.attr.materialButtonOutlinedStyle);
        exit.setText(R.string.sos_quick_exit);
        exit.setIconResource(R.drawable.ic_exit);
        exit.setContentDescription(getString(R.string.sos_quick_exit) + ". "
                + getString(R.string.sos_quick_exit_hint));
        ViewGroup.MarginLayoutParams lp = new ViewGroup.MarginLayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.topMargin = Math.round(24 * getResources().getDisplayMetrics().density);
        exit.setLayoutParams(lp);
        exit.setOnClickListener(v -> finishAndRemoveTask());
        c.addView(exit);
    }

    private ViewGroup spaced(ViewGroup c) {
        c.addView(new android.view.View(this), new ViewGroup.LayoutParams(1,
                Math.round(8 * getResources().getDisplayMetrics().density)));
        return c;
    }
}
