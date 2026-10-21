package com.safezone.app.ui.help;

import android.os.Bundle;
import android.view.ViewGroup;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.safezone.app.R;
import com.safezone.app.ui.common.Sections;
import com.safezone.app.util.EmergencyNumbers;

public class HelpActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_text_screen);
        MaterialToolbar tb = findViewById(R.id.toolbar);
        tb.setTitle(R.string.help_title);
        tb.setNavigationOnClickListener(v -> finish());

        ViewGroup c = findViewById(R.id.content);
        Sections.addSection(c, R.string.help_report_title, R.string.help_report_body);
        Sections.addSection(c, R.string.help_pins_title, R.string.help_pins_body);
        Sections.addSection(c, R.string.help_anon_title, R.string.help_anon_body);
        Sections.addSection(c, R.string.help_mod_title, R.string.help_mod_body);
        Sections.addSection(c, R.string.help_emergency_title, R.string.help_emergency_note);
        Sections.addDialRow(c, R.string.number_saps_emergency, EmergencyNumbers.SAPS_EMERGENCY);
        Sections.addDialRow(c, R.string.number_crime_stop, EmergencyNumbers.CRIME_STOP);
        Sections.addDialRow(c, R.string.number_gbv, EmergencyNumbers.GBV_COMMAND_CENTRE);
    }
}
