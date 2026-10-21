package com.safezone.app.ui.wanted;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.snackbar.Snackbar;
import com.safezone.app.R;
import com.safezone.app.ServiceLocator;
import com.safezone.app.model.WantedPerson;
import com.safezone.app.service.DataListener;
import com.safezone.app.service.Subscription;
import com.safezone.app.ui.common.StreamErrorView;
import com.safezone.app.ui.common.Ui;

import java.util.List;

public class WantedPersonsListActivity extends AppCompatActivity {

    private static final String CRIME_STOP_NUMBER = "0860010111";

    private View progress, scroll;
    private StreamErrorView errorView;
    private LinearLayout list;
    private Subscription subscription;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wanted_list);

        MaterialToolbar tb = findViewById(R.id.toolbar);
        tb.setNavigationOnClickListener(v -> finish());
        progress = findViewById(R.id.progress);
        scroll = findViewById(R.id.scroll);
        errorView = findViewById(R.id.errorView);
        errorView.setMessage("Couldn't load wanted persons.");
        list = findViewById(R.id.list);

        View banner = findViewById(R.id.infoBanner);
        banner.setBackground(Ui.rounded(this,
                Ui.withAlpha(ContextCompat.getColor(this, R.color.navy), 0.06f), 14));

        subscription = ServiceLocator.wantedPersons(this).watchWantedPersons(
                new DataListener<List<WantedPerson>>() {
                    @Override public void onData(List<WantedPerson> people) { render(people); }
                    @Override public void onError(Exception e) {
                        progress.setVisibility(View.GONE);
                        scroll.setVisibility(View.GONE);
                        errorView.setVisibility(View.VISIBLE);
                    }
                });
    }

    @Override
    protected void onDestroy() {
        if (subscription != null) subscription.cancel();
        super.onDestroy();
    }

    private void render(List<WantedPerson> people) {
        progress.setVisibility(View.GONE);
        errorView.setVisibility(View.GONE);
        scroll.setVisibility(View.VISIBLE);
        list.removeAllViews();

        if (people.isEmpty()) {
            TextView empty = new TextView(this);
            empty.setText("No wanted-persons notices published right now.");
            empty.setTextColor(ContextCompat.getColor(this, R.color.text_secondary));
            empty.setPadding(0, Ui.dp(this, 24), 0, Ui.dp(this, 24));
            list.addView(empty);
            return;
        }
        for (WantedPerson p : people) list.addView(buildCard(p, list));
    }

    private View buildCard(WantedPerson p, ViewGroup parent) {
        View card = getLayoutInflater().inflate(R.layout.item_wanted_person, parent, false);

        card.findViewById(R.id.armedBanner)
                .setVisibility(p.armedAndDangerous ? View.VISIBLE : View.GONE);

        ((TextView) card.findViewById(R.id.name)).setText(p.displayName());
        ((TextView) card.findViewById(R.id.lastSeen)).setText("Last seen: " + p.lastSeenArea);
        ((TextView) card.findViewById(R.id.wantedFor)).setText(p.wantedFor);

        TextView caseRef = card.findViewById(R.id.caseRef);
        if (p.caseReference != null) {
            caseRef.setVisibility(View.VISIBLE);
            caseRef.setText("Case ref: " + p.caseReference);
        } else {
            caseRef.setVisibility(View.GONE);
        }

        bindPhoto(card.findViewById(R.id.photo), p.photoUrl);
        card.findViewById(R.id.callButton).setOnClickListener(v -> callCrimeStop(v));
        return card;
    }

    private void bindPhoto(ImageView iv, @Nullable String url) {
        if (url == null) { showPlaceholder(iv); return; }
        iv.setImageTintList(null);
        iv.setScaleType(ImageView.ScaleType.CENTER_CROP);
        Glide.with(this).load(url).listener(new RequestListener<Drawable>() {
            @Override public boolean onLoadFailed(@Nullable GlideException e, Object m,
                                                  Target<Drawable> t, boolean first) {
                showPlaceholder(iv);
                return true;
            }
            @Override public boolean onResourceReady(Drawable r, Object m, Target<Drawable> t,
                                                     DataSource d, boolean first) { return false; }
        }).into(iv);
    }

    private void showPlaceholder(ImageView iv) {
        iv.setScaleType(ImageView.ScaleType.CENTER);
        iv.setImageResource(R.drawable.ic_person_28);
        iv.setImageTintList(ColorStateList.valueOf(
                ContextCompat.getColor(this, R.color.text_secondary)));
    }

    private void callCrimeStop(View anchor) {
        boolean launched;
        try {
            startActivity(new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + CRIME_STOP_NUMBER)));
            launched = true;
        } catch (ActivityNotFoundException | SecurityException e) {
            launched = false;
        }
        if (!launched) {
            Snackbar.make(anchor, "Couldn't open the phone app. Crime Stop: 08600 10111",
                    Snackbar.LENGTH_LONG).show();
        }
    }
}
