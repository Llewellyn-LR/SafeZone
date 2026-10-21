package com.safezone.app.ui.report;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import com.safezone.app.model.ReportCategory;

import org.junit.Test;

public class ReportValidatorTest {
    @Test
    public void requiresACategoryFirst() {
        assertEquals("Please choose a category.", ReportValidator.validate(null, "Area", "long enough text"));
    }

    @Test
    public void requiresAnArea() {
        assertEquals("Please enter an area or landmark.",
                ReportValidator.validate(ReportCategory.ROBBERY, "   ", "long enough text"));
    }

    @Test
    public void requiresTenCharactersOfDescriptionAfterTrimming() {
        assertEquals("Please describe what happened (10+ characters).",
                ReportValidator.validate(ReportCategory.ROBBERY, "Area", "  short   "));
        assertNull(ReportValidator.validate(ReportCategory.ROBBERY, "Area", "exactly 10"));
    }

    @Test
    public void acceptsAValidReport() {
        assertNull(ReportValidator.validate(ReportCategory.ASSAULT, "North End", "Saw something happen"));
    }
}
