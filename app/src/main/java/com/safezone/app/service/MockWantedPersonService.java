package com.safezone.app.service;

import com.safezone.app.model.WantedPerson;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class MockWantedPersonService implements WantedPersonService {

    private final Scheduler scheduler;

    public MockWantedPersonService() { this(Scheduler.MAIN); }
    public MockWantedPersonService(Scheduler scheduler) { this.scheduler = scheduler; }

    private static final long DAY_MS = 24L * 60 * 60 * 1000;

    private static Date daysAgo(int days) {
        return new Date(System.currentTimeMillis() - days * DAY_MS);
    }

    public List<WantedPerson> getWantedPersons() {
        final String note = "Sample entry for demonstration only. This is not a real SAPS notice.";
        List<WantedPerson> l = new ArrayList<>();
        l.add(new WantedPerson.Builder().id("sample1").name("SAMPLE — Unknown Male")
                .wantedFor(note).lastSeenArea("Demo area")
                .armedAndDangerous(true).publishedAt(daysAgo(2)).build());
        l.add(new WantedPerson.Builder().id("sample2").name("SAMPLE — Unknown Male")
                .wantedFor(note).lastSeenArea("Demo area")
                .armedAndDangerous(false).publishedAt(daysAgo(6)).build());
        l.add(new WantedPerson.Builder().id("sample3").name("SAMPLE — Unknown Female")
                .wantedFor(note).lastSeenArea("Demo area")
                .armedAndDangerous(false).publishedAt(daysAgo(11)).build());
        return Collections.unmodifiableList(l);
    }

    @Override
    public Subscription watchWantedPersons(DataListener<List<WantedPerson>> listener) {

        Scheduler.Cancelable c = scheduler.postDelayed(
                () -> listener.onData(getWantedPersons()), 400);
        return c::cancel;
    }
}
