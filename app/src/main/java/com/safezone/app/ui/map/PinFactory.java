package com.safezone.app.ui.map;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RadialGradient;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;

import com.safezone.app.R;
import com.safezone.app.model.ReportCategory;
import com.safezone.app.ui.common.Ui;

final class PinFactory {
    private PinFactory() {}

    private static final float PIN_W = 56f, PIN_H = 58f;
    private static final float HEAD_R = 14f, HEAD_CY = 26f;
    private static final float GLOW_R = 24f;

    static final float ANCHOR_X = 0.5f;
    static final float ANCHOR_Y = (HEAD_CY + 2 * HEAD_R) / PIN_H;

    static Drawable hotspotPin(Context c, int color, boolean verified,
                               @Nullable ReportCategory category, boolean glow) {
        Bitmap bmp = Bitmap.createBitmap(Ui.dp(c, PIN_W), Ui.dp(c, PIN_H), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bmp);
        float cx = Ui.dp(c, PIN_W) / 2f;
        float cy = Ui.dp(c, HEAD_CY);
        float r = Ui.dp(c, HEAD_R);

        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        if (glow) {
            float gr = Ui.dp(c, GLOW_R);
            p.setShader(new RadialGradient(cx, cy, gr,
                    new int[]{Ui.withAlpha(color, 0.65f), Ui.withAlpha(color, 0.25f), Ui.withAlpha(color, 0f)},
                    new float[]{0.45f, 0.75f, 1f}, Shader.TileMode.CLAMP));
            canvas.drawCircle(cx, cy, gr, p);
            p.setShader(null);
        }

        float tipY = cy + 2 * r;
        float startX = cx + (float) (r * Math.cos(Math.toRadians(150)));
        float startY = cy + (float) (r * Math.sin(Math.toRadians(150)));
        Path drop = new Path();
        drop.moveTo(cx, tipY);
        drop.lineTo(startX, startY);
        drop.arcTo(new RectF(cx - r, cy - r, cx + r, cy + r), 150, 240);
        drop.close();

        p.setStyle(Paint.Style.FILL);
        p.setColor(verified ? color : Color.WHITE);
        p.setShadowLayer(Ui.dp(c, 3), 0, Ui.dp(c, 1.5f), 0x33000000);
        canvas.drawPath(drop, p);
        p.clearShadowLayer();

        p.setStyle(Paint.Style.STROKE);
        p.setStrokeJoin(Paint.Join.ROUND);
        p.setStrokeWidth(Ui.dp(c, verified ? 1.5f : 2.5f));
        p.setColor(verified ? Color.WHITE : color);
        canvas.drawPath(drop, p);

        int iconRes = category != null ? category.iconRes : R.drawable.ic_priority_high;
        Drawable icon = DrawableCompat.wrap(ContextCompat.getDrawable(c, iconRes).mutate());
        DrawableCompat.setTint(icon, verified ? Color.WHITE : color);
        int half = Ui.dp(c, 8);
        icon.setBounds((int) cx - half, (int) cy - half, (int) cx + half, (int) cy + half);
        icon.draw(canvas);
        return new BitmapDrawable(c.getResources(), bmp);
    }

    static Drawable stationPin(Context c, int navy) {
        int size = Ui.dp(c, 40);
        Bitmap bmp = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bmp);
        float cx = size / 2f, cy = size / 2f;
        float half = Ui.dp(c, 14);
        float corner = Ui.dp(c, 7);

        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        p.setColor(navy);
        p.setShadowLayer(Ui.dp(c, 4), 0, Ui.dp(c, 2), 0x26000000);
        RectF box = new RectF(cx - half, cy - half, cx + half, cy + half);
        canvas.drawRoundRect(box, corner, corner, p);
        p.clearShadowLayer();

        p.setStyle(Paint.Style.STROKE);
        p.setStrokeWidth(Ui.dp(c, 2));
        p.setColor(Color.WHITE);
        canvas.drawRoundRect(box, corner, corner, p);

        Drawable icon = DrawableCompat.wrap(
                ContextCompat.getDrawable(c, R.drawable.ic_local_police).mutate());
        DrawableCompat.setTint(icon, Color.WHITE);
        int h = Ui.dp(c, 9);
        icon.setBounds((int) cx - h, (int) cy - h, (int) cx + h, (int) cy + h);
        icon.draw(canvas);
        return new BitmapDrawable(c.getResources(), bmp);
    }

    static Drawable userDot(Context c, int navy) {
        int size = Ui.dp(c, 22);
        Bitmap bmp = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bmp);
        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        p.setColor(Color.WHITE);
        canvas.drawCircle(size / 2f, size / 2f, size / 2f, p);
        p.setColor(navy);
        canvas.drawCircle(size / 2f, size / 2f, size / 2f - Ui.dp(c, 3), p);
        return new BitmapDrawable(c.getResources(), bmp);
    }
}
