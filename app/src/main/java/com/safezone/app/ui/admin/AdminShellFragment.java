package com.safezone.app.ui.admin;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.safezone.app.R;

public class AdminShellFragment extends Fragment {
    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater i, @Nullable ViewGroup c, @Nullable Bundle s) {
        return i.inflate(R.layout.fragment_admin_shell, c, false);
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle s) {
        View reports = v.findViewById(R.id.tabReports);
        View wanted = v.findViewById(R.id.tabWanted);

        if (s == null) {
            getChildFragmentManager().beginTransaction()
                    .replace(R.id.tabReports, new ModerationQueueFragment())
                    .replace(R.id.tabWanted, new WantedPersonsAdminFragment())
                    .commit();
        }
        BottomNavigationView nav = v.findViewById(R.id.adminNav);
        nav.setOnItemSelectedListener(item -> {
            boolean r = item.getItemId() == R.id.admin_nav_reports;
            View in = r ? reports : wanted, out = r ? wanted : reports;
            out.setVisibility(View.GONE);
            if (in.getVisibility() != View.VISIBLE) {
                in.setAlpha(0f);
                in.setVisibility(View.VISIBLE);
                in.animate().alpha(1f).setDuration(180).start();
            }
            return true;
        });
    }
}
