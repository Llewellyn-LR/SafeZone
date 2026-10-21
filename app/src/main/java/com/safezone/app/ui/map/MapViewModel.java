package com.safezone.app.ui.map;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.safezone.app.util.LatLng;

public class MapViewModel extends ViewModel {
    private final MutableLiveData<LatLng> location = new MutableLiveData<>();
    private boolean locationRequested;

    public LiveData<LatLng> location() { return location; }
    public void postLocation(LatLng l) { location.postValue(l); }

    public boolean markLocationRequested() {
        if (locationRequested) return false;
        locationRequested = true;
        return true;
    }
}
