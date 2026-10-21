package com.safezone.app.service;

import com.safezone.app.model.CrimeHotspot;

import java.util.List;

public interface HotspotService {
    Subscription watchHotspots(DataListener<List<CrimeHotspot>> listener);
}
