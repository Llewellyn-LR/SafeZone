package com.safezone.app.service;

import com.safezone.app.model.AppUser;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public abstract class AuthService {
    public interface Listener { void onChanged(); }

    public interface Callback {
        void onSuccess();
        void onError(String message);
    }

    private final List<Listener> listeners = new CopyOnWriteArrayList<>();

    public void addListener(Listener l) { listeners.add(l); }
    public void removeListener(Listener l) { listeners.remove(l); }
    protected void notifyListeners() { for (Listener l : listeners) l.onChanged(); }

    public abstract AppUser getCurrentUser();
    public boolean isLoggedIn() { return getCurrentUser() != null; }
    public abstract boolean isLoading();

    public abstract void login(String email, String password, Callback cb);
    public abstract void register(String name, String email, String password, Callback cb);
    public abstract void continueAsGuest(Callback cb);
    public abstract void logout();
}
