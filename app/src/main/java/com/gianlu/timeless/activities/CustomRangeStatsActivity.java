package com.gianlu.timeless.activities;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.util.Log;
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

import com.gianlu.commonutils.dialogs.ActivityWithDialog;
import com.gianlu.commonutils.dialogs.MaterialDatePickerDialog;
import com.gianlu.commonutils.misc.RecyclerMessageView;
import com.gianlu.commonutils.typography.MaterialColors;
import com.gianlu.commonutils.ui.Toaster;
import com.gianlu.timeless.R;
import com.gianlu.timeless.ThisApplication;
import com.gianlu.timeless.Utils;
import com.gianlu.timeless.api.WakaTime;
import com.gianlu.timeless.api.WakaTimeException;
import com.gianlu.timeless.api.models.Summaries;
import com.gianlu.timeless.colors.LookupColorMapper;
import com.gianlu.timeless.colors.PersistentColorMapper;
import com.gianlu.timeless.listing.CardsAdapter;
import com.gianlu.timeless.listing.PieChartViewHolder.ChartContext;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class CustomRangeStatsActivity extends ActivityWithDialog implements WakaTime.OnSummary, DatePickerDialog.OnDateSetListener {
    private static final String TAG = CustomRangeStatsActivity.class.getSimpleName();
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
            rangeText.setText(String.format(Locale.getDefault(), "%s - %s", formatter.format(currentRange.first), formatter.format(currentRange.second)));
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
        } catch (WakaTime.MissingCredentialsException ex) {
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
                .addPieChart(R.string.projects, ChartContext.PROJECTS, summaries.globalSummary.interval(), summaries.globalSummary.projects, PersistentColorMapper.get(PersistentColorMapper.Type.PROJECTS))
                .addPieChart(R.string.languages, ChartContext.IRRELEVANT, summaries.globalSummary.interval(), summaries.globalSummary.languages, LookupColorMapper.get(this, LookupColorMapper.Type.LANGUAGES))
                .addPieChart(R.string.editors, ChartContext.IRRELEVANT, summaries.globalSummary.interval(), summaries.globalSummary.editors, LookupColorMapper.get(this, LookupColorMapper.Type.EDITORS))
                .addPieChart(R.string.machines, ChartContext.IRRELEVANT, summaries.globalSummary.interval(), summaries.globalSummary.machines, PersistentColorMapper.get(PersistentColorMapper.Type.MACHINES))
                .addPieChart(R.string.operatingSystems, ChartContext.IRRELEVANT, summaries.globalSummary.interval(), summaries.globalSummary.operating_systems, LookupColorMapper.get(this, LookupColorMapper.Type.OPERATING_SYSTEMS));

        rmv.loadListData(new CardsAdapter(this, cards, null, this));
        dismissDialog();
    }

    @Override
    public void onWakaTimeError(@NonNull WakaTimeException ex) {
        rmv.showError(ex.getMessage());
        dismissDialog();
    }

    @Override
    public void onException(@NonNull Exception ex) {
        Log.e(TAG, "Failed loading stats.", ex);
        Toaster.with(this).message(R.string.failedLoading).show();
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
                return true;
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
                return true;
            default:
                return false;
        }
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
}
