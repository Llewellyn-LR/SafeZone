package com.safezone.app.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.util.Date;
import java.util.Map;

public class WantedPersonTest {

    @Test
    public void storesAllConstructorFieldsAsGiven() {
        Date publishedAt = new Date(1709289000000L);
        WantedPerson p = new WantedPerson.Builder()
                .id("w1").name("Unknown Male").alias("'Skhokho'").wantedFor("Armed robbery")
                .caseReference("SAPS/KWK/2024/03112").lastSeenArea("KwaZakhele")
                .armedAndDangerous(true).photoUrl("https://example.com/photo.jpg")
                .publishedAt(publishedAt).build();

        assertEquals("w1", p.id);
        assertEquals("Unknown Male", p.name);
        assertEquals("'Skhokho'", p.alias);
        assertEquals("Armed robbery", p.wantedFor);
        assertEquals("SAPS/KWK/2024/03112", p.caseReference);
        assertEquals("KwaZakhele", p.lastSeenArea);
        assertTrue(p.armedAndDangerous);
        assertEquals("https://example.com/photo.jpg", p.photoUrl);
        assertEquals(publishedAt, p.publishedAt);
    }

    @Test
    public void armedAndDangerousDefaultsToFalse() {
        WantedPerson p = new WantedPerson.Builder()
                .id("w2").name("Unknown Female").wantedFor("Fraud")
                .lastSeenArea("Motherwell").publishedAt(new Date()).build();
        assertFalse(p.armedAndDangerous);
    }

    @Test
    public void aliasCaseReferenceAndPhotoUrlAreOptional() {
        WantedPerson p = new WantedPerson.Builder()
                .id("w3").name("Unknown Male").wantedFor("Theft")
                .lastSeenArea("New Brighton").publishedAt(new Date()).build();
        assertNull(p.alias);
        assertNull(p.caseReference);
        assertNull(p.photoUrl);
    }

    @Test
    public void displayNameAppendsAliasWhenPresent() {
        WantedPerson withAlias = new WantedPerson.Builder().id("a").name("Unknown Male")
                .alias("'Mancane'").wantedFor("x").lastSeenArea("y").publishedAt(new Date()).build();
        WantedPerson without = new WantedPerson.Builder().id("b").name("Unknown Female")
                .wantedFor("x").lastSeenArea("y").publishedAt(new Date()).build();
        assertEquals("Unknown Male ('Mancane')", withAlias.displayName());
        assertEquals("Unknown Female", without.displayName());
    }

    @Test(expected = IllegalStateException.class)
    public void builderRejectsMissingRequiredFields() {
        new WantedPerson.Builder().id("w").name("Unknown Male").build();
    }

    @Test
    public void toFirestoreMapIncludesAllFieldsExceptPublishedAtAsLiteral() {
        WantedPerson p = new WantedPerson.Builder()
                .id("w4").name("Unknown Male").alias("'Mancane'")
                .wantedFor("Theft of a motor vehicle").caseReference("SAPS/NBR/2024/00871")
                .lastSeenArea("New Brighton").armedAndDangerous(false)
                .publishedAt(new Date()).build();

        Map<String, Object> map = p.toFirestoreMap();

        assertEquals("Unknown Male", map.get("name"));
        assertEquals("'Mancane'", map.get("alias"));
        assertEquals("Theft of a motor vehicle", map.get("wantedFor"));
        assertEquals("SAPS/NBR/2024/00871", map.get("caseReference"));
        assertEquals("New Brighton", map.get("lastSeenArea"));
        assertEquals(false, map.get("armedAndDangerous"));

        assertTrue(map.containsKey("publishedAt"));
        assertNotNull(map.get("publishedAt"));
        assertFalse(map.get("publishedAt") instanceof Date);
    }
}
