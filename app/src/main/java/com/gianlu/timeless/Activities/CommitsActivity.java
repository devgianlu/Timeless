package com.gianlu.timeless.Activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.gianlu.commonutils.CommonUtils;
import com.gianlu.timeless.Activities.Commits.CommitsFragment;
import com.gianlu.timeless.GrantActivity;
import com.gianlu.timeless.NetIO.WakaTime;
import com.gianlu.timeless.NetIO.WakaTimeException;
import com.gianlu.timeless.Objects.Project;
import com.gianlu.timeless.R;
import com.gianlu.timeless.Utils;

import java.util.ArrayList;
import java.util.List;

public class CommitsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_commits);
        setTitle(R.string.commits);

        Toolbar toolbar = (Toolbar) findViewById(R.id.commits_toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null)
            actionBar.setDisplayHomeAsUpEnabled(true);
        final ViewPager pager = (ViewPager) findViewById(R.id.commits_pager);
        TabLayout tabLayout = (TabLayout) findViewById(R.id.commits_tabs);

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
            public void onProjects(final List<Project> projects) {
                for (Project project : projects)
                    if (project.hasRepository)
                        fragments.add(CommitsFragment.getInstance(project));

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        pager.setAdapter(new PagerAdapter(getSupportFragmentManager(), fragments));
                        pager.setOffscreenPageLimit(fragments.size());
                        pd.dismiss();

                        String project_id = getIntent().getStringExtra("project_id");
                        if (project_id != null) {
                            int pos = projects.indexOf(Project.find(project_id, projects));
                            if (pos != -1)
                                pager.setCurrentItem(pos, false);
                        }
                    }
                });
            }

            @Override
            public void onException(Exception ex) {
                CommonUtils.UIToast(CommitsActivity.this, Utils.ToastMessages.FAILED_LOADING, ex);
                onBackPressed();
            }

            @Override
            public void onWakaTimeException(WakaTimeException ex) {
                CommonUtils.UIToast(CommitsActivity.this, Utils.ToastMessages.INVALID_TOKEN, ex);
                startActivity(new Intent(CommitsActivity.this, GrantActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
            }
        });
    }
}
