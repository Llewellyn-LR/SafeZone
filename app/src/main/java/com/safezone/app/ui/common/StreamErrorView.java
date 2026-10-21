package com.safezone.app.ui.common;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.safezone.app.R;

public class StreamErrorView extends LinearLayout {
    private final TextView text;

    public StreamErrorView(Context c) { this(c, null); }

    public StreamErrorView(Context c, @Nullable AttributeSet a) {
        super(c, a);
        setOrientation(VERTICAL);
        setGravity(android.view.Gravity.CENTER);
        int pad = Ui.dp(c, 24);
        setPadding(pad, pad, pad, pad);
        LayoutInflater.from(c).inflate(R.layout.view_stream_error, this, true);
        text = findViewById(R.id.errorText);
        text.setText(DEFAULT_MESSAGE);
    }

    public static final String DEFAULT_MESSAGE =
            "Couldn't load live data. Check your Firebase setup "
                    + "(see FIREBASE_SETUP.md) or your connection.";

    public void setMessage(CharSequence message) { text.setText(message); }
}
