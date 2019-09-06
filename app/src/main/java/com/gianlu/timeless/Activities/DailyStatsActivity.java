package com.gianlu.timeless.Activities;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.DatePicker;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;

import com.gianlu.commonutils.CasualViews.RecyclerMessageView;
import com.gianlu.commonutils.Dialogs.MaterialDatePickerDialog;
import com.gianlu.commonutils.Lifecycle.LifecycleAwareHandler;
import com.gianlu.commonutils.MaterialColors;
import com.gianlu.timeless.Charting.SaveChartAppCompatActivity;
import com.gianlu.timeless.Listing.CardsAdapter;
import com.gianlu.timeless.Models.Durations;
import com.gianlu.timeless.Models.Project;
import com.gianlu.timeless.Models.Summaries;
import com.gianlu.timeless.NetIO.WakaTime;
import com.gianlu.timeless.NetIO.WakaTimeException;
import com.gianlu.timeless.R;
import com.gianlu.timeless.Utils;

import java.util.Calendar;
import java.util.Date;

public class DailyStatsActivity extends SaveChartAppCompatActivity implements DatePickerDialog.OnDateSetListener, WakaTime.BatchStuff, CardsAdapter.Listener, DatePicker.OnDateChangedListener {
    private TextView currDay;
    private Date currentDate;
    private RecyclerMessageView rmv;
    private ImageButton nextDay;

    private void updatePage(@NonNull Date newDate, boolean refresh) {
        if (newDate.after(new Date())) return;

        Calendar cal = Calendar.getInstance();
        cal.setTime(newDate);
        cal.add(Calendar.DATE, +1);

        if (cal.getTime().after(new Date())) nextDay.setVisibility(View.INVISIBLE);
        else nextDay.setVisibility(View.VISIBLE);

        currentDate = newDate;
        currDay.setText(Utils.getVerbalDateFormatter().format(newDate));

        rmv.startLoading();

        try {
            WakaTime.get().batch(null, this, refresh);
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
                MaterialDatePickerDialog.get(getString(R.string.selectDay), currentDate,
                        null, new Date(), this)
                        .show(getSupportFragmentManager(), null);
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

        rmv = findViewById(R.id.dailyStats_recyclerViewLayout);
        rmv.linearLayoutManager(RecyclerView.VERTICAL, false);
        rmv.enableSwipeRefresh(() -> updatePage(currentDate == null ? new Date() : currentDate, true), MaterialColors.getInstance().getColorsRes());
        rmv.dividerDecoration(RecyclerView.VERTICAL);

        nextDay = findViewById(R.id.dailyStats_nextDay);
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
    public void onDateSet(DatePicker datePicker, int year, int monthOfYear, int dayOfMonth) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(0);
        cal.set(Calendar.YEAR, year);
        cal.set(Calendar.MONTH, monthOfYear);
        cal.set(Calendar.DAY_OF_MONTH, dayOfMonth);

        updatePage(cal.getTime(), false);
    }

    @Override
    public void onDateChanged(DatePicker datePicker, int i, int i1, int i2) {

    }

    @Override
    public void request(@NonNull WakaTime.Requester requester, @NonNull LifecycleAwareHandler ui) throws Exception {
        Summaries summaries = requester.summaries(currentDate, currentDate, null, null);
        Durations durations = requester.durations(currentDate, null, null);

        final CardsAdapter adapter = new CardsAdapter(this, new CardsAdapter.CardsList()
                .addGlobalSummary(summaries.globalSummary, CardsAdapter.SummaryContext.DAILY_STATS)
                .addDurations(durations)
                .addPieChart(R.string.projects, summaries.globalSummary.projects)
                .addPieChart(R.string.languages, summaries.globalSummary.languages)
                .addPieChart(R.string.editors, summaries.globalSummary.editors)
                .addPieChart(R.string.machines, summaries.globalSummary.machines)
                .addPieChart(R.string.operatingSystems, summaries.globalSummary.operating_systems), this, this);

        ui.post(this, () -> rmv.loadListData(adapter));
    }

    @Override
    public void somethingWentWrong(@NonNull Exception ex) {
        if (ex instanceof WakaTimeException) rmv.showError(ex.getMessage());
        else rmv.showError(R.string.failedLoading_reason, ex.getMessage());
    }
}
