package com.safezone.app.ui.common;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

public class EventTest {
    @Test
    public void contentIsDeliveredExactlyOnce() {
        Event<String> e = new Event<>("go");
        assertEquals("go", e.consume());
        assertNull(e.consume());
    }
}
