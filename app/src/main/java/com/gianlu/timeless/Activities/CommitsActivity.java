package com.gianlu.timeless.Activities;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;

import com.gianlu.commonutils.Dialogs.ActivityWithDialog;
import com.gianlu.commonutils.Dialogs.DialogUtils;
import com.gianlu.commonutils.Toaster;
import com.gianlu.timeless.Activities.Commits.CommitsFragment;
import com.gianlu.timeless.Models.Project;
import com.gianlu.timeless.NetIO.WakaTime;
import com.gianlu.timeless.R;
import com.gianlu.timeless.Utils;

import java.util.ArrayList;
import java.util.List;

public class CommitsActivity extends ActivityWithDialog implements WakaTime.OnProjects {
    private final List<CommitsFragment> fragments = new ArrayList<>();
    private ViewPager pager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_commits);
        setTitle(R.string.commits);

        Toolbar toolbar = findViewById(R.id.commits_toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) actionBar.setDisplayHomeAsUpEnabled(true);
        pager = findViewById(R.id.commits_pager);
        pager.setOffscreenPageLimit(4);
        TabLayout tabLayout = findViewById(R.id.commits_tabs);

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
        WakaTime.get().getProjects(this);
    }

    @Override
    public void onProjects(List<Project> projects) {
        for (Project project : projects)
            if (project.hasRepository)
                fragments.add(CommitsFragment.getInstance(project));

        pager.setAdapter(new PagerAdapter(getSupportFragmentManager(), fragments));
        pager.setOffscreenPageLimit(fragments.size());
        dismissDialog();

        String project_id = getIntent().getStringExtra("project_id");
        if (project_id != null) {
            int pos = projects.indexOf(Project.find(project_id, projects));
            if (pos != -1) pager.setCurrentItem(pos, false);
        }
    }

    @Override
    public void onBackPressed() {
        if (fragments.isEmpty()) {
            super.onBackPressed();
            return;
        }

        CommitsFragment fragment = fragments.get(pager.getCurrentItem());
        if (fragment.onBackPressed()) super.onBackPressed();
    }

    @Override
    public void onException(Exception ex) {
        Toaster.show(CommitsActivity.this, Utils.Messages.FAILED_LOADING, ex, new Runnable() {
            @Override
            public void run() {
                onBackPressed();
            }
        });
    }
}
