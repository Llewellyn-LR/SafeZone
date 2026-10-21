package com.safezone.app.ui.anim;

import android.graphics.Matrix;
import android.graphics.Path;
import android.graphics.RectF;

final class HandShape {
    static final float W = 40f, H = 48f, CX = 20f, CY = 24f;

    private HandShape() {}

    static Path create() {
        Path p = new Path();
        p.addRoundRect(new RectF(9f, 22f, 31f, 44f), 9f, 9f, Path.Direction.CW);
        float[][] fingers = {{9.5f, 14f, 20f}, {15f, 6f, 26f}, {20.5f, 3f, 28f}, {26f, 8f, 24f}};
        for (float[] f : fingers) {
            p.addRoundRect(new RectF(f[0], f[1], f[0] + 4.6f, f[1] + f[2]), 2.3f, 2.3f, Path.Direction.CW);
        }
        Path thumb = new Path();
        thumb.addRoundRect(new RectF(31f, 24f, 35.6f, 39f), 2.3f, 2.3f, Path.Direction.CW);
        Matrix m = new Matrix();
        m.setRotate(35f, 33f, 38f);
        thumb.transform(m);
        p.addPath(thumb);
        return p;
    }
}
