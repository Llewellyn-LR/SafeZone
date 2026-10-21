package com.safezone.app.service;

import android.net.Uri;

import com.safezone.app.model.CrimeReport;
import com.safezone.app.model.ReportCategory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class MockReportService extends ReportService {
    private final Scheduler scheduler;
    private final List<CrimeReport> myReports = new ArrayList<>();

    public MockReportService() { this(Scheduler.MAIN); }
    public MockReportService(Scheduler scheduler) { this.scheduler = scheduler; }

    @Override
    public List<CrimeReport> getMyReports() {
        List<CrimeReport> copy = new ArrayList<>(myReports);
        Collections.reverse(copy);
        return Collections.unmodifiableList(copy);
    }

    @Override
    public void respondToSolved(CrimeReport report, boolean solved, ActionCallback cb) {
        if (!report.awaitingReporterConfirmation()) {
            cb.onError(new IllegalStateException("This report isn't waiting for your confirmation."));
            return;
        }
        for (int i = 0; i < myReports.size(); i++) {
            if (myReports.get(i).id.equals(report.id)) {
                myReports.set(i, report.copyWith(solved
                        ? com.safezone.app.model.ReportStatus.SOLVED
                        : com.safezone.app.model.ReportStatus.FOLLOW_UP));
            }
        }
        notifyListeners();
        cb.onSuccess();
    }

    @Override
    public void submitReport(ReportCategory category, String area, String description,
                             Uri photo, Double latitude, Double longitude,
                             SubmitCallback cb) {
        scheduler.postDelayed(() -> {
            CrimeReport report = new CrimeReport(
                    "r" + System.nanoTime(), category, area, description, photo != null, new Date(),
                    com.safezone.app.model.ReportStatus.PENDING_REVIEW, null, latitude, longitude, null);
            myReports.add(report);
            notifyListeners();
            cb.onSuccess(report);
        }, 900);
    }
}
