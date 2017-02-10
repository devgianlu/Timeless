package com.gianlu.timeless.Activities;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Pair;
import android.view.MenuItem;

import com.borax12.materialdaterangepicker.date.DatePickerDialog;
import com.gianlu.commonutils.CommonUtils;
import com.gianlu.timeless.Activities.Projects.ProjectFragment;
import com.gianlu.timeless.NetIO.WakaTime;
import com.gianlu.timeless.Objects.Project;
import com.gianlu.timeless.R;
import com.gianlu.timeless.Utils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class ProjectsActivity extends AppCompatActivity implements DatePickerDialog.OnDateSetListener {
    private Pair<Date, Date> currentRange;
    private ViewPager pager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_projects);
        setTitle(R.string.projects);

        Toolbar toolbar = (Toolbar) findViewById(R.id.projects_toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null)
            actionBar.setDisplayHomeAsUpEnabled(true);
        pager = (ViewPager) findViewById(R.id.projects_pager);
        pager.setOffscreenPageLimit(4);
        TabLayout tabLayout = (TabLayout) findViewById(R.id.projects_tabs);

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

        final ProgressDialog pd = CommonUtils.fastIndeterminateProgressDialog(this, R.string.loadingData);
        CommonUtils.showDialog(this, pd);

        currentRange = WakaTime.Range.LAST_7_DAYS.getStartAndEnd();
        WakaTime.getInstance().getProjects(this, new WakaTime.IProjects() {
            @Override
            public void onProjects(List<Project> projects) {
                final List<Fragment> fragments = new ArrayList<>();
                for (Project project : projects)
                    fragments.add(ProjectFragment.getInstance(project, currentRange));

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        pager.setAdapter(new PagerAdapter(getSupportFragmentManager(), fragments));
                        pd.dismiss();
                    }
                });
            }

            @Override
            public void onException(Exception ex) {
                CommonUtils.UIToast(ProjectsActivity.this, Utils.ToastMessages.FAILED_LOADING, ex);
                onBackPressed();
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

                Calendar end = Calendar.getInstance();
                end.setTime(currentRange.second);

                DatePickerDialog.newInstance(this,
                        start.get(Calendar.YEAR), start.get(Calendar.MONTH), start.get(Calendar.DAY_OF_MONTH),
                        end.get(Calendar.YEAR), end.get(Calendar.MONTH), end.get(Calendar.DAY_OF_MONTH))
                        .show(getFragmentManager(), "DatePickerDialog");
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDateSet(DatePickerDialog view, int year, int monthOfYear, int dayOfMonth, int yearEnd, int monthOfYearEnd, int dayOfMonthEnd) {
        Calendar start = Calendar.getInstance();
        start.set(year, monthOfYear, dayOfMonth);

        Calendar end = Calendar.getInstance();
        end.set(yearEnd, monthOfYearEnd, dayOfMonthEnd);

        currentRange = new Pair<>(start.getTime(), end.getTime());

        final ProgressDialog pd = CommonUtils.fastIndeterminateProgressDialog(this, R.string.loadingData);
        CommonUtils.showDialog(this, pd);
        WakaTime.getInstance().getProjects(this, new WakaTime.IProjects() {
            @Override
            public void onProjects(List<Project> projects) {
                final List<Fragment> fragments = new ArrayList<>();
                for (Project project : projects)
                    fragments.add(ProjectFragment.getInstance(project, currentRange));

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        pager.setAdapter(new PagerAdapter(getSupportFragmentManager(), fragments));
                        pd.dismiss();
                    }
                });
            }

            @Override
            public void onException(Exception ex) {
                CommonUtils.UIToast(ProjectsActivity.this, Utils.ToastMessages.FAILED_LOADING, ex);
                onBackPressed();
            }
        });
    }
}
