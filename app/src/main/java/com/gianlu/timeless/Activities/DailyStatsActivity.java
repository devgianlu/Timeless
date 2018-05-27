package com.gianlu.timeless.Activities;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.gianlu.commonutils.MaterialColors;
import com.gianlu.commonutils.RecyclerViewLayout;
import com.gianlu.commonutils.Toaster;
import com.gianlu.timeless.Charting.SaveChartAppCompatActivity;
import com.gianlu.timeless.Listing.CardsAdapter;
import com.gianlu.timeless.Models.Durations;
import com.gianlu.timeless.Models.Project;
import com.gianlu.timeless.Models.Summaries;
import com.gianlu.timeless.NetIO.WakaTime;
import com.gianlu.timeless.NetIO.WakaTimeException;
import com.gianlu.timeless.R;
import com.gianlu.timeless.Utils;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;

import java.util.Calendar;
import java.util.Date;

public class DailyStatsActivity extends SaveChartAppCompatActivity implements DatePickerDialog.OnDateSetListener, WakaTime.BatchStuff, CardsAdapter.IAdapter {
    private TextView currDay;
    private Date currentDate;
    private RecyclerViewLayout recyclerViewLayout;

    private void updatePage(@NonNull Date newDate, boolean refresh) {
        if (newDate.after(new Date())) {
            Toaster.show(DailyStatsActivity.this, Utils.Messages.FUTURE_DATE, Utils.getOnlyDateFormatter().format(newDate));
            return;
        }

        currentDate = newDate;
        currDay.setText(Utils.getVerbalDateFormatter().format(newDate));

        recyclerViewLayout.startLoading();

        try {
            WakaTime.get().batch(this, refresh);
        } catch (WakaTime.ShouldGetAccessToken ex) {
            ex.resolve(this);
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
        recyclerViewLayout.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        recyclerViewLayout.enableSwipeRefresh(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                updatePage(currentDate == null ? new Date() : currentDate, true);
            }
        }, MaterialColors.getInstance().getColorsRes());

        ImageButton nextDay = findViewById(R.id.dailyStats_nextDay);
        ImageButton prevDay = findViewById(R.id.dailyStats_prevDay);
        currDay = findViewById(R.id.dailyStats_day);

        nextDay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentDate == null) return;

                Calendar cal = Calendar.getInstance();
                cal.setTime(currentDate);
                cal.add(Calendar.DATE, 1);

                updatePage(cal.getTime(), false);
            }
        });

        prevDay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentDate == null) return;

                Calendar cal = Calendar.getInstance();
                cal.setTime(currentDate);
                cal.add(Calendar.DATE, -1);

                updatePage(cal.getTime(), false);
            }
        });

        updatePage(new Date(), false);
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

        updatePage(cal.getTime(), false);
    }

    @Override
    public void request(@NonNull WakaTime.Requester requester, @NonNull Handler ui) throws Exception {
        Summaries summaries = requester.summaries(currentDate, currentDate, null, null);
        Durations durations = requester.durations(currentDate, null, null);

        final CardsAdapter adapter = new CardsAdapter(this, new CardsAdapter.CardsList()
                .addGlobalSummary(summaries.globalSummary)
                .addDurations(R.string.durations, durations.durations)
                .addPieChart(R.string.projects, summaries.globalSummary.projects)
                .addPieChart(R.string.languages, summaries.globalSummary.languages)
                .addPieChart(R.string.editors, summaries.globalSummary.editors)
                .addPieChart(R.string.operatingSystems, summaries.globalSummary.operating_systems), this, this);

        ui.post(new Runnable() {
            @Override
            public void run() {
                recyclerViewLayout.loadListData(adapter);
            }
        });
    }

    @Override
    public void somethingWentWrong(@NonNull Exception ex) {
        if (ex instanceof WakaTimeException) recyclerViewLayout.showMessage(ex.getMessage(), false);
        else recyclerViewLayout.showMessage(R.string.failedLoading_reason, true, ex.getMessage());
    }
}
