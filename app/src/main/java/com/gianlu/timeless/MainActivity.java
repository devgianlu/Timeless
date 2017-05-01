package com.gianlu.timeless;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.DrawableRes;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.gianlu.commonutils.CommonUtils;
import com.gianlu.commonutils.Drawer.BaseDrawerItem;
import com.gianlu.commonutils.Drawer.BaseDrawerProfile;
import com.gianlu.commonutils.Drawer.DrawerManager;
import com.gianlu.commonutils.Drawer.ProfilesAdapter;
import com.gianlu.timeless.Activities.CommitsActivity;
import com.gianlu.timeless.Activities.LeadersActivity;
import com.gianlu.timeless.Activities.ProjectsActivity;
import com.gianlu.timeless.Main.DrawerConst;
import com.gianlu.timeless.Main.MainFragment;
import com.gianlu.timeless.Main.PagerAdapter;
import com.gianlu.timeless.Models.User;
import com.gianlu.timeless.NetIO.WakaTime;

import java.util.List;

public class MainActivity extends AppCompatActivity implements DrawerManager.ISetup {
    private DrawerManager drawerManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.main_toolbar);
        setSupportActionBar(toolbar);
        final ViewPager pager = (ViewPager) findViewById(R.id.main_pager);
        TabLayout tabLayout = (TabLayout) findViewById(R.id.main_tabs);

        User user = (User) getIntent().getSerializableExtra("user");
        if (user == null) {
            CommonUtils.UIToast(this, Utils.ToastMessages.FAILED_LOADING, new NullPointerException("user is null!"));
            onBackPressed();
            return;
        }

        drawerManager = DrawerManager.setup(this, (DrawerLayout) findViewById(R.id.main_drawer), toolbar, this)
                .hasSingleProfile(user, new DrawerManager.ILogout() {
                    @Override
                    public void logout() {
                        deleteFile("token");
                        PreferenceManager.getDefaultSharedPreferences(MainActivity.this).edit().putBoolean("firstRun", true).apply();
                        startActivity(new Intent(MainActivity.this, GrantActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
                        finish();
                    }
                })
                .addMenuItem(new BaseDrawerItem(DrawerConst.HOME, R.drawable.ic_home_black_48dp, getString(R.string.home)))
                .addMenuItem(new BaseDrawerItem(DrawerConst.PROJECTS, R.drawable.ic_view_module_black_48dp, getString(R.string.projects)))
                .addMenuItem(new BaseDrawerItem(DrawerConst.COMMITS, R.drawable.ic_linear_scale_black_48dp, getString(R.string.commits)))
                .addMenuItem(new BaseDrawerItem(DrawerConst.LEADERS, R.drawable.ic_show_chart_black_48dp, getString(R.string.leaderboards)))
                .addMenuItemSeparator()
                .addMenuItem(new BaseDrawerItem(DrawerConst.PREFERENCES, R.drawable.ic_settings_black_48dp, getString(R.string.preferences)))
                .addMenuItem(new BaseDrawerItem(DrawerConst.SUPPORT, R.drawable.ic_report_problem_black_48dp, getString(R.string.support)))
                .build();

        drawerManager.setDrawerListener(new DrawerManager.IDrawerListener() {
            @Override
            public boolean onMenuItemSelected(BaseDrawerItem which) {
                switch (which.id) {
                    case DrawerConst.HOME:
                        return true;
                    case DrawerConst.COMMITS:
                        startActivity(new Intent(MainActivity.this, CommitsActivity.class));
                        return false;
                    case DrawerConst.PROJECTS:
                        startActivity(new Intent(MainActivity.this, ProjectsActivity.class));
                        return false;
                    case DrawerConst.LEADERS:
                        startActivity(new Intent(MainActivity.this, LeadersActivity.class));
                        return false;
                    case DrawerConst.PREFERENCES:
                        startActivity(new Intent(MainActivity.this, PreferencesActivity.class));
                        return false;
                    case DrawerConst.SUPPORT:
                        CommonUtils.sendEmail(MainActivity.this, getString(R.string.app_name));
                        return true;
                    default:
                        return true;
                }
            }

            @Override
            public void onProfileSelected(BaseDrawerProfile profile) {

            }

            @Override
            public void addProfile() {

            }

            @Override
            public void editProfile(List<BaseDrawerProfile> items) {

            }
        });

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
        if (drawerManager != null)
            drawerManager.onTogglerConfigurationChanged(newConfig);
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        if (drawerManager != null)
            drawerManager.syncTogglerState();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (drawerManager != null)
            drawerManager.syncTogglerState();
    }

    @Override
    public int getColorAccent() {
        return R.color.colorAccent;
    }

    @Override
    public int getHeaderBackground() {
        return R.drawable.drawer_background;
    }

    @Override
    public int getOpenDrawerDesc() {
        return R.string.openDrawer;
    }

    @Override
    public int getCloseDrawerDesc() {
        return R.string.closeDrawer;
    }

    @Override
    public int getRippleDark() {
        return R.drawable.ripple_effect_dark;
    }

    @Override
    public int getDrawerBadge() {
        return 0; // Not needed
    }

    @Override
    public int getColorPrimaryShadow() {
        return R.color.colorPrimary_shadow;
    }

    @Override
    public int getColorPrimary() {
        return R.color.colorPrimary;
    }

    @Override
    public ProfilesAdapter getProfilesAdapter(Context context, List<BaseDrawerProfile> profiles, @DrawableRes int ripple_dark, DrawerManager.IDrawerListener listener) {
        return null;
    }
}
