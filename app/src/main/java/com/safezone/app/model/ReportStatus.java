package com.safezone.app.model;

import androidx.annotation.ColorRes;

import com.safezone.app.R;

public enum ReportStatus {
    PENDING_REVIEW("pendingReview", "Pending review", R.color.amber, R.color.amber_text),
    PUBLISHED("published", "Published", R.color.green, R.color.green_text),
    REJECTED("rejected", "Not published", R.color.text_secondary, R.color.text_secondary),

    AWAITING_CONFIRMATION("awaitingConfirmation", "Confirm solved?", R.color.amber, R.color.amber_text),

    SOLVED("solved", "Solved", R.color.green, R.color.green_text),

    FOLLOW_UP("followUp", "Reopened", R.color.coral, R.color.coral_text);

    public final String wireValue;
    public final String label;
    @ColorRes public final int colorRes;
    @ColorRes public final int textColorRes;

    ReportStatus(String wireValue, String label, @ColorRes int colorRes, @ColorRes int textColorRes) {
        this.wireValue = wireValue;
        this.label = label;
        this.colorRes = colorRes;
        this.textColorRes = textColorRes;
    }

    public static ReportStatus fromWireValue(String value) {
        for (ReportStatus s : values()) if (s.wireValue.equals(value)) return s;
        return PENDING_REVIEW;
    }
}
