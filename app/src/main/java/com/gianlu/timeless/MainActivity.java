package com.gianlu.timeless;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.Toolbar;

import com.gianlu.commonutils.CommonUtils;
import com.gianlu.commonutils.Dialogs.ActivityWithDialog;
import com.gianlu.commonutils.Drawer.BaseDrawerItem;
import com.gianlu.commonutils.Drawer.DrawerManager;
import com.gianlu.commonutils.Toaster;
import com.gianlu.timeless.Activities.CommitsActivity;
import com.gianlu.timeless.Activities.DailyStatsActivity;
import com.gianlu.timeless.Activities.LeadersActivity;
import com.gianlu.timeless.Activities.PrivateLeaderboardsActivity;
import com.gianlu.timeless.Activities.ProjectsActivity;
import com.gianlu.timeless.Main.DrawerConst;
import com.gianlu.timeless.Main.MainFragment;
import com.gianlu.timeless.Main.PagerAdapter;
import com.gianlu.timeless.Models.User;
import com.gianlu.timeless.NetIO.WakaTime;

public class MainActivity extends ActivityWithDialog implements DrawerManager.MenuDrawerListener {
    private DrawerManager<User> drawerManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.main_toolbar);
        setSupportActionBar(toolbar);
        final ViewPager pager = findViewById(R.id.main_pager);
        TabLayout tabLayout = findViewById(R.id.main_tabs);

        User user = (User) getIntent().getSerializableExtra("user");
        if (user == null) {
            Toaster.with(this).message(R.string.failedLoading).ex(new NullPointerException("user is null!")).show();
            onBackPressed();
            return;
        }

        drawerManager = new DrawerManager.Config<User>(this, R.drawable.drawer_background)
                .singleProfile(user, new DrawerManager.OnAction() {
                    @Override
                    public void drawerAction() {
                        PreferenceManager.getDefaultSharedPreferences(MainActivity.this).edit().putBoolean("firstRun", true).apply();
                        startActivity(new Intent(MainActivity.this, GrantActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
                        finish();
                    }
                })
                .addMenuItem(new BaseDrawerItem(DrawerConst.HOME, R.drawable.baseline_home_24, getString(R.string.home)))
                .addMenuItem(new BaseDrawerItem(DrawerConst.DAILY_STATS, R.drawable.baseline_view_day_24, getString(R.string.dailyStats)))
                .addMenuItem(new BaseDrawerItem(DrawerConst.PROJECTS, R.drawable.baseline_view_module_24, getString(R.string.projects)))
                .addMenuItem(new BaseDrawerItem(DrawerConst.COMMITS, R.drawable.baseline_linear_scale_24, getString(R.string.commits)))
                .addMenuItem(new BaseDrawerItem(DrawerConst.PUBLIC_LEADERBOARD, R.drawable.baseline_show_chart_24, getString(R.string.publicLeaderboard)))
                .addMenuItem(new BaseDrawerItem(DrawerConst.PRIVATE_LEADERBOARDS, R.drawable.baseline_vpn_lock_24, getString(R.string.privateLeaderboards)))
                .addMenuItemSeparator()
                .addMenuItem(new BaseDrawerItem(DrawerConst.PREFERENCES, R.drawable.baseline_settings_24, getString(R.string.preferences)))
                .addMenuItem(new BaseDrawerItem(DrawerConst.SUPPORT, R.drawable.baseline_report_problem_24, getString(R.string.support))).build(this, (DrawerLayout) findViewById(R.id.main_drawer), toolbar);

        drawerManager.setActiveItem(DrawerConst.HOME);

        pager.setOffscreenPageLimit(3);
        pager.setAdapter(new PagerAdapter(getSupportFragmentManager(),
                MainFragment.getInstance(this, WakaTime.Range.TODAY),
                MainFragment.getInstance(this, WakaTime.Range.LAST_7_DAYS),
                MainFragment.getInstance(this, WakaTime.Range.LAST_30_DAYS)));

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
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (drawerManager != null) drawerManager.onTogglerConfigurationChanged(newConfig);
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        if (drawerManager != null) drawerManager.syncTogglerState();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (drawerManager != null) drawerManager.syncTogglerState();
    }

    @Override
    public boolean onDrawerMenuItemSelected(@NonNull BaseDrawerItem item) {
        switch (item.id) {
            case DrawerConst.HOME:
                return true;
            case DrawerConst.DAILY_STATS:
                startActivity(new Intent(MainActivity.this, DailyStatsActivity.class));
                return false;
            case DrawerConst.COMMITS:
                startActivity(new Intent(MainActivity.this, CommitsActivity.class));
                return false;
            case DrawerConst.PROJECTS:
                startActivity(new Intent(MainActivity.this, ProjectsActivity.class));
                return false;
            case DrawerConst.PRIVATE_LEADERBOARDS:
                startActivity(new Intent(MainActivity.this, PrivateLeaderboardsActivity.class));
                return false;
            case DrawerConst.PUBLIC_LEADERBOARD:
                LeadersActivity.startActivity(this);
                return false;
            case DrawerConst.PREFERENCES:
                startActivity(new Intent(MainActivity.this, PreferencesActivity.class));
                return false;
            case DrawerConst.SUPPORT:
                CommonUtils.sendEmail(MainActivity.this, getString(R.string.app_name), null);
                return true;
            default:
                return true;
        }
    }
}
