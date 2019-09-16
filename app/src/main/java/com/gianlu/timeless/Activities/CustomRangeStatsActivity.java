package com.gianlu.timeless.Activities;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.util.Pair;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.DatePicker;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;

import com.gianlu.commonutils.CasualViews.RecyclerMessageView;
import com.gianlu.commonutils.Dialogs.MaterialDatePickerDialog;
import com.gianlu.commonutils.MaterialColors;
import com.gianlu.commonutils.Toaster;
import com.gianlu.timeless.Charting.OnSaveChart;
import com.gianlu.timeless.Charting.SaveChartAppCompatActivity;
import com.gianlu.timeless.Listing.CardsAdapter;
import com.gianlu.timeless.Listing.HelperViewHolder;
import com.gianlu.timeless.Models.Project;
import com.gianlu.timeless.Models.Summaries;
import com.gianlu.timeless.NetIO.WakaTime;
import com.gianlu.timeless.NetIO.WakaTimeException;
import com.gianlu.timeless.R;
import com.gianlu.timeless.ThisApplication;
import com.gianlu.timeless.Utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class CustomRangeStatsActivity extends SaveChartAppCompatActivity implements WakaTime.OnSummary, DatePickerDialog.OnDateSetListener, HelperViewHolder.Listener, OnSaveChart {
    private Pair<Date, Date> currentRange;
    private Date tmpStart;
    private WakaTime wakaTime;
    private RecyclerMessageView rmv;
    private TextView rangeText;

    public void updateRangeText() {
        SimpleDateFormat formatter = new SimpleDateFormat("dd MMM", Locale.getDefault());
        if (currentRange.first.getTime() == currentRange.second.getTime())
            rangeText.setText(formatter.format(currentRange.first));
        else
            rangeText.setText(formatter.format(currentRange.first) + " - " + formatter.format(currentRange.second));
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_custom_range_stats);
        setTitle(R.string.customRangeStats);

        Toolbar toolbar = findViewById(R.id.customRangeStats_toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) actionBar.setDisplayHomeAsUpEnabled(true);

        rmv = findViewById(R.id.customRangeStats_recyclerViewLayout);
        rmv.linearLayoutManager(RecyclerView.VERTICAL, false);
        rmv.dividerDecoration(RecyclerView.VERTICAL);
        rmv.enableSwipeRefresh(() -> {
            wakaTime.skipNextRequestCache();
            wakaTime.getRangeSummary(currentRange, null, this);
        }, MaterialColors.getInstance().getColorsRes());

        rangeText = findViewById(R.id.customRangeStats_rangeText);

        showProgress(R.string.loadingData);

        Date date = (Date) getIntent().getSerializableExtra("date");
        if (date != null) currentRange = new Pair<>(date, date);
        else currentRange = WakaTime.Range.LAST_7_DAYS.getStartAndEnd();

        try {
            wakaTime = WakaTime.get();
        } catch (WakaTime.ShouldGetAccessToken ex) {
            ex.resolve(this);
            return;
        }

        updateRangeText();
        wakaTime.getRangeSummary(currentRange, null, this);
    }

    @Override
    public void onSummary(@NonNull Summaries summaries) {
        CardsAdapter.CardsList cards = new CardsAdapter.CardsList()
                .addGlobalSummary(summaries.globalSummary, CardsAdapter.SummaryContext.CUSTOM_RANGE)
                .addProjectsBarChart(R.string.periodActivity, summaries)
                .addPieChart(R.string.projects, summaries.globalSummary.projects)
                .addPieChart(R.string.languages, summaries.globalSummary.languages)
                .addPieChart(R.string.editors, summaries.globalSummary.editors)
                .addPieChart(R.string.machines, summaries.globalSummary.machines)
                .addPieChart(R.string.operatingSystems, summaries.globalSummary.operating_systems);

        rmv.loadListData(new CardsAdapter(this, cards, this, this));
        dismissDialog();
    }

    @Override
    public void onWakaTimeError(@NonNull WakaTimeException ex) {
        rmv.showError(ex.getMessage());
        dismissDialog();
    }

    @Override
    public void onException(@NonNull Exception ex) {
        Toaster.with(this).message(R.string.failedLoading).ex(ex).show();
        onBackPressed();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.custom_range_stats, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
            case R.id.customRangeStats_range:
                Calendar start = Calendar.getInstance();
                start.setTime(currentRange.first);

                MaterialDatePickerDialog.get(getString(R.string.selectStartDate),
                        currentRange.first, null, new Date(), (view, year, month, dayOfMonth) -> {
                            Calendar start1 = Calendar.getInstance();
                            start1.setTimeInMillis(0);
                            start1.set(year, month, dayOfMonth);
                            tmpStart = start1.getTime();

                            MaterialDatePickerDialog.get(getString(R.string.selectEndDate),
                                    currentRange.second, tmpStart, new Date(), CustomRangeStatsActivity.this)
                                    .show(getSupportFragmentManager(), "END");
                        }).show(getSupportFragmentManager(), "START");
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
        Calendar end = Calendar.getInstance();
        end.setTimeInMillis(0);
        end.set(year, monthOfYear, dayOfMonth);

        currentRange = new Pair<>(tmpStart, end.getTime());
        updateRangeText();

        showProgress(R.string.loadingData);
        wakaTime.getRangeSummary(currentRange, null, this);

        ThisApplication.sendAnalytics(Utils.ACTION_DATE_RANGE);
    }

    @Nullable
    @Override
    public Project getProject() {
        return null;
    }
}
