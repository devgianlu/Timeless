package com.gianlu.timeless.Activities;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.gianlu.commonutils.CommonUtils;
import com.gianlu.timeless.Activities.Projects.ProjectFragment;
import com.gianlu.timeless.NetIO.WakaTime;
import com.gianlu.timeless.Objects.Project;
import com.gianlu.timeless.R;
import com.gianlu.timeless.Utils;

import java.util.ArrayList;
import java.util.List;

public class ProjectsActivity extends AppCompatActivity {

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
        final ViewPager pager = (ViewPager) findViewById(R.id.projects_pager);
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

        final List<Fragment> fragments = new ArrayList<>();
        final ProgressDialog pd = CommonUtils.fastIndeterminateProgressDialog(this, R.string.loadingData);
        CommonUtils.showDialog(this, pd);

        WakaTime.getInstance().getProjects(this, new WakaTime.IProjects() {
            @Override
            public void onProjects(List<Project> projects) {
                for (Project project : projects)
                    fragments.add(ProjectFragment.getInstance(project, WakaTime.Range.LAST_7_DAYS));

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
                // TODO: Select date range
                break;
        }

        return super.onOptionsItemSelected(item);
    }
}
