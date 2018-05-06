package com.gianlu.timeless.Activities;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.util.Pair;
import android.view.MenuItem;
import android.widget.TextView;

import com.gianlu.commonutils.Dialogs.ActivityWithDialog;
import com.gianlu.commonutils.Dialogs.DialogUtils;
import com.gianlu.commonutils.Toaster;
import com.gianlu.timeless.Activities.Projects.ProjectFragment;
import com.gianlu.timeless.Models.Project;
import com.gianlu.timeless.NetIO.WakaTime;
import com.gianlu.timeless.R;
import com.gianlu.timeless.ThisApplication;
import com.gianlu.timeless.Utils;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class ProjectsActivity extends ActivityWithDialog implements DatePickerDialog.OnDateSetListener, WakaTime.OnProjects {
    private Pair<Date, Date> currentRange;
    private ViewPager pager;
    private Date tmpStart;
    private WakaTime wakaTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_projects);
        setTitle(R.string.projects);

        Toolbar toolbar = findViewById(R.id.projects_toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) actionBar.setDisplayHomeAsUpEnabled(true);

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
        wakaTime.getProjects(this);
    }

    @Override
    public void onProjects(final List<Project> projects) {
        final List<Fragment> fragments = new ArrayList<>();
        for (Project project : projects)
            fragments.add(ProjectFragment.getInstance(project, currentRange));

        pager.setAdapter(new PagerAdapter(getSupportFragmentManager(), fragments));
        dismissDialog();

        String project_id = getIntent().getStringExtra("project_id");
        if (project_id != null) {
            int pos = projects.indexOf(Project.find(project_id, projects));
            if (pos != -1) pager.setCurrentItem(pos, false);
        }
    }

    @Override
    public void onException(Exception ex) {
        Toaster.show(ProjectsActivity.this, Utils.Messages.FAILED_LOADING, ex, new Runnable() {
            @Override
            public void run() {
                onBackPressed();
            }
        });
    }

    @SuppressLint("SetTextI18n")
    public void updateRangeText() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                SimpleDateFormat formatter = new SimpleDateFormat("dd MMM", Locale.getDefault());
                TextView rangeTextView = findViewById(R.id.projects_rangeText);
                if (currentRange.first.getTime() == currentRange.second.getTime())
                    rangeTextView.setText(formatter.format(currentRange.first));
                else
                    rangeTextView.setText(formatter.format(currentRange.first) + " - " + formatter.format(currentRange.second));
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
            case R.id.projects_range:
                Calendar start = Calendar.getInstance();
                start.setTime(currentRange.first);

                DatePickerDialog dialog = DatePickerDialog.newInstance(this,
                        start.get(Calendar.YEAR),
                        start.get(Calendar.MONTH),
                        start.get(Calendar.DAY_OF_MONTH));

                dialog.setTitle(getString(R.string.selectStartDate));
                dialog.show(getFragmentManager(), "START");
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
            dialog.show(getFragmentManager(), "END");
            return;
        }

        Calendar end = Calendar.getInstance();
        end.setTimeInMillis(0);
        end.set(year, monthOfYear, dayOfMonth);

        currentRange = new Pair<>(tmpStart, end.getTime());
        updateRangeText();

        showDialog(DialogUtils.progressDialog(this, R.string.loadingData));
        wakaTime.getProjects(new WakaTime.OnProjects() {
            @Override
            public void onProjects(List<Project> projects) {
                final List<Fragment> fragments = new ArrayList<>();
                for (Project project : projects)
                    fragments.add(ProjectFragment.getInstance(project, currentRange));

                int sel = pager.getCurrentItem();
                pager.setAdapter(new PagerAdapter(getSupportFragmentManager(), fragments));
                pager.setCurrentItem(sel, false);
                dismissDialog();
            }

            @Override
            public void onException(Exception ex) {
                dismissDialog();
                Toaster.show(ProjectsActivity.this, Utils.Messages.FAILED_LOADING, ex, new Runnable() {
                    @Override
                    public void run() {
                        onBackPressed();
                    }
                });
            }
        });

        ThisApplication.sendAnalytics(this, Utils.ACTION_DATE_RANGE);
    }
}
