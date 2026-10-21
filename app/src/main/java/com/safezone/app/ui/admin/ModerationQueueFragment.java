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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.safezone.app.R;
import com.safezone.app.ServiceLocator;
import com.safezone.app.model.CrimeReport;
import com.safezone.app.service.DataListener;
import com.safezone.app.service.FirebaseReportService;
import com.safezone.app.service.Subscription;
import com.safezone.app.ui.common.Ui;

import java.util.ArrayList;
import java.util.List;

public class ModerationQueueFragment extends Fragment {
    private FirebaseReportService reports;
    private Subscription subscription;
    private View progress, empty;
    private TextView errorText;
    private RecyclerView recycler;
    private final Adapter adapter = new Adapter();

    private java.util.function.Consumer<android.net.Uri> proofCallback;
    private final java.util.concurrent.ExecutorService io =
            java.util.concurrent.Executors.newSingleThreadExecutor();
    private final androidx.activity.result.ActivityResultLauncher<
            androidx.activity.result.PickVisualMediaRequest> proofLauncher =
            registerForActivityResult(
                    new androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia(),
                    this::onProofPicked);

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater i, @Nullable ViewGroup c, @Nullable Bundle s) {
        return i.inflate(R.layout.fragment_moderation_queue, c, false);
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle s) {
        reports = ServiceLocator.firebaseReports();

        MaterialToolbar tb = v.findViewById(R.id.toolbar);
        tb.setOnMenuItemClickListener(item -> {
            FirebaseAuth.getInstance().signOut();
            return true;
        });
        progress = v.findViewById(R.id.progress);
        empty = v.findViewById(R.id.empty);
        errorText = v.findViewById(R.id.errorText);
        recycler = v.findViewById(R.id.recycler);
        recycler.setLayoutManager(new LinearLayoutManager(requireContext()));
        recycler.setAdapter(adapter);

        subscription = reports.watchPendingReports(new DataListener<List<CrimeReport>>() {
            @Override public void onData(List<CrimeReport> data) {
                progress.setVisibility(View.GONE);
                errorText.setVisibility(View.GONE);
                adapter.submit(data);
                empty.setVisibility(data.isEmpty() ? View.VISIBLE : View.GONE);
                recycler.setVisibility(data.isEmpty() ? View.GONE : View.VISIBLE);
            }
            @Override public void onError(Exception e) {
                progress.setVisibility(View.GONE);
                recycler.setVisibility(View.GONE);
                empty.setVisibility(View.GONE);
                errorText.setText("Couldn't load the queue: " + e.getMessage());
                errorText.setVisibility(View.VISIBLE);
            }
        });
    }

    @Override
    public void onDestroy() {
        io.shutdown();
        super.onDestroy();
    }

    @Override
    public void onDestroyView() {
        if (subscription != null) subscription.cancel();
        super.onDestroyView();
    }

    private void approve(CrimeReport report) {
        View root = requireView();
        ApproveReportDialog.show(requireContext(), report, decision ->
                reports.approveReport(report, decision.severity, decision.latitude, decision.longitude)
                        .addOnSuccessListener(x -> Snackbar.make(root,
                                "Report approved and published.", Snackbar.LENGTH_SHORT).show())
                        .addOnFailureListener(e -> Snackbar.make(root,
                                "Could not approve: " + e.getMessage(), Snackbar.LENGTH_LONG).show()));
    }

    private void reject(CrimeReport report) {
        View root = requireView();
        androidx.appcompat.app.AlertDialog d = new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Reject report?")
                .setMessage("This report won't be published. It stays on file for audit "
                        + "purposes but never appears on the Map or Alerts tabs.")
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Reject", (dlg, w) ->
                        reports.rejectReport(report)
                                .addOnSuccessListener(x -> Snackbar.make(root,
                                        "Report rejected.", Snackbar.LENGTH_SHORT).show())
                                .addOnFailureListener(e -> Snackbar.make(root,
                                        "Could not reject: " + e.getMessage(),
                                        Snackbar.LENGTH_LONG).show()))
                .create();
        d.show();
        d.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE)
                .setTextColor(ContextCompat.getColor(requireContext(), R.color.coral_text));
    }

    private void markSolved(CrimeReport report) {
        View root = requireView();
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Mark as solved?")
                .setMessage("The reporter will be asked to confirm. If they say it isn't solved, "
                        + "the case comes back to this queue with only \"Solved\" available.")
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Solved", (dlg, w) ->
                        reports.markSolved(report)
                                .addOnSuccessListener(x -> Snackbar.make(root,
                                        "Marked solved — waiting for the reporter to confirm.",
                                        Snackbar.LENGTH_LONG).show())
                                .addOnFailureListener(e -> Snackbar.make(root,
                                        "Could not update: " + e.getMessage(),
                                        Snackbar.LENGTH_LONG).show()))
                .show();
    }

    private void dismiss(CrimeReport report) {
        View root = requireView();
        DismissReportDialog.show(requireContext(), report, this::pickProof, decision -> {
            Snackbar.make(root, "Dismissing…", Snackbar.LENGTH_SHORT).show();
            com.google.firebase.auth.FirebaseUser u = FirebaseAuth.getInstance().getCurrentUser();
            reports.dismissReport(report, decision.reason, decision.proof, u != null ? u.getUid() : null)
                    .addOnSuccessListener(x -> Snackbar.make(root,
                            "Report dismissed. The reporter will see it as rejected.",
                            Snackbar.LENGTH_LONG).show())
                    .addOnFailureListener(e -> Snackbar.make(root,
                            "Could not dismiss: " + e.getMessage(), Snackbar.LENGTH_LONG).show());
        });
    }

    private void pickProof(java.util.function.Consumer<android.net.Uri> onPicked) {
        proofCallback = onPicked;
        proofLauncher.launch(new androidx.activity.result.PickVisualMediaRequest.Builder()
                .setMediaType(androidx.activity.result.contract.ActivityResultContracts
                        .PickVisualMedia.ImageOnly.INSTANCE).build());
    }

    private void onProofPicked(@Nullable android.net.Uri src) {
        final java.util.function.Consumer<android.net.Uri> cb = proofCallback;
        proofCallback = null;
        if (src == null || cb == null || !isAdded()) return;
        final android.content.Context app = requireContext().getApplicationContext();
        final View root = requireView();
        io.execute(() -> {
            try {
                java.io.File f = com.safezone.app.ui.common.ImageUtils.prepareForUpload(app, src, 1600, 85);
                root.post(() -> cb.accept(android.net.Uri.fromFile(f)));
            } catch (Exception e) {
                root.post(() -> Snackbar.make(root, "Couldn't use that photo: " + e.getMessage(),
                        Snackbar.LENGTH_LONG).show());
            }
        });
    }

    private static String waitingLabel(long millis) {
        long minutes = millis / 60_000L, hours = millis / 3_600_000L, days = millis / 86_400_000L;
        if (minutes < 1) return "Just now";
        if (minutes < 60) return minutes + "m waiting";
        if (hours < 24) return hours + "h waiting";
        return days + "d waiting";
    }

    private final class Adapter extends RecyclerView.Adapter<Adapter.VH> {
        private final List<CrimeReport> items = new ArrayList<>();

        void submit(List<CrimeReport> l) { items.clear(); items.addAll(l); notifyDataSetChanged(); }

        @NonNull @Override
        public VH onCreateViewHolder(@NonNull ViewGroup p, int t) {
            return new VH(LayoutInflater.from(p.getContext()).inflate(R.layout.item_report_review, p, false));
        }

        @Override
        public void onBindViewHolder(@NonNull VH h, int pos) {
            CrimeReport r = items.get(pos);
            android.content.Context c = h.itemView.getContext();
            boolean hasLocation = r.latitude != null && r.longitude != null;

            h.icon.setImageResource(r.category.iconRes);
            h.title.setText(r.category.label);
            h.waiting.setText(waitingLabel(System.currentTimeMillis() - r.submittedAt.getTime()));
            h.area.setText(r.area);
            h.description.setText(r.description);
            boolean showPhoto = r.photoUrl != null && r.photoUrl.startsWith("http");
            h.photo.setVisibility(showPhoto ? View.VISIBLE : View.GONE);
            if (showPhoto) com.bumptech.glide.Glide.with(h.photo).load(r.photoUrl).centerCrop().into(h.photo);
            tag(h.photoTag, h.photoTagText, r.hasPhoto, r.hasPhoto ? "Photo attached" : "No photo");
            tag(h.gpsTag, h.gpsTagText, hasLocation, hasLocation ? "Location captured" : "No location");
            com.google.android.material.button.MaterialButton approveBtn =
                    (com.google.android.material.button.MaterialButton) h.approve;
            com.google.android.material.button.MaterialButton rejectBtn =
                    (com.google.android.material.button.MaterialButton) h.reject;
            if (r.isGbv()) {

                h.privateNote.setVisibility(View.VISIBLE);
                h.privateNote.setText(r.status == com.safezone.app.model.ReportStatus.FOLLOW_UP
                        ? "Reopened — the reporter said this was NOT resolved. Mark it solved again once it is."
                        : "Sensitive report — GBV reports are never published to the map or alerts.");
                approveBtn.setText("Solved");
                rejectBtn.setText("Dismiss");
                rejectBtn.setVisibility(r.canDismiss() ? View.VISIBLE : View.GONE);
                approveBtn.setOnClickListener(x -> markSolved(r));
                rejectBtn.setOnClickListener(x -> dismiss(r));
            } else {
                h.privateNote.setVisibility(View.GONE);
                approveBtn.setText("Approve");
                rejectBtn.setText("Reject");
                rejectBtn.setVisibility(View.VISIBLE);
                approveBtn.setOnClickListener(x -> approve(r));
                rejectBtn.setOnClickListener(x -> reject(r));
            }

        }

        private void tag(View pill, TextView text, boolean active, String label) {
            android.content.Context c = pill.getContext();
            int color = ContextCompat.getColor(c, active ? R.color.green : R.color.text_secondary);
            pill.setBackground(Ui.rounded(c, Ui.withAlpha(color, 0.1f), 20));
            text.setText(label);
            text.setTextColor(ContextCompat.getColor(c, active ? R.color.green_text : R.color.text_secondary));
            for (int i = 0; i < ((ViewGroup) pill).getChildCount(); i++) {
                View child = ((ViewGroup) pill).getChildAt(i);
                if (child instanceof ImageView) {
                    ((ImageView) child).setImageTintList(android.content.res.ColorStateList.valueOf(color));
                }
            }
        }

        @Override public int getItemCount() { return items.size(); }

        final class VH extends RecyclerView.ViewHolder {
            final ImageView icon, photo;
            final TextView title, waiting, area, description, photoTagText, gpsTagText;
            final View photoTag, gpsTag, approve, reject;
            final TextView privateNote;
            VH(@NonNull View v) {
                super(v);
                icon = v.findViewById(R.id.icon);
                photo = v.findViewById(R.id.photo);
                title = v.findViewById(R.id.title);
                waiting = v.findViewById(R.id.waiting);
                area = v.findViewById(R.id.area);
                description = v.findViewById(R.id.description);
                photoTag = v.findViewById(R.id.photoTag);
                photoTagText = v.findViewById(R.id.photoTagText);
                gpsTag = v.findViewById(R.id.gpsTag);
                gpsTagText = v.findViewById(R.id.gpsTagText);
                approve = v.findViewById(R.id.approve);
                reject = v.findViewById(R.id.reject);
                privateNote = v.findViewById(R.id.privateNote);
            }
        }
    }
}
