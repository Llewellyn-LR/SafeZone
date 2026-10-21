package com.safezone.app.ui.report;

import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.safezone.app.R;
import com.safezone.app.ServiceLocator;
import com.safezone.app.model.CrimeReport;
import com.safezone.app.service.ReportService;

import java.util.List;

public class MyReportsActivity extends AppCompatActivity {
    private LinearLayout list;
    private View empty;
    private final ReportService.Listener listener = this::render;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_my_reports);
        MaterialToolbar tb = findViewById(R.id.toolbar);
        tb.setNavigationOnClickListener(v -> finish());
        list = findViewById(R.id.myReportsList);
        empty = findViewById(R.id.emptyText);
    }

    @Override
    protected void onStart() {
        super.onStart();
        ServiceLocator.reports().addListener(listener);
        render();
    }

    @Override
    protected void onStop() {
        ServiceLocator.reports().removeListener(listener);
        super.onStop();
    }

    private void render() {
        List<CrimeReport> mine = ServiceLocator.reports().getMyReports();
        empty.setVisibility(mine.isEmpty() ? View.VISIBLE : View.GONE);
        MyReportTiles.render(this, getLayoutInflater(), list, mine);
    }
}
