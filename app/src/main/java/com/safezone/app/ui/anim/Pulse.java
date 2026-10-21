package com.safezone.app.ui.anim;

import android.animation.Keyframe;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.view.View;

import androidx.annotation.Nullable;

public final class Pulse {
    private Pulse() {}

    @Nullable
    public static ObjectAnimator heartbeat(View v) {
        if (!ValueAnimator.areAnimatorsEnabled()) return null;
        PropertyValuesHolder sx = PropertyValuesHolder.ofKeyframe(View.SCALE_X,
                Keyframe.ofFloat(0f, 1f), Keyframe.ofFloat(0.15f, 1.045f), Keyframe.ofFloat(0.30f, 1f),
                Keyframe.ofFloat(0.42f, 1.03f), Keyframe.ofFloat(0.60f, 1f), Keyframe.ofFloat(1f, 1f));
        PropertyValuesHolder sy = PropertyValuesHolder.ofKeyframe(View.SCALE_Y,
                Keyframe.ofFloat(0f, 1f), Keyframe.ofFloat(0.15f, 1.045f), Keyframe.ofFloat(0.30f, 1f),
                Keyframe.ofFloat(0.42f, 1.03f), Keyframe.ofFloat(0.60f, 1f), Keyframe.ofFloat(1f, 1f));
        ObjectAnimator a = ObjectAnimator.ofPropertyValuesHolder(v, sx, sy);
        a.setDuration(2200);
        a.setRepeatCount(ValueAnimator.INFINITE);
        a.start();
        return a;
    }

    public static void pause(@Nullable ObjectAnimator a) {
        if (a != null && a.isRunning()) a.pause();
    }

    public static void resume(@Nullable ObjectAnimator a) {
        if (a != null && a.isPaused()) a.resume();
    }

    public static void cancel(@Nullable ObjectAnimator a, View v) {
        if (a != null) a.cancel();
        v.setScaleX(1f);
        v.setScaleY(1f);
    }
}
