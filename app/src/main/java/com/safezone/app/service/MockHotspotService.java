package com.safezone.app.service;

import com.safezone.app.model.CrimeHotspot;
import com.safezone.app.model.HotspotSeverity;
import com.safezone.app.model.ReportCategory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MockHotspotService implements HotspotService {
    private final Scheduler scheduler;

    public MockHotspotService() { this(Scheduler.MAIN); }
    public MockHotspotService(Scheduler scheduler) { this.scheduler = scheduler; }

    public List<CrimeHotspot> getHotspots() {
        List<CrimeHotspot> l = new ArrayList<>();
        l.add(new CrimeHotspot("h1", "Armed robbery reported", "KwaZakhele",
                -33.8890, 25.5610, HotspotSeverity.HIGH, "18 min ago",
                "Armed robbery reported near the main taxi rank. SAPS on scene. "
                        + "Avoid the area if possible.", true, ReportCategory.ROBBERY));
        l.add(new CrimeHotspot("h2", "Vehicle break-in cluster", "New Brighton",
                -33.9130, 25.5750, HotspotSeverity.HIGH, "2 hrs ago",
                "Multiple vehicle break-ins reported this week along the same "
                        + "street. Residents advise not leaving valuables visible.", true, ReportCategory.VEHICLE_CRIME));
        l.add(new CrimeHotspot("h3", "Suspicious activity", "Motherwell NU2",
                -33.8215, 25.5860, HotspotSeverity.MEDIUM, "1 hr ago",
                "Community member reported unfamiliar individuals loitering near "
                        + "the school. Unverified — pending a second report.", false, ReportCategory.SUSPICIOUS_ACTIVITY));
        l.add(new CrimeHotspot("h4", "Community patrol update", "Kwanobuhle",
                -33.8010, 25.4180, HotspotSeverity.LOW, "3 hrs ago",
                "CPF patrol completed with no incidents. Area currently calm.", true));
        l.add(new CrimeHotspot("h5", "Assault reported", "Motherwell NU10",
                -33.8390, 25.6120, HotspotSeverity.HIGH, "40 min ago",
                "Assault reported outside a local tavern. Victim assisted by "
                        + "SAPS Vulnerability Services.", true, ReportCategory.ASSAULT));
        l.add(new CrimeHotspot("h6", "Unattended package", "Gqeberha CBD",
                -33.9608, 25.6022, HotspotSeverity.MEDIUM, "25 min ago",
                "Unattended bag reported near Govan Mbeki Ave. Metro police "
                        + "notified. Unverified — single report so far.", false, ReportCategory.SUSPICIOUS_ACTIVITY));
        return Collections.unmodifiableList(l);
    }

    @Override
    public Subscription watchHotspots(DataListener<List<CrimeHotspot>> listener) {
        Scheduler.Cancelable c = scheduler.postDelayed(() -> listener.onData(CrimeHotspot.withoutExpired(CrimeHotspot.publicOnly(getHotspots()), System.currentTimeMillis())), 400);
        return c::cancel;
    }
}
