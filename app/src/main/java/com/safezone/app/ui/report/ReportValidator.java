package com.safezone.app.ui.report;

import com.safezone.app.model.ReportCategory;

final class ReportValidator {
    private ReportValidator() {}

    static String validate(ReportCategory category, String area, String description) {
        if (category == null) return "Please choose a category.";
        if (area.trim().isEmpty()) return "Please enter an area or landmark.";
        if (description.trim().length() < 10) return "Please describe what happened (10+ characters).";
        return null;
    }
}
