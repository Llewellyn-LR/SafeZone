package com.safezone.app.ui.auth;

import android.animation.ValueAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.animation.DecelerateInterpolator;

import androidx.appcompat.app.AppCompatActivity;

import com.safezone.app.R;
import com.safezone.app.ServiceLocator;

public class SplashActivity extends AppCompatActivity {
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Runnable route = () -> {
        if (isFinishing() || isDestroyed()) return;
        if (ServiceLocator.auth().isLoggedIn()) {

            RoleRouter.routeAfterAuth(this);
        } else {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        }
    };

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_splash);

        View logo = findViewById(R.id.logo);
        if (ValueAnimator.areAnimatorsEnabled()) {
            logo.setAlpha(0f);
            logo.setScaleX(0.92f);
            logo.setScaleY(0.92f);
            logo.animate().alpha(1f).scaleX(1f).scaleY(1f).setDuration(700)
                    .setInterpolator(new DecelerateInterpolator()).start();
        }
        handler.postDelayed(route, 2200);
    }

    @Override
    protected void onDestroy() {
        handler.removeCallbacks(route);
        super.onDestroy();
    }
}
