package com.gianlu.timeless;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;

import com.gianlu.commonutils.dialogs.ActivityWithDialog;
import com.gianlu.commonutils.drawer.BaseDrawerItem;
import com.gianlu.commonutils.drawer.DrawerManager;
import com.gianlu.commonutils.logs.LogsHelper;
import com.gianlu.commonutils.preferences.Prefs;
import com.gianlu.commonutils.ui.Toaster;
import com.gianlu.timeless.activities.CommitsActivity;
import com.gianlu.timeless.activities.CustomRangeStatsActivity;
import com.gianlu.timeless.activities.DailyStatsActivity;
import com.gianlu.timeless.activities.LeadersActivity;
import com.gianlu.timeless.activities.LifetimeStatsActivity;
import com.gianlu.timeless.activities.PrivateLeaderboardsActivity;
import com.gianlu.timeless.activities.ProjectsActivity;
import com.gianlu.timeless.api.WakaTime;
import com.gianlu.timeless.api.models.User;
import com.gianlu.timeless.main.DrawerItem;
import com.gianlu.timeless.main.MainFragment;
import com.gianlu.timeless.main.PagerAdapter;
import com.google.android.material.tabs.TabLayout;

public class MainActivity extends ActivityWithDialog implements DrawerManager.MenuDrawerListener<DrawerItem> {
    private DrawerManager<User, DrawerItem> drawerManager;

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
            Toaster.with(this).message(R.string.failedLoading).show();
            onBackPressed();
            return;
        }

        drawerManager = new DrawerManager.Config<User, DrawerItem>(this)
                .singleProfile(user, () -> {
                    Prefs.putBoolean(PK.FIRST_RUN, true);
                    Prefs.remove(PK.API_KEY);
                    Prefs.remove(PK.TOKEN_RAW);

                    startActivity(new Intent(MainActivity.this, GrantActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
                    finish();
                })
                .addMenuItem(new BaseDrawerItem<>(DrawerItem.DAILY_STATS, R.drawable.baseline_view_day_24, getString(R.string.dailyStats)))
                .addMenuItem(new BaseDrawerItem<>(DrawerItem.LIFETIME_STATS, R.drawable.baseline_poll_24, getString(R.string.lifetimeStats)))
                .addMenuItem(new BaseDrawerItem<>(DrawerItem.CUSTOM_RANGE_STATS, R.drawable.baseline_date_range_24, getString(R.string.customRangeStats)))
                .addMenuItem(new BaseDrawerItem<>(DrawerItem.PROJECTS, R.drawable.baseline_view_module_24, getString(R.string.projects)))
                .addMenuItem(new BaseDrawerItem<>(DrawerItem.COMMITS, R.drawable.baseline_linear_scale_24, getString(R.string.commits)))
                .addMenuItem(new BaseDrawerItem<>(DrawerItem.PUBLIC_LEADERBOARD, R.drawable.baseline_show_chart_24, getString(R.string.publicLeaderboard)))
                .addMenuItem(new BaseDrawerItem<>(DrawerItem.PRIVATE_LEADERBOARDS, R.drawable.baseline_vpn_lock_24, getString(R.string.privateLeaderboards)))
                .addMenuItemSeparator()
                .addMenuItem(new BaseDrawerItem<>(DrawerItem.PREFERENCES, R.drawable.baseline_settings_24, getString(R.string.preferences)))
                .addMenuItem(new BaseDrawerItem<>(DrawerItem.SUPPORT, R.drawable.baseline_report_problem_24, getString(R.string.support))).build(this, findViewById(R.id.main_drawer), toolbar);

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
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
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
    public boolean onDrawerMenuItemSelected(@NonNull BaseDrawerItem<DrawerItem> item) {
        switch (item.id) {
            case DAILY_STATS:
                startActivity(new Intent(this, DailyStatsActivity.class));
                return false;
            case LIFETIME_STATS:
                startActivity(new Intent(this, LifetimeStatsActivity.class));
                return false;
            case CUSTOM_RANGE_STATS:
                startActivity(new Intent(this, CustomRangeStatsActivity.class));
                return false;
            case COMMITS:
                CommitsActivity.startActivity(this, null);
                return false;
            case PROJECTS:
                ProjectsActivity.startActivity(this, null, null);
                return false;
            case PRIVATE_LEADERBOARDS:
                startActivity(new Intent(this, PrivateLeaderboardsActivity.class));
                return false;
            case PUBLIC_LEADERBOARD:
                LeadersActivity.startActivity(this);
                return false;
            case PREFERENCES:
                startActivity(new Intent(this, PreferenceActivity.class));
                return false;
            case SUPPORT:
                LogsHelper.openGithubIssue(this,"Timeless", null);
                return true;
            default:
                return true;
        }
    }
}
