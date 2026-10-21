package com.safezone.app.ui.common;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.view.View;
import android.widget.Button;

import androidx.annotation.ColorInt;
import androidx.core.graphics.ColorUtils;

public final class Ui {
    private Ui() {}

    public static int dp(Context c, float v) {
        return Math.round(v * c.getResources().getDisplayMetrics().density);
    }

    @ColorInt
    public static int withAlpha(@ColorInt int color, float alpha) {
        return ColorUtils.setAlphaComponent(color, Math.round(alpha * 255));
    }

    public static GradientDrawable rounded(Context c, @ColorInt int color, float radiusDp) {
        GradientDrawable d = new GradientDrawable();
        d.setColor(color);
        d.setCornerRadius(dp(c, radiusDp));
        return d;
    }

    public static GradientDrawable stroked(Context c, @ColorInt int fill, @ColorInt int stroke,
                                           float strokeDp, float radiusDp) {
        GradientDrawable d = rounded(c, fill, radiusDp);
        d.setStroke(Math.round(strokeDp * c.getResources().getDisplayMetrics().density), stroke);
        return d;
    }

    public static GradientDrawable circle(@ColorInt int color) {
        GradientDrawable d = new GradientDrawable();
        d.setShape(GradientDrawable.OVAL);
        d.setColor(color);
        return d;
    }

    public static void setButtonLoading(Button button, View spinner, CharSequence label, boolean loading) {
        button.setText(loading ? "" : label);
        button.setClickable(!loading);
        spinner.setVisibility(loading ? View.VISIBLE : View.GONE);
    }
}
