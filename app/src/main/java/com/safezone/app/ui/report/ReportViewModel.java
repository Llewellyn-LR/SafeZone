package com.safezone.app.ui.report;

import android.net.Uri;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.safezone.app.ServiceLocator;
import com.safezone.app.model.CrimeReport;
import com.safezone.app.model.ReportCategory;
import com.safezone.app.service.ReportService;
import com.safezone.app.ui.common.Event;
import com.safezone.app.util.LatLng;

public class ReportViewModel extends ViewModel {
    private final MutableLiveData<ReportCategory> category = new MutableLiveData<>();
    private final MutableLiveData<Uri> photo = new MutableLiveData<>();
    private final MutableLiveData<Boolean> picking = new MutableLiveData<>(false);
    private Uri pendingCameraUri;
    private final MutableLiveData<Boolean> submitting = new MutableLiveData<>(false);
    private final MutableLiveData<String> error = new MutableLiveData<>();
    private final MutableLiveData<Event<Boolean>> submitted = new MutableLiveData<>();

    public LiveData<ReportCategory> category() { return category; }
    public LiveData<Uri> photo() { return photo; }
    public LiveData<Boolean> picking() { return picking; }
    public LiveData<Boolean> submitting() { return submitting; }
    public LiveData<String> error() { return error; }
    public LiveData<Event<Boolean>> submitted() { return submitted; }

    public boolean isSubmitting() { return Boolean.TRUE.equals(submitting.getValue()); }
    public ReportCategory getCategory() { return category.getValue(); }

    public void selectCategory(ReportCategory c) { category.setValue(c); }
    public Uri getPendingCameraUri() { return pendingCameraUri; }
    public void setPendingCameraUri(Uri uri) { pendingCameraUri = uri; }
    public void setPicking(boolean value) { picking.setValue(value); }
    public void setPhoto(Uri uri) { photo.setValue(uri); }
    public void removePhoto() { photo.setValue(null); }

    public void postPhoto(Uri uri) { picking.postValue(false); photo.postValue(uri); }
    public void postPickError(String message) { picking.postValue(false); error.postValue(message); }
    public void setError(String message) { error.setValue(message); }

    public boolean begin(String area, String description) {
        if (isSubmitting()) return false;
        String problem = ReportValidator.validate(getCategory(), area, description);
        if (problem != null) {
            error.setValue(problem);
            return false;
        }
        error.setValue(null);
        submitting.setValue(true);
        return true;
    }

    public void send(String area, String description, LatLng location) {
        ServiceLocator.reports().submitReport(getCategory(), area, description,
                photo.getValue(), location.latitude, location.longitude,
                new ReportService.SubmitCallback() {
                    @Override public void onSuccess(CrimeReport report) {
                        category.setValue(null);
                        photo.setValue(null);
                        submitting.setValue(false);
                        submitted.setValue(new Event<>(true));
                    }
                    @Override public void onError(Exception e) {
                        submitting.setValue(false);
                        error.setValue("Couldn't submit your report: " + e.getMessage());
                    }
                });
    }
}
