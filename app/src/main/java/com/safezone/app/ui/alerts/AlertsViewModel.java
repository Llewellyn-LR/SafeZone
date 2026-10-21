package com.safezone.app.ui.alerts;

import androidx.lifecycle.ViewModel;

import com.safezone.app.model.HotspotSeverity;

public class AlertsViewModel extends ViewModel {
    private HotspotSeverity filter;

    public HotspotSeverity getFilter() { return filter; }
    public void setFilter(HotspotSeverity filter) { this.filter = filter; }
}
