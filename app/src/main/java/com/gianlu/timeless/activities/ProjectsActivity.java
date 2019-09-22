package com.gianlu.timeless.activities;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Pair;
import android.view.MenuItem;
import android.widget.DatePicker;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import com.gianlu.commonutils.Dialogs.ActivityWithDialog;
import com.gianlu.commonutils.Dialogs.DialogUtils;
import com.gianlu.commonutils.Dialogs.MaterialDatePickerDialog;
import com.gianlu.commonutils.Toaster;
import com.gianlu.timeless.R;
import com.gianlu.timeless.ThisApplication;
import com.gianlu.timeless.Utils;
import com.gianlu.timeless.activities.projects.ProjectFragment;
import com.gianlu.timeless.api.WakaTime;
import com.gianlu.timeless.api.models.Project;
import com.gianlu.timeless.api.models.Projects;
import com.google.android.material.tabs.TabLayout;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ProjectsActivity extends ActivityWithDialog implements DatePickerDialog.OnDateSetListener, WakaTime.OnResult<Projects> {
    private Pair<Date, Date> currentRange;
    private ViewPager pager;
    private Date tmpStart;
    private WakaTime wakaTime;
    private TextView rangeText;

    public static void startActivity(@NonNull Context context, @Nullable Pair<Date, Date> interval, @Nullable String projectName) {
        context.startActivity(startIntent(context, interval, projectName));
    }

    @NonNull
    public static Intent startIntent(@NonNull Context context, @Nullable Pair<Date, Date> interval, @Nullable String projectName) {
        Intent intent = new Intent(context, ProjectsActivity.class);
        if (projectName != null) intent.putExtra("projectName", projectName);
        if (interval != null) {
            intent.putExtra("intervalStart", interval.first);
            intent.putExtra("intervalEnd", interval.second);
        }

        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_projects);
        setTitle(R.string.projects);

        Toolbar toolbar = findViewById(R.id.projects_toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) actionBar.setDisplayHomeAsUpEnabled(true);

        rangeText = findViewById(R.id.projects_rangeText);

        pager = findViewById(R.id.projects_pager);
        pager.setOffscreenPageLimit(4);
        TabLayout tabLayout = findViewById(R.id.projects_tabs);

        tabLayout.setupWithViewPager(pager);
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                pager.setCurrentItem(tab.getPosition(), true);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });

        showDialog(DialogUtils.progressDialog(this, R.string.loadingData));

        Date start = (Date) getIntent().getSerializableExtra("intervalStart");
        Date end = (Date) getIntent().getSerializableExtra("intervalEnd");
        if (start != null && end != null) currentRange = new Pair<>(start, end);
        else currentRange = WakaTime.Range.LAST_7_DAYS.getStartAndEnd();

        try {
            wakaTime = WakaTime.get();
        } catch (WakaTime.ShouldGetAccessToken ex) {
            ex.resolve(this);
            return;
        }

        updateRangeText();
        wakaTime.getProjects(null, this);
    }

    @Override
    public void onResult(@NonNull Projects projects) {
        List<Fragment> fragments = new ArrayList<>();
        for (Project project : projects)
            fragments.add(ProjectFragment.getInstance(project, currentRange));

        pager.setAdapter(new PagerAdapter(getSupportFragmentManager(), fragments));
        dismissDialog();

        String projectName = getIntent().getStringExtra("projectName");
        if (projectName != null) {
            int pos = projects.indexOfName(projectName);
            if (pos != -1) pager.setCurrentItem(pos, false);
        }
    }

    @Override
    public void onException(@NonNull Exception ex) {
        Toaster.with(this).message(R.string.failedLoading).ex(ex).show();
        onBackPressed();
    }

    public void updateRangeText() {
        SimpleDateFormat formatter = new SimpleDateFormat("dd MMM", Locale.getDefault());
        if (currentRange.first.getTime() == currentRange.second.getTime())
            rangeText.setText(formatter.format(currentRange.first));
        else
            rangeText.setText(formatter.format(currentRange.first) + " - " + formatter.format(currentRange.second));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
            case R.id.projects_range:
                MaterialDatePickerDialog.get(getString(R.string.selectStartDate),
                        currentRange.first, null, new Date(), (view, year, month, dayOfMonth) -> {
                            Calendar start = Calendar.getInstance();
                            start.setTimeInMillis(0);
                            start.set(year, month, dayOfMonth);
                            tmpStart = start.getTime();

                            MaterialDatePickerDialog.get(getString(R.string.selectEndDate),
                                    currentRange.second, tmpStart, new Date(), ProjectsActivity.this)
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

        showDialog(DialogUtils.progressDialog(this, R.string.loadingData));
        wakaTime.getProjects(this, new WakaTime.OnResult<Projects>() {
            @Override
            public void onResult(@NonNull Projects projects) {
                List<Fragment> fragments = new ArrayList<>();
                for (Project project : projects)
                    fragments.add(ProjectFragment.getInstance(project, currentRange));

                int sel = pager.getCurrentItem();
                pager.setAdapter(new PagerAdapter(getSupportFragmentManager(), fragments));
                pager.setCurrentItem(sel, false);
                dismissDialog();
            }

            @Override
            public void onException(@NonNull Exception ex) {
                dismissDialog();
                Toaster.with(ProjectsActivity.this).message(R.string.failedLoading).ex(ex).show();
                onBackPressed();
            }
        });

        ThisApplication.sendAnalytics(Utils.ACTION_DATE_RANGE);
    }
}
