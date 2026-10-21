package com.safezone.app.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.util.Date;

public class CrimeReportTest {
    @Test
    public void categoryRoundTripsEveryWireValue() {
        for (ReportCategory c : ReportCategory.values()) {
            assertEquals(c, ReportCategory.fromWireValue(c.wireValue));
        }
    }

    @Test
    public void categoryFallsBackToOther() {
        assertEquals(ReportCategory.OTHER, ReportCategory.fromWireValue("nonsense"));
        assertEquals(ReportCategory.OTHER, ReportCategory.fromWireValue(null));
    }

    @Test
    public void statusRoundTripsEveryWireValue() {
        for (ReportStatus s : ReportStatus.values()) {
            assertEquals(s, ReportStatus.fromWireValue(s.wireValue));
        }
    }

    @Test
    public void statusFallsBackToPendingReview() {
        assertEquals(ReportStatus.PENDING_REVIEW, ReportStatus.fromWireValue("nonsense"));
        assertEquals(ReportStatus.PENDING_REVIEW, ReportStatus.fromWireValue(null));
    }

    @Test
    public void wireValuesMatchTheDartEnumNamesSoExistingFirestoreDataStillParses() {
        assertEquals("vehicleCrime", ReportCategory.VEHICLE_CRIME.wireValue);
        assertEquals("suspiciousActivity", ReportCategory.SUSPICIOUS_ACTIVITY.wireValue);
        assertEquals("pendingReview", ReportStatus.PENDING_REVIEW.wireValue);
    }

    @Test
    public void newReportAlwaysStartsPendingReviewByDefault() {
        CrimeReport r = new CrimeReport("r1", ReportCategory.ROBBERY, "Test Area", "Test",
                false, new Date());
        assertEquals(ReportStatus.PENDING_REVIEW, r.status);
    }

    @Test
    public void copyWithStatusOnlyChangesStatus() {
        Date submittedAt = new Date(1767268800000L);
        CrimeReport original = new CrimeReport("r1", ReportCategory.ASSAULT, "Test Area",
                "Test description", true, submittedAt, ReportStatus.PENDING_REVIEW,
                "uid-123", -33.9, 25.6);

        CrimeReport published = original.copyWith(ReportStatus.PUBLISHED);

        assertEquals(ReportStatus.PUBLISHED, published.status);
        assertEquals(original.id, published.id);
        assertEquals(original.category, published.category);
        assertEquals(original.area, published.area);
        assertEquals(original.description, published.description);
        assertEquals(original.hasPhoto, published.hasPhoto);
        assertEquals(original.submittedAt, published.submittedAt);
        assertEquals(original.reporterUid, published.reporterUid);
        assertEquals(original.latitude, published.latitude);
        assertEquals(original.longitude, published.longitude);
    }

    @Test
    public void latitudeLongitudeDefaultToNullWhenNotCaptured() {
        CrimeReport r = new CrimeReport("r2", ReportCategory.OTHER, "Test Area", "",
                false, new Date());
        assertNull(r.latitude);
        assertNull(r.longitude);
    }

    @Test
    public void photoUrlIsKeptAndSurvivesCopyWith() {
        CrimeReport r = new CrimeReport("r3", ReportCategory.VANDALISM, "Area", "desc", true,
                new Date(), ReportStatus.PENDING_REVIEW, "uid", null, null, "https://example.test/p.jpg");
        assertEquals("https://example.test/p.jpg", r.photoUrl);
        assertEquals("https://example.test/p.jpg", r.copyWith(ReportStatus.PUBLISHED).photoUrl);
    }

    @Test
    public void photoUrlDefaultsToNull() {
        assertNull(new CrimeReport("r4", ReportCategory.OTHER, "A", "", false, new Date()).photoUrl);
    }

    private static CrimeReport report(ReportCategory c, ReportStatus st) {
        return new CrimeReport("r", c, "Area", "desc", false, new Date(), st, "uid", null, null);
    }

    @Test
    public void gbvLifecycle_freshReportCanBeSolvedOrDismissed() {
        CrimeReport r = report(ReportCategory.GBV, ReportStatus.PENDING_REVIEW);
        assertTrue(r.canMarkSolved());
        assertTrue(r.canDismiss());
        assertFalse(r.awaitingReporterConfirmation());
    }

    @Test
    public void gbvLifecycle_solvedWaitsForTheReporter() {
        CrimeReport r = report(ReportCategory.GBV, ReportStatus.AWAITING_CONFIRMATION);
        assertTrue(r.awaitingReporterConfirmation());
        assertFalse(r.canMarkSolved());
        assertFalse(r.canDismiss());
    }

    @Test
    public void gbvLifecycle_reopenedCaseOffersSolvedOnly() {
        CrimeReport r = report(ReportCategory.GBV, ReportStatus.FOLLOW_UP);
        assertTrue(r.canMarkSolved());
        assertFalse(r.canDismiss());
    }

    @Test
    public void gbvLifecycle_finalStatesOfferNothing() {
        for (ReportStatus st : new ReportStatus[]{ReportStatus.SOLVED, ReportStatus.REJECTED}) {
            CrimeReport r = report(ReportCategory.GBV, st);
            assertFalse(r.canMarkSolved());
            assertFalse(r.canDismiss());
            assertFalse(r.awaitingReporterConfirmation());
        }
    }

    @Test
    public void otherCategoriesNeverUseTheGbvWorkflow() {
        CrimeReport r = report(ReportCategory.ROBBERY, ReportStatus.PENDING_REVIEW);
        assertFalse(r.canMarkSolved());
        assertFalse(r.canDismiss());
    }

    @Test
    public void gbvDismissalReadsRejectedToTheReporter() {
        assertEquals("Rejected", report(ReportCategory.GBV, ReportStatus.REJECTED).displayStatusLabel());
        assertEquals(ReportStatus.REJECTED.label,
                report(ReportCategory.ROBBERY, ReportStatus.REJECTED).displayStatusLabel());
    }

    @Test
    public void copyWithKeepsTheDismissalDetails() {
        CrimeReport r = new CrimeReport("r", ReportCategory.GBV, "A", "d", false, new Date(),
                ReportStatus.REJECTED, "uid", null, null, null, "Duplicate report", "https://p/1.jpg");
        CrimeReport copy = r.copyWith(ReportStatus.REJECTED);
        assertEquals("Duplicate report", copy.rejectionReason);
        assertEquals("https://p/1.jpg", copy.proofUrl);
    }
}
