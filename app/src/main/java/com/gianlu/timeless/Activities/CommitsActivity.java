package com.gianlu.timeless.Activities;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.gianlu.commonutils.CommonUtils;
import com.gianlu.commonutils.Toaster;
import com.gianlu.timeless.Activities.Commits.CommitsFragment;
import com.gianlu.timeless.Models.Project;
import com.gianlu.timeless.NetIO.WakaTime;
import com.gianlu.timeless.NetIO.WakaTimeException;
import com.gianlu.timeless.R;
import com.gianlu.timeless.Utils;

import java.util.ArrayList;
import java.util.List;

// TODO: Support for branches
public class CommitsActivity extends AppCompatActivity implements WakaTime.IProjects {
    private final List<Fragment> fragments = new ArrayList<>();
    private ViewPager pager;
    private ProgressDialog pd;

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

        pd = CommonUtils.fastIndeterminateProgressDialog(this, R.string.loadingData);
        CommonUtils.showDialog(this, pd);

        WakaTime.getInstance().getProjects(this);
    }

    @Override
    public void onProjects(final List<Project> projects) {
        for (Project project : projects)
            if (project.hasRepository)
                fragments.add(CommitsFragment.getInstance(project));

        pager.setAdapter(new PagerAdapter(getSupportFragmentManager(), fragments));
        pager.setOffscreenPageLimit(fragments.size());
        pd.dismiss();

        String project_id = getIntent().getStringExtra("project_id");
        if (project_id != null) {
            int pos = projects.indexOf(Project.find(project_id, projects));
            if (pos != -1) pager.setCurrentItem(pos, false);
        }
    }

    @Override
    public void onException(Exception ex) {
        Toaster.show(CommitsActivity.this, Utils.ToastMessages.FAILED_LOADING, ex, new Runnable() {
            @Override
            public void run() {
                onBackPressed();
            }
        });
    }

    @Override
    public void onInvalidToken(WakaTimeException ex) {
        Utils.invalidToken(this, ex);
    }
}
