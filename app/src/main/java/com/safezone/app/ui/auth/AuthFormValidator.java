package com.safezone.app.ui.auth;

final class AuthFormValidator {
    private AuthFormValidator() {}

    static String validateRegistration(String password, String confirm) {
        if (!password.equals(confirm)) return "Passwords do not match.";
        return null;
    }
}
