package com.safezone.app.ui.auth;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.safezone.app.ServiceLocator;
import com.safezone.app.service.AuthService;
import com.safezone.app.ui.common.Event;

public class AuthFormViewModel extends ViewModel {
    private final MutableLiveData<Boolean> loading = new MutableLiveData<>(false);
    private final MutableLiveData<String> error = new MutableLiveData<>();
    private final MutableLiveData<Event<Boolean>> signedIn = new MutableLiveData<>();

    public LiveData<Boolean> loading() { return loading; }
    public LiveData<String> error() { return error; }
    public LiveData<Event<Boolean>> signedIn() { return signedIn; }

    public boolean isLoading() { return Boolean.TRUE.equals(loading.getValue()); }

    private final AuthService.Callback callback = new AuthService.Callback() {
        @Override public void onSuccess() {

            signedIn.setValue(new Event<>(true));
        }
        @Override public void onError(String message) {
            loading.setValue(false);
            error.setValue(message);
        }
    };

    public void login(String email, String password) {
        if (isLoading()) return;
        begin();
        ServiceLocator.auth().login(email, password, callback);
    }

    public void continueAsGuest() {
        if (isLoading()) return;
        begin();
        ServiceLocator.auth().continueAsGuest(callback);
    }

    public void register(String name, String email, String password, String confirm) {
        if (isLoading()) return;
        String problem = AuthFormValidator.validateRegistration(password, confirm);
        if (problem != null) {
            error.setValue(problem);
            return;
        }
        begin();
        ServiceLocator.auth().register(name, email, password, callback);
    }

    private void begin() {
        error.setValue(null);
        loading.setValue(true);
    }
}
