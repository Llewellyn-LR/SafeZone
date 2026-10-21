package com.safezone.app.ui.common;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.view.View;

import com.google.android.material.snackbar.Snackbar;
import com.safezone.app.R;

public final class Dialer {
    private Dialer() {}

    public static void dial(View anchor, String dial, String display) {
        try {
            anchor.getContext().startActivity(
                    new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + Uri.encode(dial))));
        } catch (ActivityNotFoundException | SecurityException e) {
            Snackbar.make(anchor, anchor.getContext().getString(R.string.dial_failed, display),
                    Snackbar.LENGTH_LONG).show();
        }
    }

    public static void directions(View anchor, String name, double lat, double lng) {
        Uri uri = Uri.parse("geo:" + lat + "," + lng + "?q=" + lat + "," + lng
                + "(" + Uri.encode(name) + ")");
        try {
            anchor.getContext().startActivity(new Intent(Intent.ACTION_VIEW, uri));
        } catch (ActivityNotFoundException e) {
            Snackbar.make(anchor, R.string.directions_failed, Snackbar.LENGTH_LONG).show();
        }
    }
}
