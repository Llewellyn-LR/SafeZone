package com.safezone.app.ui.report;

import android.content.res.ColorStateList;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.safezone.app.R;
import com.safezone.app.ServiceLocator;
import com.safezone.app.model.CrimeReport;
import com.safezone.app.model.ReportCategory;
import com.safezone.app.service.LocationService;
import com.safezone.app.service.ReportService;
import com.safezone.app.ui.common.ImageUtils;
import com.safezone.app.ui.common.Ui;

import java.io.File;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ReportFragment extends Fragment {
    private TextInputEditText area, description;
    private TextView errorText;
    private ChipGroup categoryGroup;
    private View gbvNote;
    private MaterialButton submitButton;
    private MaterialCardView attachButton, attachedCard;
    private View attachIcon, attachSpinner;
    private ImageView thumb;
    private View submitSpinner, myReportsLink;
    private TextView myReportsSummary;
    private ReportViewModel vm;

    private static final int MAX_WIDTH = 1600;
    private static final int JPEG_QUALITY = 85;
    private final ExecutorService io = Executors.newSingleThreadExecutor();

    private final ActivityResultLauncher<PickVisualMediaRequest> galleryLauncher =
            registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), this::onPicked);
    private final ActivityResultLauncher<Uri> cameraLauncher =
            registerForActivityResult(new ActivityResultContracts.TakePicture(), ok -> {
                ReportViewModel m = new ViewModelProvider(this).get(ReportViewModel.class);
                onPicked(Boolean.TRUE.equals(ok) ? m.getPendingCameraUri() : null);
            });

    private final ReportService.Listener reportsListener = this::renderMyReports;

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater i, @Nullable ViewGroup c, @Nullable Bundle s) {
        return i.inflate(R.layout.fragment_report, c, false);
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle s) {
        vm = new ViewModelProvider(this).get(ReportViewModel.class);

        int navy = ContextCompat.getColor(requireContext(), R.color.navy);
        v.findViewById(R.id.infoBox).setBackground(
                Ui.rounded(requireContext(), Ui.withAlpha(navy, 0.06f), 12));

        area = v.findViewById(R.id.areaField);
        description = v.findViewById(R.id.descriptionField);
        errorText = v.findViewById(R.id.errorText);
        categoryGroup = v.findViewById(R.id.categoryGroup);
        gbvNote = v.findViewById(R.id.gbvNote);
        attachButton = v.findViewById(R.id.attachButton);
        attachedCard = v.findViewById(R.id.attachedCard);
        attachIcon = v.findViewById(R.id.attachIcon);
        attachSpinner = v.findViewById(R.id.attachSpinner);
        thumb = v.findViewById(R.id.thumb);
        submitButton = v.findViewById(R.id.submitButton);
        submitSpinner = v.findViewById(R.id.submitSpinner);
        myReportsLink = v.findViewById(R.id.myReportsLink);
        myReportsSummary = v.findViewById(R.id.myReportsSummary);
        myReportsLink.setOnClickListener(x ->
                startActivity(new android.content.Intent(requireContext(), MyReportsActivity.class)));

        attachButton.setOnClickListener(x -> {
            if (!Boolean.TRUE.equals(vm.picking().getValue())) pickPhoto();
        });
        v.findViewById(R.id.removePhoto).setOnClickListener(x -> vm.removePhoto());
        submitButton.setOnClickListener(x -> submit());

        buildCategories();
        vm.category().observe(getViewLifecycleOwner(), c -> buildCategories());
        vm.photo().observe(getViewLifecycleOwner(), uri -> refreshPhotoUi(uri));
        vm.picking().observe(getViewLifecycleOwner(), busy -> {
            boolean b = Boolean.TRUE.equals(busy);
            attachIcon.setVisibility(b ? View.INVISIBLE : View.VISIBLE);
            attachSpinner.setVisibility(b ? View.VISIBLE : View.GONE);
            attachButton.setAlpha(b ? 0.5f : 1f);
        });
        vm.submitting().observe(getViewLifecycleOwner(), busy ->
                Ui.setButtonLoading(submitButton, submitSpinner, "Submit Report", Boolean.TRUE.equals(busy)));
        vm.error().observe(getViewLifecycleOwner(), message -> {
            errorText.setText(message);
            errorText.setVisibility(message == null ? View.GONE : View.VISIBLE);
        });
        vm.submitted().observe(getViewLifecycleOwner(), event -> {
            if (event != null && event.consume() != null) {
                area.setText("");
                description.setText("");
                showSubmittedDialog();
            }
        });

        ServiceLocator.reports().addListener(reportsListener);
        renderMyReports();
    }

    @Override
    public void onDestroy() {
        io.shutdown();
        super.onDestroy();
    }

    @Override
    public void onDestroyView() {
        ServiceLocator.reports().removeListener(reportsListener);
        super.onDestroyView();
    }

    private void buildCategories() {
        categoryGroup.removeAllViews();
        ReportCategory selected = vm.getCategory();
        gbvNote.setVisibility(selected == ReportCategory.GBV ? View.VISIBLE : View.GONE);
        for (ReportCategory category : ReportCategory.values()) {
            boolean isSelected = selected == category;
            int navy = ContextCompat.getColor(requireContext(), R.color.navy);
            int fg = isSelected ? navy : ContextCompat.getColor(requireContext(), R.color.text_secondary);

            LinearLayout chip = new LinearLayout(requireContext());
            chip.setOrientation(LinearLayout.HORIZONTAL);
            chip.setGravity(Gravity.CENTER_VERTICAL);
            chip.setMinimumHeight(Ui.dp(requireContext(), 48));
            chip.setPadding(Ui.dp(requireContext(), 12), Ui.dp(requireContext(), 8),
                    Ui.dp(requireContext(), 12), Ui.dp(requireContext(), 8));
            chip.setBackground(Ui.stroked(requireContext(),
                    isSelected ? Ui.withAlpha(navy, 0.1f)
                            : ContextCompat.getColor(requireContext(), R.color.surface),
                    isSelected ? navy : ContextCompat.getColor(requireContext(), R.color.outline_grey),
                    isSelected ? 1.4f : 1f, 12));
            chip.setSelected(isSelected);

            ImageView icon = new ImageView(requireContext());
            icon.setImageResource(category.iconRes);
            icon.setImageTintList(ColorStateList.valueOf(fg));
            icon.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
            icon.setLayoutParams(new LinearLayout.LayoutParams(
                    Ui.dp(requireContext(), 17), Ui.dp(requireContext(), 17)));
            chip.addView(icon);

            TextView label = new TextView(requireContext());
            label.setText(category.label);
            label.setTextSize(13);
            label.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
            label.setTextColor(isSelected ? navy
                    : ContextCompat.getColor(requireContext(), R.color.text_primary));
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            lp.setMarginStart(Ui.dp(requireContext(), 6));
            chip.addView(label, lp);

            chip.setOnClickListener(x -> vm.selectCategory(category));
            categoryGroup.addView(chip);
        }
    }

    private void refreshPhotoUi(@Nullable Uri uri) {
        boolean has = uri != null;
        attachButton.setVisibility(has ? View.GONE : View.VISIBLE);
        attachedCard.setVisibility(has ? View.VISIBLE : View.GONE);
        if (has) Glide.with(this).load(uri).centerCrop().into(thumb);
    }

    private void pickPhoto() {
        BottomSheetDialog sheet = new BottomSheetDialog(requireContext());
        View content = getLayoutInflater().inflate(R.layout.sheet_photo_source, null);
        content.findViewById(R.id.optCamera).setOnClickListener(x -> { sheet.dismiss(); launchCamera(); });
        content.findViewById(R.id.optGallery).setOnClickListener(x -> {
            sheet.dismiss();
            vm.setPicking(true);
            galleryLauncher.launch(new PickVisualMediaRequest.Builder()
                    .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE).build());
        });
        sheet.setContentView(content);
        sheet.show();
    }

    private void launchCamera() {
        vm.setPicking(true);
        try {
            File dir = new File(requireContext().getCacheDir(), "images");

            dir.mkdirs();
            File f = File.createTempFile("report_", ".jpg", dir);
            Uri uri = FileProvider.getUriForFile(requireContext(),
                    requireContext().getPackageName() + ".fileprovider", f);
            vm.setPendingCameraUri(uri);
            cameraLauncher.launch(uri);
        } catch (Exception e) {
            vm.setPicking(false);
            vm.setError("Couldn't access camera/gallery: " + e.getMessage());
        }
    }

    private void onPicked(@Nullable Uri src) {
        final ReportViewModel viewModel = new ViewModelProvider(this).get(ReportViewModel.class);
        if (src == null) { viewModel.setPicking(false); return; }
        final android.content.Context app = requireContext().getApplicationContext();
        io.execute(() -> {
            try {
                File prepared = ImageUtils.prepareForUpload(app, src, MAX_WIDTH, JPEG_QUALITY);
                viewModel.postPhoto(Uri.fromFile(prepared));
            } catch (Exception e) {
                viewModel.postPickError("Couldn't access camera/gallery: " + e.getMessage());
            }
        });
    }

    private void submit() {
        final String areaText = text(area);
        final String descriptionText = text(description);
        if (!vm.begin(areaText, descriptionText)) return;

        final ReportViewModel viewModel = vm;
        ServiceLocator.location().getCurrentLocation(requireContext(),
                (LocationService.PermissionHost) requireActivity(),
                loc -> viewModel.send(areaText, descriptionText, loc));
    }

    private void showSubmittedDialog() {
        View content = getLayoutInflater().inflate(R.layout.dialog_report_submitted, null);
        int green = ContextCompat.getColor(requireContext(), R.color.green);
        content.findViewById(R.id.icon).setBackground(Ui.circle(Ui.withAlpha(green, 0.12f)));
        androidx.appcompat.app.AlertDialog dialog = new MaterialAlertDialogBuilder(requireContext())
                .setView(content).create();
        content.findViewById(R.id.done).setOnClickListener(x -> dialog.dismiss());
        dialog.show();
    }

    private static String text(TextInputEditText f) {
        return f.getText() == null ? "" : f.getText().toString().trim();
    }

    private void renderMyReports() {
        if (!isAdded()) return;
        List<CrimeReport> mine = ServiceLocator.reports().getMyReports();
        myReportsLink.setVisibility(mine.isEmpty() ? View.GONE : View.VISIBLE);
        int waiting = MyReportTiles.countAwaitingAnswer(mine);
        String text = mine.size() + (mine.size() == 1 ? " report submitted" : " reports submitted");
        if (waiting > 0) {
            text += " \u00b7 " + waiting + (waiting == 1 ? " needs" : " need") + " your answer";
        }
        myReportsSummary.setText(text);
        myReportsSummary.setTextColor(ContextCompat.getColor(requireContext(),
                waiting > 0 ? R.color.amber_text : R.color.text_secondary));
    }
}
