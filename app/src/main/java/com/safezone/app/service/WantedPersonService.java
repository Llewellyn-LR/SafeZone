package com.safezone.app.service;

import com.safezone.app.model.WantedPerson;

import java.util.List;

public interface WantedPersonService {
    Subscription watchWantedPersons(DataListener<List<WantedPerson>> listener);
}
