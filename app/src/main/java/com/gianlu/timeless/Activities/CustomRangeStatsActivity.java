package com.gianlu.timeless.Activities;

import android.os.Bundle;
import android.util.Pair;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;

import com.gianlu.commonutils.CasualViews.RecyclerViewLayout;
import com.gianlu.commonutils.MaterialColors;
import com.gianlu.commonutils.Toaster;
import com.gianlu.timeless.Charting.OnSaveChart;
import com.gianlu.timeless.Charting.SaveChartAppCompatActivity;
import com.gianlu.timeless.Listing.CardsAdapter;
import com.gianlu.timeless.Models.Project;
import com.gianlu.timeless.Models.Summaries;
import com.gianlu.timeless.NetIO.WakaTime;
import com.gianlu.timeless.NetIO.WakaTimeException;
import com.gianlu.timeless.R;
import com.gianlu.timeless.ThisApplication;
import com.gianlu.timeless.Utils;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

public class CustomRangeStatsActivity extends SaveChartAppCompatActivity implements WakaTime.OnSummary, DatePickerDialog.OnDateSetListener, CardsAdapter.Listener, OnSaveChart {
    private Pair<Date, Date> currentRange;
    private Date tmpStart;
    private WakaTime wakaTime;
    private RecyclerViewLayout layout;
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

        layout = findViewById(R.id.customRangeStats_recyclerViewLayout);
        layout.useVerticalLinearLayoutManager();
        layout.enableSwipeRefresh(() -> {
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
                .addGlobalSummary(summaries.globalSummary)
                .addProjectsBarChart(R.string.periodActivity, summaries)
                .addPieChart(R.string.projects, summaries.globalSummary.projects)
                .addPieChart(R.string.languages, summaries.globalSummary.languages)
                .addPieChart(R.string.editors, summaries.globalSummary.editors)
                .addPieChart(R.string.operatingSystems, summaries.globalSummary.operating_systems);

        layout.loadListData(new CardsAdapter(this, cards, this, this));
        dismissDialog();
    }

    @Override
    public void onWakaTimeError(@NonNull WakaTimeException ex) {
        layout.showError(ex.getMessage());
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

                DatePickerDialog dialog = DatePickerDialog.newInstance(this,
                        start.get(Calendar.YEAR),
                        start.get(Calendar.MONTH),
                        start.get(Calendar.DAY_OF_MONTH));

                dialog.setTitle(getString(R.string.selectStartDate));
                dialog.show(getSupportFragmentManager(), "START");
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDateSet(DatePickerDialog view, int year, int monthOfYear, int dayOfMonth) {
        if (Objects.equals(view.getTag(), "START")) {
            Calendar start = Calendar.getInstance();
            start.setTimeInMillis(0);
            start.set(year, monthOfYear, dayOfMonth);
            tmpStart = start.getTime();

            Calendar end = Calendar.getInstance();
            end.setTime(currentRange.second);

            DatePickerDialog dialog = DatePickerDialog.newInstance(this,
                    end.get(Calendar.YEAR),
                    end.get(Calendar.MONTH),
                    end.get(Calendar.DAY_OF_MONTH));

            dialog.setTitle(getString(R.string.selectEndDate));
            dialog.show(getSupportFragmentManager(), "END");
            return;
        }

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