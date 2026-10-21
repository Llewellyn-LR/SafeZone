package com.safezone.app.model;

import androidx.annotation.ColorRes;

import com.safezone.app.R;

public enum HotspotSeverity {
    HIGH(R.color.coral, R.color.coral_text, "High risk", "high"),
    MEDIUM(R.color.amber, R.color.amber_text, "Medium risk", "medium"),
    LOW(R.color.green, R.color.green_text, "Low risk", "low");

    @ColorRes public final int colorRes;

    @ColorRes public final int textColorRes;
    public final String label;

    public final String wireValue;

    HotspotSeverity(@ColorRes int colorRes, @ColorRes int textColorRes, String label, String wireValue) {
        this.colorRes = colorRes;
        this.textColorRes = textColorRes;
        this.label = label;
        this.wireValue = wireValue;
    }

    public static HotspotSeverity fromWireValue(String value) {
        for (HotspotSeverity s : values()) if (s.wireValue.equals(value)) return s;
        return MEDIUM;
    }
}
