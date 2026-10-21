package com.safezone.app.ui.help;

import android.os.Bundle;
import android.view.ViewGroup;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.safezone.app.R;
import com.safezone.app.ui.common.Sections;

public class PrivacyActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_text_screen);
        MaterialToolbar tb = findViewById(R.id.toolbar);
        tb.setTitle(R.string.privacy_title);
        tb.setNavigationOnClickListener(v -> finish());

        ViewGroup c = findViewById(R.id.content);
        Sections.addSection(c, R.string.privacy_collect_title, R.string.privacy_collect_body);
        Sections.addSection(c, R.string.privacy_shown_title, R.string.privacy_shown_body);
        Sections.addSection(c, R.string.privacy_mods_title, R.string.privacy_mods_body);
        Sections.addSection(c, R.string.privacy_wanted_title, R.string.privacy_wanted_body);
    }
}
