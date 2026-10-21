package com.safezone.app.ui.profile;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.safezone.app.R;
import com.safezone.app.ServiceLocator;
import com.safezone.app.model.AppUser;
import com.safezone.app.service.AuthService;
import com.safezone.app.ui.auth.LoginActivity;
import com.safezone.app.service.NotificationService;
import com.safezone.app.ui.common.Ui;
import com.safezone.app.ui.help.HelpActivity;
import com.safezone.app.ui.help.PrivacyActivity;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class ProfileFragment extends Fragment {
    private TextView name, email;
    private View guestCard;
    private MaterialButton logoutButton;
    private final AuthService.Listener authListener = this::bind;

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater i, @Nullable ViewGroup c, @Nullable Bundle s) {
        return i.inflate(R.layout.fragment_profile, c, false);
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle s) {
        int navy = ContextCompat.getColor(requireContext(), R.color.navy);
        v.findViewById(R.id.avatar).setBackground(Ui.circle(Ui.withAlpha(navy, 0.1f)));
        name = v.findViewById(R.id.name);
        email = v.findViewById(R.id.email);
        guestCard = v.findViewById(R.id.guestCard);
        logoutButton = v.findViewById(R.id.logoutButton);

        ((com.google.android.material.card.MaterialCardView) guestCard).setCardBackgroundColor(
                Ui.withAlpha(ContextCompat.getColor(requireContext(), R.color.amber), 0.08f));

        LinearLayout tiles = v.findViewById(R.id.tiles);

        addTile(tiles, R.drawable.ic_report, "My reports",
                () -> startActivity(new Intent(requireContext(),
                        com.safezone.app.ui.report.MyReportsActivity.class)));
        addTile(tiles, R.drawable.ic_help, getString(R.string.help_title),
                () -> startActivity(new Intent(requireContext(), HelpActivity.class)));
        addTile(tiles, R.drawable.ic_notifications, "Notification settings", this::showNotificationSettings);
        addTile(tiles, R.drawable.ic_privacy_tip, getString(R.string.privacy_title),
                () -> startActivity(new Intent(requireContext(), PrivacyActivity.class)));

        logoutButton.setOnClickListener(x -> logout());
        ServiceLocator.auth().addListener(authListener);
        bind();
    }

    @Override
    public void onDestroyView() {
        ServiceLocator.auth().removeListener(authListener);
        super.onDestroyView();
    }

    private void bind() {
        if (!isAdded()) return;
        AppUser user = ServiceLocator.auth().getCurrentUser();
        boolean isGuest = user == null || user.isGuest;
        name.setText(isGuest ? "Guest" : user.name);
        email.setText(isGuest ? "" : user.email);
        email.setVisibility(isGuest ? View.GONE : View.VISIBLE);
        guestCard.setVisibility(isGuest ? View.VISIBLE : View.GONE);
        logoutButton.setText(isGuest ? "Log In / Register" : "Log Out");
    }

    private void addTile(LinearLayout parent, @DrawableRes int icon, String label, Runnable onClick) {
        View tile = getLayoutInflater().inflate(R.layout.item_profile_tile, parent, false);
        ((ImageView) tile.findViewById(R.id.icon)).setImageResource(icon);
        ((TextView) tile.findViewById(R.id.label)).setText(label);
        tile.setOnClickListener(x -> onClick.run());
        parent.addView(tile);
    }

    private void showNotificationSettings() {
        final NotificationService n = ServiceLocator.notifications();
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Notification settings")
                .setMultiChoiceItems(new String[]{"Push alerts for all of Nelson Mandela Bay"},
                        new boolean[]{n.isEnabled()}, (d, which, checked) -> n.setEnabled(checked))
                .setPositiveButton("Done", null)
                .show();
    }

    private void logout() {
        ServiceLocator.auth().logout();
        startActivity(new Intent(requireContext(), LoginActivity.class)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
    }
}
