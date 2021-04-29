package com.gianlu.timeless.activities;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.DatePicker;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;

import com.gianlu.commonutils.dialogs.ActivityWithDialog;
import com.gianlu.commonutils.dialogs.MaterialDatePickerDialog;
import com.gianlu.commonutils.lifecycle.LifecycleAwareHandler;
import com.gianlu.commonutils.misc.RecyclerMessageView;
import com.gianlu.commonutils.typography.MaterialColors;
import com.gianlu.timeless.R;
import com.gianlu.timeless.Utils;
import com.gianlu.timeless.api.WakaTime;
import com.gianlu.timeless.api.WakaTimeException;
import com.gianlu.timeless.api.models.Durations;
import com.gianlu.timeless.api.models.Summaries;
import com.gianlu.timeless.listing.CardsAdapter;
import com.gianlu.timeless.listing.PieChartViewHolder.ChartContext;

import java.util.Calendar;
import java.util.Date;

public class DailyStatsActivity extends ActivityWithDialog implements DatePickerDialog.OnDateSetListener, WakaTime.BatchStuff {
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
        } catch (WakaTime.MissingCredentialsException ex) {
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
                return true;
            case R.id.dailyStats_changeDay:
                MaterialDatePickerDialog.get(getString(R.string.selectDay), currentDate,
                        null, new Date(), this)
                        .show(getSupportFragmentManager(), null);
                return true;
            default:
                return false;
        }
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
    public void request(@NonNull WakaTime.Requester requester, @NonNull LifecycleAwareHandler ui) throws Exception {
        Summaries summaries = requester.summaries(currentDate, currentDate, null, null);

        CardsAdapter.CardsList cards = new CardsAdapter.CardsList()
                .addGlobalSummary(summaries.globalSummary, CardsAdapter.SummaryContext.DAILY_STATS);

        try {
            Durations durations = requester.durations(currentDate, null, null);
            cards.addDurations(durations);
        } catch (WakaTime.MissingEndpointException ignored) {
        }

        final CardsAdapter adapter = new CardsAdapter(this, cards
                .addPieChart(R.string.projects, ChartContext.PROJECTS, summaries.globalSummary.interval(), summaries.globalSummary.projects)
                .addPieChart(R.string.languages, ChartContext.IRRELEVANT, summaries.globalSummary.interval(), summaries.globalSummary.languages)
                .addPieChart(R.string.editors, ChartContext.IRRELEVANT, summaries.globalSummary.interval(), summaries.globalSummary.editors)
                .addPieChart(R.string.machines, ChartContext.IRRELEVANT, summaries.globalSummary.interval(), summaries.globalSummary.machines)
                .addPieChart(R.string.operatingSystems, ChartContext.IRRELEVANT, summaries.globalSummary.interval(), summaries.globalSummary.operating_systems), null, this);

        ui.post(this, () -> rmv.loadListData(adapter));
    }

    @Override
    public void somethingWentWrong(@NonNull Exception ex) {
        if (ex instanceof WakaTimeException) rmv.showError(ex.getMessage());
        else rmv.showError(R.string.failedLoading_reason, ex.getMessage());
    }
}
