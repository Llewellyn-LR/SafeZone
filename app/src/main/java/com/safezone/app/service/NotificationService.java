package com.safezone.app.service;

import com.google.firebase.messaging.FirebaseMessaging;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class NotificationService {
    public interface Listener { void onChanged(); }

    private static final String AREA_TOPIC = "nmb_alerts";

    private final boolean firebaseAvailable;
    private final List<Listener> listeners = new CopyOnWriteArrayList<>();
    private boolean enabled = true;

    public NotificationService(boolean firebaseAvailable) {
        this.firebaseAvailable = firebaseAvailable;
    }

    public void addListener(Listener l) { listeners.add(l); }
    public void removeListener(Listener l) { listeners.remove(l); }
    private void notifyListeners() { for (Listener l : listeners) l.onChanged(); }

    public boolean isEnabled() { return enabled; }

    public void init(boolean permissionGranted) {
        if (!firebaseAvailable) return;
        enabled = permissionGranted;
        if (enabled) FirebaseMessaging.getInstance().subscribeToTopic(AREA_TOPIC);
        notifyListeners();
    }

    public void setEnabled(boolean value) {
        enabled = value;
        notifyListeners();
        if (!firebaseAvailable) return;
        if (value) FirebaseMessaging.getInstance().subscribeToTopic(AREA_TOPIC);
        else FirebaseMessaging.getInstance().unsubscribeFromTopic(AREA_TOPIC);
    }
}
