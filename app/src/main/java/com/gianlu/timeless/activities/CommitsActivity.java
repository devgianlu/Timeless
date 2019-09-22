package com.gianlu.timeless.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;

import com.gianlu.commonutils.Dialogs.ActivityWithDialog;
import com.gianlu.commonutils.Dialogs.DialogUtils;
import com.gianlu.commonutils.Toaster;
import com.gianlu.timeless.R;
import com.gianlu.timeless.activities.commits.CommitsFragment;
import com.gianlu.timeless.api.WakaTime;
import com.gianlu.timeless.api.models.Project;
import com.gianlu.timeless.api.models.Projects;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;

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
            WakaTime.get().getProjects(null, this);
        } catch (WakaTime.ShouldGetAccessToken ex) {
            dismissDialog();
            ex.resolve(this);
        }
    }

    @Override
    public void onResult(@NonNull Projects projects) {
        projects.filterNoRepository();
        for (Project project : projects)
            fragments.add(CommitsFragment.getInstance(project));

        pager.setAdapter(new PagerAdapter(getSupportFragmentManager(), fragments));
        pager.setOffscreenPageLimit(fragments.size());
        dismissDialog();

        String projectId = getIntent().getStringExtra("projectId");
        if (projectId != null) {
            int pos = projects.indexOfId(projectId);
            if (pos != -1) pager.setCurrentItem(pos, false);
            getIntent().removeExtra("projectId");
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            super.onBackPressed();
            return true;
        }

        return super.onOptionsItemSelected(item);
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
