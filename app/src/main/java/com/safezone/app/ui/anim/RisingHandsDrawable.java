package com.safezone.app.ui.anim;

import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.Shader;

import androidx.annotation.NonNull;

public class RisingHandsDrawable extends LoopingDrawable {
    private static final float GRID_W = 228f, GRID_H = 110f;

    private static final float[][] ROWS = {{0, .75f, 22}, {18, .50f, 20}, {34, .32f, 18}, {48, .20f, 16}};
    private static final int[] ROW_COLORS = {0xFF7A4A1E, 0xFF9A6A33, 0xFFB88945, 0xFFC99A55};

    private final Paint bg = new Paint();
    private final Paint hand = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Path shape = HandShape.create();

    public RisingHandsDrawable() {}

    @Override protected void onBoundsChange(@NonNull Rect b) {
        bg.setShader(new LinearGradient(0, b.top, 0, b.bottom,
                new int[]{0xFFF7F0E3, 0xFFF3E4C9, 0xFFEBD3A8}, new float[]{0f, 0.7f, 1f}, Shader.TileMode.CLAMP));
    }

    @Override public void draw(@NonNull Canvas c) {
        ensureRunning();
        Rect b = getBounds();
        if (b.isEmpty()) return;
        float t = seconds();
        c.drawRect(b, bg);

        float k = b.width() / GRID_W;
        for (int r = 0; r < ROWS.length; r++) {
            float bottomOff = ROWS[r][0], size = ROWS[r][2];
            int n = (r % 2 == 1) ? 8 : 9;
            float pad = (r % 2 == 1) ? 10f : 0f;
            hand.setColor(ROW_COLORS[r]);
            hand.setAlpha(Math.round(ROWS[r][1] * 255));
            for (int i = 0; i < n; i++) {
                float wavePhase = t * 1.7f + i * 0.75f + r * 1.1f;
                float rot = (i % 2 == 1 ? 7f : -7f) + 6f * (float) Math.sin(wavePhase);
                float lift = 3.2f * (float) Math.sin(wavePhase - 0.9f);
                float cx = pad + i * (GRID_W - 2 * pad - size) / (n - 1) + size / 2f;
                float cy = GRID_H - bottomOff - size * 1.2f / 2f - Math.max(0f, lift);
                c.save();
                c.translate(b.left + cx * k, b.bottom - (GRID_H - cy) * k);
                c.rotate(rot);
                float s = size / HandShape.W * k;
                c.scale(s, s);
                c.translate(-HandShape.CX, -HandShape.CY);
                c.drawPath(shape, hand);
                c.restore();
            }
        }
    }
}
