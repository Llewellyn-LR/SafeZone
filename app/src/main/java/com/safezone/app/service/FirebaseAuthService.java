package com.safezone.app.service;

import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.safezone.app.model.AppUser;

public class FirebaseAuthService extends AuthService {
    private final FirebaseAuth auth = FirebaseAuth.getInstance();
    private AppUser currentUser;
    private boolean loading;

    public FirebaseAuthService() {

        currentUser = mapUser(auth.getCurrentUser());
        auth.addAuthStateListener(a -> {
            currentUser = mapUser(a.getCurrentUser());
            notifyListeners();
        });
    }

    @Override public AppUser getCurrentUser() { return currentUser; }
    @Override public boolean isLoading() { return loading; }

    private AppUser mapUser(FirebaseUser user) {
        if (user == null) return null;
        if (user.isAnonymous()) return AppUser.guestWithUid(user.getUid());
        String email = user.getEmail() != null ? user.getEmail() : "";
        String name;
        if (user.getDisplayName() != null && !user.getDisplayName().isEmpty()) {
            name = user.getDisplayName();
        } else if (user.getEmail() != null) {
            name = user.getEmail().split("@")[0];
        } else {
            name = "Resident";
        }
        return AppUser.fromFirebase(user.getUid(), name, email);
    }

    @Override
    public void login(String email, String password, Callback cb) {
        setLoading(true);
        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener(t -> finish(t, cb));
    }

    @Override
    public void register(String name, String email, String password, Callback cb) {
        setLoading(true);
        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(t -> {
            if (!t.isSuccessful()) { finish(t, cb); return; }
            FirebaseUser user = t.getResult().getUser();
            if (user == null) { finish(t, cb); return; }
            user.updateProfile(new UserProfileChangeRequest.Builder()
                            .setDisplayName(name.trim()).build())

                    .continueWithTask(x -> user.reload())
                    .addOnCompleteListener(x -> {
                        currentUser = mapUser(auth.getCurrentUser());
                        notifyListeners();
                        setLoading(false);
                        cb.onSuccess();
                    });
        });
    }

    @Override
    public void continueAsGuest(Callback cb) {
        setLoading(true);
        auth.signInAnonymously().addOnCompleteListener(t -> finish(t, cb));
    }

    @Override
    public void logout() {
        setLoading(true);
        try {
            auth.signOut();
        } finally {
            setLoading(false);
        }
    }

    private void finish(Task<AuthResult> t, Callback cb) {
        setLoading(false);
        if (t.isSuccessful()) cb.onSuccess();
        else cb.onError(friendlyMessage(t.getException()));
    }

    private void setLoading(boolean v) {
        loading = v;
        notifyListeners();
    }

    static String friendlyMessage(Exception e) {
        if (e instanceof FirebaseNetworkException) {
            return "No internet connection. Check your network and try again.";
        }
        if (e instanceof FirebaseAuthException) {
            switch (((FirebaseAuthException) e).getErrorCode()) {
                case "ERROR_INVALID_EMAIL": return "Enter a valid email address.";
                case "ERROR_USER_DISABLED": return "This account has been disabled.";
                case "ERROR_USER_NOT_FOUND":
                case "ERROR_WRONG_PASSWORD":
                case "ERROR_INVALID_CREDENTIAL": return "Incorrect email or password.";
                case "ERROR_EMAIL_ALREADY_IN_USE": return "An account already exists with that email.";
                case "ERROR_WEAK_PASSWORD": return "Password must be at least 6 characters.";
                default: break;
            }
        }
        return e != null && e.getMessage() != null
                ? e.getMessage() : "Something went wrong. Please try again.";
    }
}
