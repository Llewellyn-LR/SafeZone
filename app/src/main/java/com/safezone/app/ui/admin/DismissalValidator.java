package com.safezone.app.ui.admin;

public final class DismissalValidator {
    public static final int MIN_REASON_LENGTH = 10;

    private DismissalValidator() {}

    public static String validateReason(String reason) {
        String r = reason == null ? "" : reason.trim();
        if (r.isEmpty()) return "A reason is required to dismiss a report.";
        if (r.length() < MIN_REASON_LENGTH) {
            return "Please give a bit more detail (at least " + MIN_REASON_LENGTH + " characters).";
        }
        return null;
    }
}
