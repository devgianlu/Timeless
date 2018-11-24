package com.gianlu.timeless.Activities;

import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class DailyStatsActivity extends SaveChartAppCompatActivity implements DatePickerDialog.OnDateSetListener, WakaTime.BatchStuff, CardsAdapter.Listener {
    private TextView currDay;
    private Date currentDate;
    private RecyclerViewLayout recyclerViewLayout;

    private void updatePage(@NonNull Date newDate, boolean refresh) {
        if (newDate.after(new Date())) {
            Toaster.with(this).message(R.string.cannotGoFuture).extra(newDate).show();
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
                        now.get(Calendar.DAY_OF_MONTH)).show(getSupportFragmentManager(), null);
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
        recyclerViewLayout.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, false));
        recyclerViewLayout.enableSwipeRefresh(() -> updatePage(currentDate == null ? new Date() : currentDate, true), MaterialColors.getInstance().getColorsRes());

        ImageButton nextDay = findViewById(R.id.dailyStats_nextDay);
        ImageButton prevDay = findViewById(R.id.dailyStats_prevDay);
        currDay = findViewById(R.id.dailyStats_day);

        nextDay.setOnClickListener(v -> {
            if (currentDate == null) return;

            Calendar cal = Calendar.getInstance();
            cal.setTime(currentDate);
            cal.add(Calendar.DATE, 1);

            updatePage(cal.getTime(), false);
        });

        prevDay.setOnClickListener(v -> {
            if (currentDate == null) return;

            Calendar cal = Calendar.getInstance();
            cal.setTime(currentDate);
            cal.add(Calendar.DATE, -1);

            updatePage(cal.getTime(), false);
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
                .addDurations(R.string.durations, durations)
                .addPieChart(R.string.projects, summaries.globalSummary.projects)
                .addPieChart(R.string.languages, summaries.globalSummary.languages)
                .addPieChart(R.string.editors, summaries.globalSummary.editors)
                .addPieChart(R.string.operatingSystems, summaries.globalSummary.operating_systems), this, this);

        ui.post(() -> recyclerViewLayout.loadListData(adapter));
    }

    @Override
    public void somethingWentWrong(@NonNull Exception ex) {
        if (ex instanceof WakaTimeException) recyclerViewLayout.showError(ex.getMessage());
        else recyclerViewLayout.showError(R.string.failedLoading_reason, ex.getMessage());
    }
}
