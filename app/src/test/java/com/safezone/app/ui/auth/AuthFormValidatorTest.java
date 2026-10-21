package com.safezone.app.ui.auth;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

public class AuthFormValidatorTest {
    @Test
    public void mismatchedPasswordsAreRejected() {
        assertEquals("Passwords do not match.", AuthFormValidator.validateRegistration("secret1", "secret2"));
    }

    @Test
    public void matchingPasswordsPass() {
        assertNull(AuthFormValidator.validateRegistration("secret1", "secret1"));
    }
}
