package com.gianlu.timeless.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Pair;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.gianlu.commonutils.Toaster;
import com.gianlu.timeless.Charting.SaveChartAppCompatActivity;
import com.gianlu.timeless.GrantActivity;
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
    private RecyclerView list;
    private TextView currDay;
    private Pair<Date, Date> currentDatePair;
    private ProgressBar loading;
    private TextView error;
    private SwipeRefreshLayout swipeRefreshLayout;
    private WakaTime wakaTime;

    private void updatePage(Date newDate, @Nullable final SwipeRefreshLayout swipeRefresh) {
        if (newDate.after(new Date())) {
            Toaster.show(DailyStatsActivity.this, Utils.ToastMessages.FUTURE_DATE, Utils.getOnlyDateFormatter().format(newDate));
            return;
        }

        currentDatePair = new Pair<>(newDate, newDate);
        currDay.setText(Utils.getOnlyDateFormatter().format(newDate));

        if (swipeRefresh == null) {
            loading.setVisibility(View.VISIBLE);
            error.setVisibility(View.GONE);
            list.setVisibility(View.GONE);
        }

        wakaTime.getRangeSummary(currentDatePair, this);
    }

    @Override
    public void onSummary(List<Summary> summaries, final GlobalSummary globalSummary) {
        wakaTime.getDurations(currentDatePair.first, new WakaTime.IDurations() {
            @Override
            public void onDurations(final List<Duration> durations) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (swipeRefreshLayout.isRefreshing())
                            swipeRefreshLayout.setRefreshing(false);
                        error.setVisibility(View.GONE);
                        loading.setVisibility(View.GONE);
                        list.setVisibility(View.VISIBLE);
                        list.setAdapter(new CardsAdapter(DailyStatsActivity.this, new CardsAdapter.CardsList()
                                .addGlobalSummary(globalSummary)
                                .addDurations(getString(R.string.durationsSummary), durations)
                                .addPieChart(getString(R.string.projectsSummary), globalSummary.projects)
                                .addPieChart(getString(R.string.languagesSummary), globalSummary.languages)
                                .addPieChart(getString(R.string.editorsSummary), globalSummary.editors)
                                .addPieChart(getString(R.string.operatingSystemsSummary), globalSummary.operating_systems), DailyStatsActivity.this));
                    }
                });
            }

            @Override
            public void onException(final Exception ex) {
                if (ex instanceof WakaTimeException) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (swipeRefreshLayout.isRefreshing()) {
                                swipeRefreshLayout.setRefreshing(false);
                            } else {
                                loading.setVisibility(View.GONE);
                                error.setText(ex.getMessage());
                                error.setVisibility(View.VISIBLE);
                            }
                        }
                    });
                } else {
                    Toaster.show(DailyStatsActivity.this, swipeRefreshLayout.isRefreshing() ? Utils.ToastMessages.FAILED_LOADING : Utils.ToastMessages.FAILED_REFRESHING, ex, new Runnable() {
                        @Override
                        public void run() {
                            if (swipeRefreshLayout.isRefreshing()) {
                                swipeRefreshLayout.setRefreshing(false);
                            } else {
                                loading.setVisibility(View.GONE);
                                error.setVisibility(View.VISIBLE);
                            }
                        }
                    });
                }
            }

            @Override
            public void onWakaTimeException(WakaTimeException ex) {
                Toaster.show(DailyStatsActivity.this, Utils.ToastMessages.INVALID_TOKEN, ex);
                startActivity(new Intent(DailyStatsActivity.this, GrantActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
            }
        });
    }

    @Override
    public void onWakaTimeException(WakaTimeException ex) {
        Toaster.show(DailyStatsActivity.this, Utils.ToastMessages.INVALID_TOKEN, ex);
        startActivity(new Intent(DailyStatsActivity.this, GrantActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
    }

    @Override
    public void onException(final Exception ex) {
        if (ex instanceof WakaTimeException) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    swipeRefreshLayout.setRefreshing(false);
                    loading.setVisibility(View.GONE);
                    error.setText(ex.getMessage());
                    error.setVisibility(View.VISIBLE);
                }
            });
        } else {
            if (swipeRefreshLayout.isRefreshing()) {
                Toaster.show(DailyStatsActivity.this, Utils.ToastMessages.FAILED_REFRESHING, ex, new Runnable() {
                    @Override
                    public void run() {
                        swipeRefreshLayout.setRefreshing(false);
                    }
                });
            } else {
                Toaster.show(DailyStatsActivity.this, Utils.ToastMessages.FAILED_LOADING, ex, new Runnable() {
                    @Override
                    public void run() {
                        loading.setVisibility(View.GONE);
                        error.setVisibility(View.VISIBLE);
                    }
                });
            }
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

        swipeRefreshLayout = findViewById(R.id.dailyStats_swipeRefresh);
        swipeRefreshLayout.setColorSchemeResources(Utils.getColors());
        loading = findViewById(R.id.dailyStats_loading);
        error = findViewById(R.id.dailyStats_error);
        final ImageButton nextDay = findViewById(R.id.dailyStats_nextDay);
        final ImageButton prevDay = findViewById(R.id.dailyStats_prevDay);
        currDay = findViewById(R.id.dailyStats_day);
        list = findViewById(R.id.dailyStats_list);
        list.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));

        wakaTime = WakaTime.getInstance();

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                updatePage(currentDatePair.first, swipeRefreshLayout);
            }
        });

        nextDay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentDatePair == null) return;

                Calendar cal = Calendar.getInstance();
                cal.setTime(currentDatePair.first);
                cal.add(Calendar.DATE, 1);

                updatePage(cal.getTime(), null);
            }
        });

        prevDay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentDatePair == null) return;

                Calendar cal = Calendar.getInstance();
                cal.setTime(currentDatePair.first);
                cal.add(Calendar.DATE, -1);

                updatePage(cal.getTime(), null);
            }
        });

        updatePage(new Date(), null);
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

        updatePage(cal.getTime(), null);
    }
}
