package com.gianlu.timeless.Activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.gianlu.commonutils.Dialogs.ActivityWithDialog;
import com.gianlu.commonutils.Dialogs.DialogUtils;
import com.gianlu.commonutils.Toaster;
import com.gianlu.timeless.Activities.Commits.CommitsFragment;
import com.gianlu.timeless.Models.Project;
import com.gianlu.timeless.Models.Projects;
import com.gianlu.timeless.NetIO.WakaTime;
import com.gianlu.timeless.R;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;

public class CommitsActivity extends ActivityWithDialog implements WakaTime.OnResult<Projects> {
    private final List<CommitsFragment> fragments = new ArrayList<>();
    private ViewPager pager;

    public static void startActivity(Context context, @Nullable String projectId) {
        context.startActivity(new Intent(context, CommitsActivity.class)
                .putExtra("projectId", projectId));
    }

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

        try {
            showDialog(DialogUtils.progressDialog(this, R.string.loadingData));
            WakaTime.get().getProjects(this);
        } catch (WakaTime.ShouldGetAccessToken ex) {
            dismissDialog();
            ex.resolve(this);
        }
    }

    @Override
    public void onResult(@NonNull Projects projects) {
        for (Project project : projects)
            if (project.hasRepository)
                fragments.add(CommitsFragment.getInstance(project));

        pager.setAdapter(new PagerAdapter(getSupportFragmentManager(), fragments));
        pager.setOffscreenPageLimit(fragments.size());
        dismissDialog();

        String projectId = getIntent().getStringExtra("projectId");
        if (projectId != null) {
            int pos = projects.indexOf(projectId);
            if (pos != -1) pager.setCurrentItem(pos, false);
            getIntent().removeExtra("projectId");
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
    public void onException(@NonNull Exception ex) {
        Toaster.with(this).message(R.string.failedLoading).ex(ex).show();
        onBackPressed();
    }
}
