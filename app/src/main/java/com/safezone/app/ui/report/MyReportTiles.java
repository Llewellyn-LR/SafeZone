package com.safezone.app.ui.report;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import com.safezone.app.R;
import com.safezone.app.ServiceLocator;
import com.safezone.app.model.CrimeReport;
import com.safezone.app.model.ReportStatus;
import com.safezone.app.service.ReportService;
import com.safezone.app.ui.common.Ui;

import java.util.List;

final class MyReportTiles {
    private MyReportTiles() { }

    static int countAwaitingAnswer(List<CrimeReport> reports) {
        int n = 0;
        for (CrimeReport r : reports) if (r.awaitingReporterConfirmation()) n++;
        return n;
    }

    static void render(Context ctx, LayoutInflater inflater, LinearLayout list,
                       List<CrimeReport> reports) {
        list.removeAllViews();
        for (CrimeReport r : reports) {
            View tile = inflater.inflate(R.layout.item_my_report, list, false);
            ((ImageView) tile.findViewById(R.id.icon)).setImageResource(r.category.iconRes);
            ((TextView) tile.findViewById(R.id.title)).setText(r.category.label);
            ((TextView) tile.findViewById(R.id.area)).setText(r.area);
            int tint = ContextCompat.getColor(ctx, r.status.colorRes);
            TextView status = tile.findViewById(R.id.status);
            status.setText(r.displayStatusLabel());
            status.setTextColor(ContextCompat.getColor(ctx, r.status.textColorRes));
            status.setBackground(Ui.rounded(ctx, Ui.withAlpha(tint, 0.12f), 20));

            TextView detail = tile.findViewById(R.id.detail);
            String note = null;
            if (r.rejectionReason != null && !r.rejectionReason.isEmpty()) {
                note = "Reason: " + r.rejectionReason;
            } else if (r.isGbv() && r.status == ReportStatus.FOLLOW_UP) {
                note = "You said this wasn't resolved. A moderator is looking at it again.";
            } else if (r.isGbv() && r.status == ReportStatus.SOLVED) {
                note = "You confirmed this case as solved.";
            }
            detail.setText(note);
            detail.setVisibility(note == null ? View.GONE : View.VISIBLE);

            View panel = tile.findViewById(R.id.confirmPanel);
            panel.setVisibility(r.awaitingReporterConfirmation() ? View.VISIBLE : View.GONE);
            if (r.awaitingReporterConfirmation()) {
                View yes = tile.findViewById(R.id.confirmSolved);
                View no = tile.findViewById(R.id.notSolved);
                yes.setOnClickListener(x -> answer(ctx, r, true, yes, no));
                no.setOnClickListener(x -> answer(ctx, r, false, yes, no));
            }
            list.addView(tile);
        }
    }

    private static void answer(Context ctx, CrimeReport report, boolean solved, View yes, View no) {
        yes.setEnabled(false);
        no.setEnabled(false);
        ServiceLocator.reports().respondToSolved(report, solved, new ReportService.ActionCallback() {
            @Override public void onSuccess() { }
            @Override public void onError(Exception e) {
                yes.setEnabled(true);
                no.setEnabled(true);
                Toast.makeText(ctx.getApplicationContext(),
                        "Couldn't send your answer: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}
