package com.safezone.app.ui.admin;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.safezone.app.R;
import com.safezone.app.SafeZoneApp;
import com.safezone.app.service.ModeratorService;
import com.safezone.app.ui.auth.LoginActivity;

public class AdminActivity extends AppCompatActivity {
    private View progress;
    private String gateUid;
    private final FirebaseAuth.AuthStateListener authListener = auth -> onAuthChanged();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);
        progress = findViewById(R.id.adminProgress);
        if (!SafeZoneApp.isFirebaseAvailable()) toLogin();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (SafeZoneApp.isFirebaseAvailable()) {
            progress.setVisibility(View.VISIBLE);
            FirebaseAuth.getInstance().addAuthStateListener(authListener);
        }
    }

    @Override
    protected void onStop() {
        if (SafeZoneApp.isFirebaseAvailable()) {
            FirebaseAuth.getInstance().removeAuthStateListener(authListener);
        }
        super.onStop();
    }

    private void onAuthChanged() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null || user.isAnonymous()) {
            toLogin();
            return;
        }
        if (user.getUid().equals(gateUid)) {
            progress.setVisibility(View.GONE);
            return;
        }

        final String uid = user.getUid();
        gateUid = uid;
        progress.setVisibility(View.VISIBLE);
        new ModeratorService().isModerator(uid).addOnCompleteListener(task -> {
            if (isDestroyed() || !uid.equals(gateUid)) return;
            progress.setVisibility(View.GONE);
            if (!task.isSuccessful()) {
                show(AccessDeniedFragment.newInstance(
                        "Couldn't verify moderator access. Check your connection and try signing in again.",
                        true));
            } else if (Boolean.TRUE.equals(task.getResult())) {
                show(new AdminShellFragment());
            } else {
                show(AccessDeniedFragment.newInstance(AccessDeniedFragment.DEFAULT_MESSAGE, true));
            }
        });
    }

    private void toLogin() {
        startActivity(new Intent(this, LoginActivity.class)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
        finish();
    }

    private void show(Fragment f) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.adminContainer, f)
                .commitAllowingStateLoss();
    }
}
