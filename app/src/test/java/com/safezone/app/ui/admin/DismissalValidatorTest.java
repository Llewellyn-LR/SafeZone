package com.safezone.app.ui.admin;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.Test;

public class DismissalValidatorTest {
    @Test public void blankReasonIsRejected() {
        assertNotNull(DismissalValidator.validateReason(null));
        assertNotNull(DismissalValidator.validateReason(""));
        assertNotNull(DismissalValidator.validateReason("     "));
    }

    @Test public void tooShortReasonIsRejected() {
        assertNotNull(DismissalValidator.validateReason("no"));
    }

    @Test public void realReasonIsAccepted() {
        assertNull(DismissalValidator.validateReason("Duplicate of an earlier report."));
    }
}
