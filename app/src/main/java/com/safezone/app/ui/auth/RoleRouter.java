package com.safezone.app.ui.auth;

import android.app.Activity;
import android.content.Intent;

import com.safezone.app.SafeZoneApp;
import com.safezone.app.ServiceLocator;
import com.safezone.app.model.AppUser;
import com.safezone.app.service.ModeratorService;
import com.safezone.app.ui.admin.AdminActivity;
import com.safezone.app.ui.home.MainActivity;

public final class RoleRouter {
    private RoleRouter() {}

    public static void routeAfterAuth(Activity from) {
        AppUser user = ServiceLocator.auth().getCurrentUser();
        if (!SafeZoneApp.isFirebaseAvailable() || user == null || user.isGuest || user.uid == null) {
            go(from, MainActivity.class);
            return;
        }
        new ModeratorService().isModerator(user.uid).addOnCompleteListener(task -> {
            if (from.isDestroyed() || from.isFinishing()) return;
            boolean moderator = task.isSuccessful() && Boolean.TRUE.equals(task.getResult());
            go(from, moderator ? AdminActivity.class : MainActivity.class);
        });
    }

    private static void go(Activity from, Class<? extends Activity> target) {
        from.startActivity(new Intent(from, target)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
        from.finish();
    }
}
