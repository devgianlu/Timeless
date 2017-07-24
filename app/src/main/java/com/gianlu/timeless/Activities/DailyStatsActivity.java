package com.gianlu.timeless.Activities;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
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
import android.widget.Toast;

import com.gianlu.commonutils.CommonUtils;
import com.gianlu.commonutils.Toaster;
import com.gianlu.timeless.GrantActivity;
import com.gianlu.timeless.Listing.CardsAdapter;
import com.gianlu.timeless.Models.Duration;
import com.gianlu.timeless.Models.Summary;
import com.gianlu.timeless.NetIO.WakaTime;
import com.gianlu.timeless.NetIO.WakaTimeException;
import com.gianlu.timeless.R;
import com.gianlu.timeless.ThisApplication;
import com.gianlu.timeless.Utils;
import com.google.android.gms.analytics.HitBuilders;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class DailyStatsActivity extends AppCompatActivity implements CardsAdapter.ISaveChart, DatePickerDialog.OnDateSetListener, WakaTime.ISummary {
    private static final int REQUEST_CODE = 3;
    private CardsAdapter.IPermissionRequest handler;
    private RecyclerView list;
    private TextView currDay;
    private Pair<Date, Date> currentDatePair;
    private ProgressBar loading;
    private TextView error;
    private SwipeRefreshLayout swipeRefreshLayout;

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

        WakaTime.getInstance().getRangeSummary(currentDatePair, this);
    }

    @Override
    public void onSummary(List<Summary> summaries, final Summary summary) {
        WakaTime.getInstance().getDurations(DailyStatsActivity.this, currentDatePair.first, new WakaTime.IDurations() {
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
                                .addSummary(summary)
                                .addDurations(getString(R.string.durationsSummary), durations)
                                .addPieChart(getString(R.string.projectsSummary), summary.projects)
                                .addPieChart(getString(R.string.languagesSummary), summary.languages)
                                .addPieChart(getString(R.string.editorsSummary), summary.editors)
                                .addPieChart(getString(R.string.operatingSystemsSummary), summary.operating_systems), DailyStatsActivity.this));
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

        Toolbar toolbar = (Toolbar) findViewById(R.id.dailyStats_toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) actionBar.setDisplayHomeAsUpEnabled(true);

        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.dailyStats_swipeRefresh);
        swipeRefreshLayout.setColorSchemeResources(Utils.getColors());
        loading = (ProgressBar) findViewById(R.id.dailyStats_loading);
        error = (TextView) findViewById(R.id.dailyStats_error);
        final ImageButton nextDay = (ImageButton) findViewById(R.id.dailyStats_nextDay);
        final ImageButton prevDay = (ImageButton) findViewById(R.id.dailyStats_prevDay);
        currDay = (TextView) findViewById(R.id.dailyStats_day);
        list = (RecyclerView) findViewById(R.id.dailyStats_list);
        list.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));

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

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (handler != null && requestCode == REQUEST_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) handler.onGranted();
            else Toaster.show(this, Utils.ToastMessages.WRITE_DENIED);
        }
    }

    @Override
    public void onWritePermissionRequested(CardsAdapter.IPermissionRequest handler) {
        this.handler = handler;
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            CommonUtils.showDialog(this, new AlertDialog.Builder(this)
                    .setTitle(R.string.writeExternalStorageRequest_title)
                    .setMessage(R.string.writeExternalStorageRequest_message)
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            ActivityCompat.requestPermissions(DailyStatsActivity.this, new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CODE);
                        }
                    }));
        } else {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CODE);
        }
    }

    @Override
    public void onSaveRequested(View chart, String name) {
        File dest = new File(Utils.getImageDirectory(null), name + ".png");
        try (OutputStream out = new FileOutputStream(dest)) {
            Bitmap bitmap = Utils.createBitmap(chart);

            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
            out.flush();

            Toaster.show(this, getString(R.string.savedIn, dest.getPath()), Toast.LENGTH_LONG, null, null, null);
        } catch (IOException ex) {
            Toaster.show(this, Utils.ToastMessages.FAILED_SAVING_CHART, ex);
        }

        ThisApplication.sendAnalytics(this, new HitBuilders.EventBuilder()
                .setCategory(ThisApplication.CATEGORY_USER_INPUT)
                .setAction(ThisApplication.ACTION_SAVED_CHART)
                .build());
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
