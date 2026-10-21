package com.safezone.app.ui.admin;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;

import androidx.appcompat.app.AlertDialog;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.safezone.app.R;
import com.safezone.app.model.CrimeReport;

import java.util.function.Consumer;

final class DismissReportDialog {
    private DismissReportDialog() {}

    interface ProofPicker { void pick(Consumer<Uri> onPicked); }

    static final class Decision {
        final String reason;
        final Uri proof;
        Decision(String reason, Uri proof) { this.reason = reason; this.proof = proof; }
    }

    static void show(Context ctx, CrimeReport report, ProofPicker picker, Consumer<Decision> onDecision) {
        View v = LayoutInflater.from(ctx).inflate(R.layout.dialog_dismiss_report, null);
        TextInputLayout reasonLayout = v.findViewById(R.id.reasonLayout);
        TextInputEditText reasonField = v.findViewById(R.id.reasonField);
        View proofName = v.findViewById(R.id.proofName);
        View removeProof = v.findViewById(R.id.removeProof);
        com.google.android.material.button.MaterialButton attach = v.findViewById(R.id.attachProof);
        final Uri[] proof = {null};

        Runnable refreshProof = () -> {
            boolean has = proof[0] != null;
            proofName.setVisibility(has ? View.VISIBLE : View.GONE);
            removeProof.setVisibility(has ? View.VISIBLE : View.GONE);
            attach.setText(has ? "Change proof" : "Attach proof (optional)");
        };
        attach.setOnClickListener(x -> picker.pick(uri -> { proof[0] = uri; refreshProof.run(); }));
        removeProof.setOnClickListener(x -> { proof[0] = null; refreshProof.run(); });

        AlertDialog dialog = new MaterialAlertDialogBuilder(ctx)
                .setTitle("Dismiss report")
                .setView(v)
                .create();
        v.findViewById(R.id.cancel).setOnClickListener(x -> dialog.dismiss());
        v.findViewById(R.id.confirm).setOnClickListener(x -> {
            String reason = reasonField.getText() == null ? "" : reasonField.getText().toString().trim();
            String problem = DismissalValidator.validateReason(reason);
            reasonLayout.setError(problem);
            if (problem != null) return;
            dialog.dismiss();
            onDecision.accept(new Decision(reason, proof[0]));
        });
        dialog.show();
    }
}
