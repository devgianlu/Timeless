package com.gianlu.timeless.Activities;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.gianlu.timeless.NetIO.WakaTime;
import com.gianlu.timeless.Objects.Project;
import com.gianlu.timeless.R;

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
        WakaTime.getInstance().getProjects(new WakaTime.IProjects() {
            @Override
            public void onProjects(List<Project> projects) {
                for (Project project : projects)
                    fragments.add(CommitsFragment.getInstance(project));

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        pager.setAdapter(new PagerAdapter(getSupportFragmentManager(), fragments));
                    }
                });
            }

            @Override
            public void onException(Exception ex) {
                ex.printStackTrace();
            }
        });
    }
}
