package com.gianlu.timeless.Activities;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.Toolbar;
import android.util.Pair;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.gianlu.commonutils.RecyclerViewLayout;
import com.gianlu.commonutils.Toaster;
import com.gianlu.timeless.Charting.SaveChartAppCompatActivity;
import com.gianlu.timeless.Listing.CardsAdapter;
import com.gianlu.timeless.Models.Duration;
import com.gianlu.timeless.Models.GlobalSummary;
import com.gianlu.timeless.Models.Project;
import com.gianlu.timeless.Models.Summary;
import com.gianlu.timeless.NetIO.WakaTime;
import com.gianlu.timeless.NetIO.WakaTimeException;
import com.gianlu.timeless.R;
import com.gianlu.timeless.Utils;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class DailyStatsActivity extends SaveChartAppCompatActivity implements DatePickerDialog.OnDateSetListener, WakaTime.ISummary {
    private TextView currDay;
    private Pair<Date, Date> currentDatePair;
    private WakaTime wakaTime;
    private RecyclerViewLayout recyclerViewLayout;

    private void updatePage(Date newDate) {
        if (newDate.after(new Date())) {
            Toaster.show(DailyStatsActivity.this, Utils.Messages.FUTURE_DATE, Utils.getOnlyDateFormatter().format(newDate));
            return;
        }

        currentDatePair = new Pair<>(newDate, newDate);
        currDay.setText(Utils.getVerbalDateFormatter().format(newDate));

        recyclerViewLayout.startLoading();

        wakaTime.getRangeSummary(currentDatePair, this);
    }

    @Override
    public void onSummary(List<Summary> summaries, final GlobalSummary globalSummary, @Nullable List<String> branches, @Nullable final List<String> selectedBranches) {
        wakaTime.getDurations(currentDatePair.first, null, new WakaTime.IDurations() {
            @Override
            public void onDurations(final List<Duration> durations, List<String> branches) {
                recyclerViewLayout.loadListData(new CardsAdapter(DailyStatsActivity.this, new CardsAdapter.CardsList()
                        .addGlobalSummary(globalSummary)
                        .addDurations(R.string.durations, durations)
                        .addPieChart(R.string.projects, globalSummary.projects)
                        .addPieChart(R.string.languages, globalSummary.languages)
                        .addPieChart(R.string.editors, globalSummary.editors)
                        .addPieChart(R.string.operatingSystems, globalSummary.operating_systems), DailyStatsActivity.this));
            }

            @Override
            public void onException(final Exception ex) {
                DailyStatsActivity.this.onException(ex);
            }
        });
    }

    @Override
    public void onException(final Exception ex) {
        if (ex instanceof WakaTimeException) {
            recyclerViewLayout.showMessage(ex.getMessage(), false);
        } else {
            recyclerViewLayout.showMessage(R.string.failedLoading_reason, true, ex.getMessage());
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.daily_stats, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
            case R.id.dailyStats_changeDay:
                Calendar now = Calendar.getInstance();
                DatePickerDialog.newInstance(this,
                        now.get(Calendar.YEAR),
                        now.get(Calendar.MONTH),
                        now.get(Calendar.DAY_OF_MONTH)).show(getFragmentManager(), null);
                break;
        }

        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_daily_stats);
        setTitle(R.string.dailyStats);

        Toolbar toolbar = findViewById(R.id.dailyStats_toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) actionBar.setDisplayHomeAsUpEnabled(true);

        recyclerViewLayout = findViewById(R.id.dailyStats_recyclerViewLayout);
        recyclerViewLayout.disableSwipeRefresh();
        recyclerViewLayout.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        final ImageButton nextDay = findViewById(R.id.dailyStats_nextDay);
        final ImageButton prevDay = findViewById(R.id.dailyStats_prevDay);
        currDay = findViewById(R.id.dailyStats_day);

        wakaTime = WakaTime.get();

        nextDay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentDatePair == null) return;

                Calendar cal = Calendar.getInstance();
                cal.setTime(currentDatePair.first);
                cal.add(Calendar.DATE, 1);

                updatePage(cal.getTime());
            }
        });

        prevDay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentDatePair == null) return;

                Calendar cal = Calendar.getInstance();
                cal.setTime(currentDatePair.first);
                cal.add(Calendar.DATE, -1);

                updatePage(cal.getTime());
            }
        });

        updatePage(new Date());
    }

    @Nullable
    @Override
    public Project getProject() {
        return null;
    }

    @Override
    public void onDateSet(DatePickerDialog view, int year, int monthOfYear, int dayOfMonth) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(0);
        cal.set(Calendar.YEAR, year);
        cal.set(Calendar.MONTH, monthOfYear);
        cal.set(Calendar.DAY_OF_MONTH, dayOfMonth);

        updatePage(cal.getTime());
    }
}
