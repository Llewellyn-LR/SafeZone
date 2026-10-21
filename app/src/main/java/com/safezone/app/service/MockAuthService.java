package com.safezone.app.service;

import com.safezone.app.model.AppUser;

public class MockAuthService extends AuthService {
    private final Scheduler scheduler;
    private AppUser currentUser;
    private boolean loading;

    public MockAuthService() { this(Scheduler.MAIN); }
    public MockAuthService(Scheduler scheduler) { this.scheduler = scheduler; }

    @Override public AppUser getCurrentUser() { return currentUser; }
    @Override public boolean isLoading() { return loading; }

    @Override
    public void login(String email, String password, Callback cb) {
        setLoading(true);
        scheduler.postDelayed(() -> {
            if (!email.contains("@") || password.length() < 6) {
                setLoading(false);
                cb.onError("Enter a valid email and a password of at least 6 characters.");
                return;
            }
            currentUser = new AppUser(null, email.split("@")[0], email, false);
            setLoading(false);
            notifyListeners();
            cb.onSuccess();
        }, 900);
    }

    @Override
    public void register(String name, String email, String password, Callback cb) {
        setLoading(true);
        scheduler.postDelayed(() -> {
            String error = null;
            if (name.trim().isEmpty()) error = "Please enter your name.";
            else if (!email.contains("@")) error = "Enter a valid email address.";
            else if (password.length() < 6) error = "Password must be at least 6 characters.";
            if (error != null) {
                setLoading(false);
                cb.onError(error);
                return;
            }
            currentUser = new AppUser(null, name.trim(), email.trim(), false);
            setLoading(false);
            notifyListeners();
            cb.onSuccess();
        }, 900);
    }

    @Override
    public void continueAsGuest(Callback cb) {
        setLoading(true);
        scheduler.postDelayed(() -> {
            currentUser = AppUser.GUEST;
            setLoading(false);
            notifyListeners();
            cb.onSuccess();
        }, 400);
    }

    @Override
    public void logout() {
        setLoading(true);
        scheduler.postDelayed(() -> {
            currentUser = null;
            setLoading(false);
            notifyListeners();
        }, 300);
    }

    private void setLoading(boolean v) {
        loading = v;
        notifyListeners();
    }
}
