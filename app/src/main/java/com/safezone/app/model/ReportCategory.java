package com.safezone.app.model;

import androidx.annotation.DrawableRes;

import com.safezone.app.R;

public enum ReportCategory {
    ROBBERY("robbery", "Robbery / Theft", R.drawable.ic_money_off),
    ASSAULT("assault", "Assault", R.drawable.ic_personal_injury),
    VEHICLE_CRIME("vehicleCrime", "Vehicle crime", R.drawable.ic_directions_car),
    SUSPICIOUS_ACTIVITY("suspiciousActivity", "Suspicious activity", R.drawable.ic_visibility),
    GBV("gbv", "GBV / Domestic violence", R.drawable.ic_shield),
    VANDALISM("vandalism", "Vandalism", R.drawable.ic_broken_image),
    OTHER("other", "Other", R.drawable.ic_more_horiz);

    public final String wireValue;
    public final String label;
    @DrawableRes public final int iconRes;

    ReportCategory(String wireValue, String label, @DrawableRes int iconRes) {
        this.wireValue = wireValue;
        this.label = label;
        this.iconRes = iconRes;
    }

    public boolean isPublishable() { return this != GBV; }

    public static ReportCategory fromWireValue(String value) {
        for (ReportCategory c : values()) if (c.wireValue.equals(value)) return c;
        return OTHER;
    }
}
