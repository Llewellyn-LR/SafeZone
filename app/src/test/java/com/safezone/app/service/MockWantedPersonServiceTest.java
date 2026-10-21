package com.safezone.app.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.safezone.app.model.WantedPerson;

import org.junit.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MockWantedPersonServiceTest {

    @Test
    public void emitsANonEmptyList() {
        assertFalse(new MockWantedPersonService().getWantedPersons().isEmpty());
    }

    @Test
    public void emitsEntriesWithUniqueIds() {
        List<WantedPerson> people = new MockWantedPersonService().getWantedPersons();
        Set<String> ids = new HashSet<>();
        for (WantedPerson p : people) ids.add(p.id);
        assertEquals(people.size(), ids.size());
    }

    @Test
    public void everyMockEntryIsClearlyASample() {
        for (WantedPerson p : new MockWantedPersonService().getWantedPersons()) {
            assertTrue(p.name.startsWith("SAMPLE"));
            assertTrue(p.wantedFor.contains("not a real SAPS notice"));
            assertNull(p.caseReference);
        }
    }
}
