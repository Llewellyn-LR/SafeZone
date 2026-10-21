package com.safezone.app.ui.alerts;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.ColorRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.snackbar.Snackbar;
import com.safezone.app.R;
import com.safezone.app.ServiceLocator;
import com.safezone.app.model.CrimeHotspot;
import com.safezone.app.model.HotspotSeverity;
import com.safezone.app.service.DataListener;
import com.safezone.app.service.NearbyFilter;
import com.safezone.app.service.NotificationService;
import com.safezone.app.util.RadiusFilter;
import com.safezone.app.service.Subscription;
import com.safezone.app.ui.common.HotspotDetailsSheet;
import com.safezone.app.ui.common.StreamErrorView;
import com.safezone.app.ui.common.Ui;

import java.util.ArrayList;
import java.util.List;

public class AlertsFragment extends Fragment {
    private AlertsViewModel vm;
    private List<CrimeHotspot> alerts;
    private boolean hasError;
    private Subscription subscription;

    private View progress, emptyText;
    private StreamErrorView errorView;
    private RecyclerView recycler;
    private MenuItem bell;
    private LinearLayout chipRow;
    private final Adapter adapter = new Adapter();
    private final NotificationService.Listener notifListener = this::updateBell;
    private final NearbyFilter.Listener nearbyListener = () -> { if (isAdded()) render(); };
    private TextView radiusNote;

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater i, @Nullable ViewGroup c, @Nullable Bundle s) {
        return i.inflate(R.layout.fragment_alerts, c, false);
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle s) {
        vm = new ViewModelProvider(this).get(AlertsViewModel.class);
        MaterialToolbar tb = v.findViewById(R.id.toolbar);
        bell = tb.getMenu().findItem(R.id.action_push_toggle);
        bell.setOnMenuItemClickListener(item -> { toggleNotifications(); return true; });
        updateBell();
        ServiceLocator.notifications().addListener(notifListener);
        ServiceLocator.nearby().addListener(nearbyListener);
        radiusNote = v.findViewById(R.id.radiusNote);

        progress = v.findViewById(R.id.progress);
        emptyText = v.findViewById(R.id.emptyText);
        errorView = v.findViewById(R.id.errorView);
        recycler = v.findViewById(R.id.recycler);
        chipRow = v.findViewById(R.id.chipRow);
        recycler.setLayoutManager(new LinearLayoutManager(requireContext()));
        recycler.setAdapter(adapter);
        buildChips();

        subscription = ServiceLocator.hotspots().watchHotspots(new DataListener<List<CrimeHotspot>>() {
            @Override public void onData(List<CrimeHotspot> data) {
                alerts = data;
                hasError = false;
                render();
            }
            @Override public void onError(Exception e) {
                hasError = true;
                render();
            }
        });
        render();
    }

    @Override
    public void onDestroyView() {
        if (subscription != null) subscription.cancel();
        ServiceLocator.notifications().removeListener(notifListener);
        ServiceLocator.nearby().removeListener(nearbyListener);
        super.onDestroyView();
    }

    private void toggleNotifications() {
        NotificationService n = ServiceLocator.notifications();
        boolean next = !n.isEnabled();
        n.setEnabled(next);
        Snackbar.make(requireView(),
                next ? "Push alerts turned on for your area" : "Push alerts turned off",
                2000).show();
    }

    private void updateBell() {
        if (bell == null) return;
        boolean on = ServiceLocator.notifications().isEnabled();
        bell.setIcon(on ? R.drawable.ic_notifications_active : R.drawable.ic_notifications_off);
        bell.setTooltipText(on ? "Notifications on" : "Notifications off");
    }

    private void buildChips() {
        chipRow.removeAllViews();
        addChip("All", R.color.navy, R.color.navy, null);
        addChip("High", R.color.coral, R.color.coral_text, HotspotSeverity.HIGH);
        addChip("Medium", R.color.amber, R.color.amber_text, HotspotSeverity.MEDIUM);
        addChip("Low", R.color.green, R.color.green_text, HotspotSeverity.LOW);
    }

    private void addChip(String label, @ColorRes int colorRes, @ColorRes int textColorRes,
                         HotspotSeverity value) {
        boolean selected = vm.getFilter() == value;
        int chipColor = ContextCompat.getColor(requireContext(), colorRes);
        int chipTextColor = ContextCompat.getColor(requireContext(), textColorRes);
        TextView chip = new TextView(requireContext());
        chip.setText(label);
        chip.setTextSize(13);
        chip.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
        chip.setGravity(android.view.Gravity.CENTER_VERTICAL);
        chip.setMinHeight(Ui.dp(requireContext(), 48));
        chip.setSelected(selected);
        chip.setPadding(Ui.dp(requireContext(), 14), Ui.dp(requireContext(), 8),
                Ui.dp(requireContext(), 14), Ui.dp(requireContext(), 8));
        chip.setTextColor(selected ? chipTextColor
                : ContextCompat.getColor(requireContext(), R.color.text_secondary));
        chip.setBackground(Ui.stroked(requireContext(),
                selected ? Ui.withAlpha(chipColor, 0.12f)
                        : ContextCompat.getColor(requireContext(), R.color.surface),
                selected ? chipColor : ContextCompat.getColor(requireContext(), R.color.outline_grey),
                selected ? 1.4f : 1f, 20));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        if (chipRow.getChildCount() > 0) lp.setMarginStart(Ui.dp(requireContext(), 8));
        chip.setLayoutParams(lp);
        chip.setOnClickListener(x -> {
            vm.setFilter(value);
            buildChips();
            render();
        });
        chipRow.addView(chip);
    }

    private void render() {
        if (!isAdded()) return;
        if (hasError) {
            progress.setVisibility(View.GONE);
            recycler.setVisibility(View.GONE);
            emptyText.setVisibility(View.GONE);
            errorView.setVisibility(View.VISIBLE);
            return;
        }
        errorView.setVisibility(View.GONE);
        if (alerts == null) {
            progress.setVisibility(View.VISIBLE);
            return;
        }
        progress.setVisibility(View.GONE);
        List<CrimeHotspot> visible = new ArrayList<>();
        NearbyFilter nearby = ServiceLocator.nearby();
        radiusNote.setText(nearby.getLocation() == null || RadiusFilter.isAll(nearby.getRadiusKm())
                ? "Showing alerts across all of Nelson Mandela Bay"
                : "Showing alerts within " + Math.round(nearby.getRadiusKm())
                        + " km of you. Change this on the Map tab.");
        for (CrimeHotspot a : nearby.apply(alerts)) if (vm.getFilter() == null || a.severity == vm.getFilter()) visible.add(a);
        adapter.submit(visible);
        emptyText.setVisibility(visible.isEmpty() ? View.VISIBLE : View.GONE);
        recycler.setVisibility(visible.isEmpty() ? View.GONE : View.VISIBLE);
    }

    private final class Adapter extends RecyclerView.Adapter<Adapter.VH> {
        private final List<CrimeHotspot> items = new ArrayList<>();

        void submit(List<CrimeHotspot> l) { items.clear(); items.addAll(l); notifyDataSetChanged(); }

        @NonNull @Override
        public VH onCreateViewHolder(@NonNull ViewGroup p, int t) {
            return new VH(LayoutInflater.from(p.getContext()).inflate(R.layout.item_alert_list, p, false));
        }

        @Override
        public void onBindViewHolder(@NonNull VH h, int pos) {
            CrimeHotspot a = items.get(pos);
            android.content.Context c = h.itemView.getContext();
            h.bar.setBackground(Ui.rounded(c, ContextCompat.getColor(c, a.severity.colorRes), 4));
            h.title.setText(a.title);
            h.pill.setBackground(Ui.rounded(c, Ui.withAlpha(
                    ContextCompat.getColor(c, R.color.text_secondary), 0.1f), 20));
            h.pill.setVisibility(a.verified ? View.GONE : View.VISIBLE);
            h.meta.setText(a.area + " · " + a.timeAgo);
            h.itemView.setOnClickListener(x -> HotspotDetailsSheet.show(c, a));
        }

        @Override public int getItemCount() { return items.size(); }

        final class VH extends RecyclerView.ViewHolder {
            final View bar, pill;
            final TextView title, meta;
            VH(@NonNull View v) {
                super(v);
                bar = v.findViewById(R.id.bar);
                pill = v.findViewById(R.id.unverifiedPill);
                title = v.findViewById(R.id.title);
                meta = v.findViewById(R.id.meta);
            }
        }
    }
}
