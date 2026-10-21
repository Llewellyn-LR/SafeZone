package com.safezone.app.ui.anim;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.LinearInterpolator;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.safezone.app.R;

public class HandsLoaderView extends View {
    private static final int COUNT = 5;
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Path shape = HandShape.create();
    private final float handH, handW, gap, rise;
    private ValueAnimator animator;
    private float progress;

    public HandsLoaderView(Context c) { this(c, null); }

    public HandsLoaderView(Context c, @Nullable AttributeSet a) {
        super(c, a);
        float d = getResources().getDisplayMetrics().density;
        handW = 26 * d;
        handH = handW * HandShape.H / HandShape.W;
        gap = 8 * d;
        rise = 14 * d;
        paint.setColor(ContextCompat.getColor(c, R.color.brand_brown));
    }

    @Override protected void onMeasure(int w, int h) {
        setMeasuredDimension(resolveSize((int) (COUNT * handW + (COUNT - 1) * gap), w),
                resolveSize((int) (handH + rise), h));
    }

    private void startLoop() {
        if (animator != null) return;
        if (!ValueAnimator.areAnimatorsEnabled()) { progress = 0.25f; invalidate(); return; }
        animator = ValueAnimator.ofFloat(0f, 1f);
        animator.setDuration(1500);
        animator.setInterpolator(new LinearInterpolator());
        animator.setRepeatCount(ValueAnimator.INFINITE);
        animator.addUpdateListener(v -> { progress = (float) v.getAnimatedValue(); invalidate(); });
        animator.start();
    }

    private void stopLoop() {
        if (animator != null) animator.cancel();
        animator = null;
    }

    @Override public void onVisibilityAggregated(boolean isVisible) {
        super.onVisibilityAggregated(isVisible);
        if (isVisible) startLoop(); else stopLoop();
    }

    @Override protected void onDetachedFromWindow() {
        stopLoop();
        super.onDetachedFromWindow();
    }

    @Override protected void onDraw(Canvas c) {
        float total = COUNT * handW + (COUNT - 1) * gap;
        float left = (getWidth() - total) / 2f;
        float baseY = getHeight();
        float s = handW / HandShape.W;
        for (int i = 0; i < COUNT; i++) {

            float phase = progress - i * 0.12f;
            phase -= (float) Math.floor(phase);
            float up = (float) Math.sin(Math.PI * Math.min(1f, phase * 1.8f));
            if (phase * 1.8f > 1f) up = 0f;
            paint.setAlpha(Math.round(255 * (0.35f + 0.65f * up)));
            c.save();
            c.translate(left + i * (handW + gap) + handW / 2f, baseY - handH / 2f - rise * up);
            c.rotate((i % 2 == 0 ? -1 : 1) * 8f * up);
            c.scale(s, s);
            c.translate(-HandShape.CX, -HandShape.CY);
            c.drawPath(shape, paint);
            c.restore();
        }
    }
}
