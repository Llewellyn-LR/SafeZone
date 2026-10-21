package com.safezone.app.ui.anim;

import android.animation.ValueAnimator;
import android.graphics.ColorFilter;
import android.graphics.PixelFormat;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;
import android.os.SystemClock;

import androidx.annotation.Nullable;

abstract class LoopingDrawable extends Drawable implements Animatable {
    private static final long FRAME_MS = 33;
    private boolean running;
    private long startUptime;

    private final Runnable tick = new Runnable() {
        @Override public void run() {
            if (!running) return;
            invalidateSelf();
            scheduleSelf(this, SystemClock.uptimeMillis() + FRAME_MS);
        }
    };

    protected final float seconds() {
        if (!ValueAnimator.areAnimatorsEnabled()) return 0f;
        return (SystemClock.uptimeMillis() - startUptime) / 1000f;
    }

    @Override public void start() {
        if (running) return;
        running = true;
        startUptime = SystemClock.uptimeMillis();
        if (ValueAnimator.areAnimatorsEnabled()) scheduleSelf(tick, startUptime + FRAME_MS);
        invalidateSelf();
    }

    @Override public void stop() {
        running = false;
        unscheduleSelf(tick);
    }

    @Override public boolean isRunning() { return running; }

    @Override public boolean setVisible(boolean visible, boolean restart) {
        boolean changed = super.setVisible(visible, restart);
        if (visible) start(); else stop();
        return changed;
    }

    protected final void ensureRunning() { if (!running && isVisible()) start(); }

    @Override public void setAlpha(int alpha) {}
    @Override public void setColorFilter(@Nullable ColorFilter colorFilter) {}
    @Override public int getOpacity() { return PixelFormat.OPAQUE; }

    @Override public int getIntrinsicWidth() { return -1; }
    @Override public int getIntrinsicHeight() { return -1; }

    protected static float dp(float v) { return v * android.content.res.Resources.getSystem().getDisplayMetrics().density; }
}
