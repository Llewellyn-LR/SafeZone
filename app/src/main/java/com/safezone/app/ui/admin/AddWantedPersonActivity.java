package com.safezone.app.ui.admin;

import android.content.res.ColorStateList;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.bumptech.glide.Glide;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.safezone.app.R;
import com.safezone.app.ServiceLocator;
import com.safezone.app.ui.common.ImageUtils;
import com.safezone.app.ui.common.Ui;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AddWantedPersonActivity extends AppCompatActivity {
    private static final int MAX_WIDTH = 1600;
    private static final int JPEG_QUALITY = 85;

    private final ExecutorService io = Executors.newSingleThreadExecutor();

    private TextInputEditText nameField, aliasField, wantedForField, caseRefField, areaField;
    private MaterialSwitch armedSwitch;
    private MaterialCardView attachButton, attachedCard;
    private View attachIcon, attachSpinner;
    private ImageView thumb;
    private TextView errorText;
    private MaterialButton publishButton;
    private View publishSpinner;

    private Uri pickedPhoto;
    private boolean isPickingPhoto, isSubmitting;
    private Uri pendingCameraUri;

    private ActivityResultLauncher<PickVisualMediaRequest> galleryLauncher;
    private ActivityResultLauncher<Uri> cameraLauncher;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_wanted);

        MaterialToolbar tb = findViewById(R.id.toolbar);
        tb.setNavigationOnClickListener(v -> finish());

        int navy = ContextCompat.getColor(this, R.color.navy);
        findViewById(R.id.infoBox).setBackground(Ui.rounded(this, Ui.withAlpha(navy, 0.06f), 12));

        nameField = findViewById(R.id.nameField);
        aliasField = findViewById(R.id.aliasField);
        wantedForField = findViewById(R.id.wantedForField);
        caseRefField = findViewById(R.id.caseRefField);
        areaField = findViewById(R.id.areaField);
        armedSwitch = findViewById(R.id.armedSwitch);
        attachButton = findViewById(R.id.attachButton);
        attachedCard = findViewById(R.id.attachedCard);
        attachIcon = findViewById(R.id.attachIcon);
        attachSpinner = findViewById(R.id.attachSpinner);
        thumb = findViewById(R.id.thumb);
        errorText = findViewById(R.id.errorText);
        publishButton = findViewById(R.id.publishButton);
        publishSpinner = findViewById(R.id.publishSpinner);

        galleryLauncher = registerForActivityResult(
                new ActivityResultContracts.PickVisualMedia(), uri -> onPicked(uri));
        cameraLauncher = registerForActivityResult(
                new ActivityResultContracts.TakePicture(),
                ok -> onPicked(Boolean.TRUE.equals(ok) ? pendingCameraUri : null));

        attachButton.setOnClickListener(v -> { if (!isPickingPhoto) pickPhoto(); });
        findViewById(R.id.removePhoto).setOnClickListener(v -> {
            pickedPhoto = null;
            refreshPhotoUi();
        });
        publishButton.setOnClickListener(v -> submit());
        refreshPhotoUi();
    }

    @Override
    protected void onDestroy() {
        io.shutdown();
        super.onDestroy();
    }

    private void pickPhoto() {
        BottomSheetDialog sheet = new BottomSheetDialog(this);
        View content = getLayoutInflater().inflate(R.layout.sheet_photo_source, null);
        content.findViewById(R.id.optCamera).setOnClickListener(v -> { sheet.dismiss(); launchCamera(); });
        content.findViewById(R.id.optGallery).setOnClickListener(v -> {
            sheet.dismiss();
            setPicking(true);
            galleryLauncher.launch(new PickVisualMediaRequest.Builder()
                    .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE).build());
        });
        sheet.setContentView(content);
        sheet.show();
    }

    private void launchCamera() {
        setPicking(true);
        try {
            File dir = new File(getCacheDir(), "images");

            dir.mkdirs();
            File f = File.createTempFile("capture_", ".jpg", dir);
            pendingCameraUri = FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", f);
            cameraLauncher.launch(pendingCameraUri);
        } catch (Exception e) {
            setPicking(false);
            showError("Couldn't access camera/gallery: " + e.getMessage());
        }
    }

    private void onPicked(@Nullable Uri src) {
        if (src == null) { setPicking(false); return; }
        io.execute(() -> {
            try {
                File prepared = ImageUtils.prepareForUpload(this, src, MAX_WIDTH, JPEG_QUALITY);
                runOnUiThread(() -> {
                    if (isDestroyed()) return;
                    pickedPhoto = Uri.fromFile(prepared);
                    errorText.setVisibility(View.GONE);
                    setPicking(false);
                    refreshPhotoUi();
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    if (isDestroyed()) return;
                    setPicking(false);
                    showError("Couldn't access camera/gallery: " + e.getMessage());
                });
            }
        });
    }

    private void setPicking(boolean picking) {
        isPickingPhoto = picking;
        attachIcon.setVisibility(picking ? View.INVISIBLE : View.VISIBLE);
        attachSpinner.setVisibility(picking ? View.VISIBLE : View.GONE);
        attachButton.setAlpha(picking ? 0.5f : 1f);
    }

    private void refreshPhotoUi() {
        boolean has = pickedPhoto != null;
        attachButton.setVisibility(has ? View.GONE : View.VISIBLE);
        attachedCard.setVisibility(has ? View.VISIBLE : View.GONE);
        if (has) Glide.with(this).load(pickedPhoto).centerCrop().into(thumb);
    }

    private void submit() {
        if (isSubmitting) return;
        String name = text(nameField), wantedFor = text(wantedForField), area = text(areaField);
        if (name.isEmpty()) {
            showError("Please enter a name (or \"Unknown Male\"/\"Unknown Female\").");
            return;
        }
        if (wantedFor.isEmpty()) { showError("Please describe what they are wanted for."); return; }
        if (area.isEmpty()) { showError("Please enter a last-seen area."); return; }

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) { showError("Your session expired — please sign in again."); return; }

        setSubmitting(true);
        errorText.setVisibility(View.GONE);

        String alias = text(aliasField), caseRef = text(caseRefField);
        ServiceLocator.firebaseWantedPersons(this).publishWantedPerson(
                name,
                alias.isEmpty() ? null : alias,
                wantedFor,
                caseRef.isEmpty() ? null : caseRef,
                area,
                armedSwitch.isChecked(),
                pickedPhoto,
                user.getUid())
                .addOnSuccessListener(x -> { setResult(RESULT_OK); finish(); })
                .addOnFailureListener(e -> {
                    setSubmitting(false);
                    showError("Couldn't publish: " + e.getMessage());
                });
    }

    private void setSubmitting(boolean v) {
        isSubmitting = v;
        Ui.setButtonLoading(publishButton, publishSpinner, "Publish", v);
    }

    private void showError(String m) {
        errorText.setText(m);
        errorText.setVisibility(View.VISIBLE);
    }

    private static String text(TextInputEditText f) {
        return f.getText() == null ? "" : f.getText().toString().trim();
    }
}
