package com.safezone.app.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import android.net.Uri;

import com.safezone.app.model.CrimeReport;
import com.safezone.app.model.ReportCategory;
import com.safezone.app.model.ReportStatus;

import org.junit.Test;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class MockReportServiceTest {
    private static CrimeReport submit(MockReportService s, ReportCategory c, String area,
                                      String desc, boolean photo) {
        AtomicReference<CrimeReport> out = new AtomicReference<>();

        Uri uri = null;
        s.submitReport(c, area, desc, uri, null, null, new ReportService.SubmitCallback() {
            @Override public void onSuccess(CrimeReport r) { out.set(r); }
            @Override public void onError(Exception e) { throw new AssertionError(e); }
        });
        return out.get();
    }

    private MockReportService service() { return new MockReportService(Scheduler.IMMEDIATE); }

    @Test
    public void myReportsStartsEmpty() {
        assertTrue(service().getMyReports().isEmpty());
    }

    @Test
    public void submitReportAddsTheReportToMyReports() {
        MockReportService s = service();
        CrimeReport submitted = submit(s, ReportCategory.ROBBERY, "Test Area", "A test report.", false);
        assertEquals(1, s.getMyReports().size());
        assertEquals(submitted.id, s.getMyReports().get(0).id);
        assertEquals(ReportStatus.PENDING_REVIEW, s.getMyReports().get(0).status);
    }

    @Test
    public void myReportsListsMostRecentSubmissionFirst() {
        MockReportService s = service();
        CrimeReport first = submit(s, ReportCategory.VANDALISM, "Area A", "First report", false);
        CrimeReport second = submit(s, ReportCategory.ASSAULT, "Area B", "Second report", true);
        assertEquals(second.id, s.getMyReports().get(0).id);
        assertEquals(first.id, s.getMyReports().get(1).id);
    }

    @Test
    public void submitReportNotifiesListeners() {
        MockReportService s = service();
        AtomicBoolean notified = new AtomicBoolean(false);
        s.addListener(() -> notified.set(true));
        submit(s, ReportCategory.OTHER, "Area", "", false);
        assertTrue(notified.get());
    }
}
