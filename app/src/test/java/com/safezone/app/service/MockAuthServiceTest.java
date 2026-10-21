package com.safezone.app.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class MockAuthServiceTest {

    private static final class Result implements AuthService.Callback {
        boolean ok;
        String error;
        @Override public void onSuccess() { ok = true; }
        @Override public void onError(String message) { error = message; }
    }

    private MockAuthService service() { return new MockAuthService(Scheduler.IMMEDIATE); }

    @Test
    public void startsLoggedOut() {
        MockAuthService s = service();
        assertNull(s.getCurrentUser());
        assertFalse(s.isLoggedIn());
    }

    @Test
    public void loginWithValidCredentialsSetsCurrentUser() {
        MockAuthService s = service();
        Result r = new Result();
        s.login("resident@example.com", "password1", r);
        assertTrue(r.ok);
        assertTrue(s.isLoggedIn());
        assertEquals("resident@example.com", s.getCurrentUser().email);
        assertFalse(s.getCurrentUser().isGuest);
    }

    @Test
    public void loginWithAnInvalidPasswordReportsAnError() {
        Result r = new Result();
        service().login("resident@example.com", "short", r);
        assertNotNull(r.error);
        assertFalse(r.ok);
    }

    @Test
    public void registerWithValidFieldsSetsCurrentUserWithTheGivenName() {
        MockAuthService s = service();
        s.register("Thandiwe", "thandiwe@example.com", "password1", new Result());
        assertEquals("Thandiwe", s.getCurrentUser().name);
    }

    @Test
    public void registerWithAnEmptyNameReportsAnError() {
        Result r = new Result();
        service().register("", "a@example.com", "password1", r);
        assertEquals("Please enter your name.", r.error);
    }

    @Test
    public void continueAsGuestSetsAnIsGuestUser() {
        MockAuthService s = service();
        s.continueAsGuest(new Result());
        assertTrue(s.getCurrentUser().isGuest);
    }

    @Test
    public void logoutClearsCurrentUser() {
        MockAuthService s = service();
        s.continueAsGuest(new Result());
        s.logout();
        assertNull(s.getCurrentUser());
        assertFalse(s.isLoggedIn());
    }
}
