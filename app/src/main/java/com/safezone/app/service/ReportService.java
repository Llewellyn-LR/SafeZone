package com.safezone.app.service;

import android.net.Uri;

import com.safezone.app.model.CrimeReport;
import com.safezone.app.model.ReportCategory;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public abstract class ReportService {
    public interface Listener { void onChanged(); }

    public interface SubmitCallback {
        void onSuccess(CrimeReport report);
        void onError(Exception e);
    }

    public interface ActionCallback {
        void onSuccess();
        void onError(Exception e);
    }

    private final List<Listener> listeners = new CopyOnWriteArrayList<>();
    public void addListener(Listener l) { listeners.add(l); }
    public void removeListener(Listener l) { listeners.remove(l); }
    protected void notifyListeners() { for (Listener l : listeners) l.onChanged(); }

    public abstract List<CrimeReport> getMyReports();

    public abstract void submitReport(ReportCategory category, String area, String description,
                                      Uri photo, Double latitude, Double longitude,
                                      SubmitCallback cb);

    public abstract void respondToSolved(CrimeReport report, boolean solved, ActionCallback cb);

    public void dispose() { }
}
