package com.safezone.app.ui.anim;

import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RadialGradient;
import android.graphics.Rect;
import android.graphics.Shader;

import androidx.annotation.NonNull;

public class SunsetBayDrawable extends LoopingDrawable {
    private static final float TWO_PI = (float) (Math.PI * 2);

    private static final float[][] LAYERS = {
            {0.28f, 0.09f, 0.95f, 11f, 1f},
            {0.48f, 0.10f, 0.70f, 8f, -1f},
            {0.70f, 0.09f, 0.55f, 6f, 1f},
    };
    private static final int[] COLORS = {0x8C4A9DB0, 0xBF2F7F94, 0xEB1D5E74};

    private final Paint sky = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint sun = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint wave = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Path path = new Path();
    private final Matrix sunMatrix = new Matrix();
    private final Paint gull = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Path gullPath = new Path();

    private static final float[][] GULLS = {
            {0.13f, 9f, 0.030f, 0.10f, 5.0f, 0.0f},
            {0.20f, 6f, 0.022f, 0.55f, 6.2f, 1.7f},
            {0.09f, 5f, 0.016f, 0.80f, 5.6f, 3.1f},
    };
    private Shader sunShader;
    private float sunCx, sunCy;

    public SunsetBayDrawable() {
        gull.setStyle(Paint.Style.STROKE);
        gull.setStrokeCap(Paint.Cap.ROUND);
        gull.setStrokeJoin(Paint.Join.ROUND);
        gull.setColor(0x995B3A1E);
        gull.setStrokeWidth(dp(1.6f));
    }

    @Override protected void onBoundsChange(@NonNull Rect b) {
        sky.setShader(new LinearGradient(0, b.top, 0, b.bottom,
                new int[]{0xFFCFE6F0, 0xFFF7C892, 0xFFE58B5E}, new float[]{0f, 0.55f, 1f}, Shader.TileMode.CLAMP));
        sunCx = b.exactCenterX();
        sunCy = b.top + b.height() * 0.8f;
        sunShader = new RadialGradient(sunCx, sunCy, b.width() * 0.6f,
                0xF2FFD68C, 0x00FFD68C, Shader.TileMode.CLAMP);
        sun.setShader(sunShader);
    }

    @Override public void draw(@NonNull Canvas c) {
        ensureRunning();
        Rect b = getBounds();
        if (b.isEmpty()) return;
        float t = seconds();

        c.drawRect(b, sky);

        float pulse = 1f + 0.06f * (float) Math.sin(t * 1.1f);
        float e = android.animation.ValueAnimator.areAnimatorsEnabled() ? Math.min(1f, t / 2.5f) : 1f;
        float rise = (1f - (1f - e) * (1f - e)) * b.height() * 0.10f;
        sunMatrix.setScale(pulse, pulse, sunCx, sunCy);
        sunMatrix.postTranslate(0, b.height() * 0.10f - rise);
        sunShader.setLocalMatrix(sunMatrix);
        c.drawRect(b, sun);

        drawGulls(c, b, t);

        float band = Math.min(dp(150), b.height());
        float top = b.bottom - band;
        for (int i = 0; i < LAYERS.length; i++) {
            float[] l = LAYERS[i];
            float base = top + band * l[0] + (float) Math.sin(t * 0.8f + i) * band * 0.012f;
            float amp = band * l[1];
            float k = TWO_PI / (b.width() * l[2]);
            float phase = l[4] * t * TWO_PI / l[3];
            path.reset();
            path.moveTo(b.left, b.bottom);
            for (float x = 0; x <= b.width() + 8; x += 8) {
                float a = x * k + phase + i * 1.7f;
                float y = base + amp * ((float) Math.sin(a) + 0.3f * (float) Math.sin(2 * a + 1.3f)) / 1.3f;
                path.lineTo(b.left + x, y);
            }
            path.lineTo(b.right, b.bottom);
            path.close();
            wave.setColor(COLORS[i]);
            c.drawPath(path, wave);
        }
    }

    private void drawGulls(Canvas c, Rect b, float t) {
        for (float[] g : GULLS) {
            float span = b.width() + 4 * dp(g[1]);
            float x = b.left - 2 * dp(g[1]) + ((g[3] + t * g[2]) % 1f) * span;
            float y = b.top + b.height() * g[0] + (float) Math.sin(t * 0.7f + g[5]) * dp(5);
            float s = dp(g[1]);
            float f = 0.45f * (float) Math.sin(t * g[4] + g[5]);
            gullPath.reset();
            gullPath.moveTo(x - s, y - f * s);
            gullPath.quadTo(x - s * 0.5f, y - f * s * 0.6f - s * 0.35f, x, y);
            gullPath.quadTo(x + s * 0.5f, y - f * s * 0.6f - s * 0.35f, x + s, y - f * s);
            c.drawPath(gullPath, gull);
        }
    }
}
