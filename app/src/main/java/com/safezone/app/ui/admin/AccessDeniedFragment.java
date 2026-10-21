package com.safezone.app.ui.admin;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.safezone.app.R;
import com.safezone.app.ui.common.Ui;

public class AccessDeniedFragment extends Fragment {
    public static final String DEFAULT_MESSAGE =
            "This account isn't registered as a SafeZone moderator yet. Contact an administrator to be added.";

    public static AccessDeniedFragment newInstance(String message, boolean showSignOut) {
        AccessDeniedFragment f = new AccessDeniedFragment();
        Bundle a = new Bundle();
        a.putString("message", message);
        a.putBoolean("signOut", showSignOut);
        f.setArguments(a);
        return f;
    }

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater i, @Nullable ViewGroup c, @Nullable Bundle s) {
        return i.inflate(R.layout.fragment_access_denied, c, false);
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle s) {
        v.findViewById(R.id.card).setBackground(Ui.rounded(requireContext(),
                ContextCompat.getColor(requireContext(), R.color.surface), 20));
        int coral = ContextCompat.getColor(requireContext(), R.color.coral);
        ImageView icon = v.findViewById(R.id.icon);
        icon.setBackground(Ui.rounded(requireContext(), Ui.withAlpha(coral, 0.12f), 12));
        ((TextView) v.findViewById(R.id.message)).setText(requireArguments().getString("message"));
        View btn = v.findViewById(R.id.signOut);
        btn.setVisibility(requireArguments().getBoolean("signOut") ? View.VISIBLE : View.GONE);
        btn.setOnClickListener(x -> FirebaseAuth.getInstance().signOut());
    }
}
