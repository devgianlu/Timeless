package com.gianlu.timeless;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.gianlu.commonutils.CommonUtils;
import com.gianlu.timeless.Activities.CommitsActivity;
import com.gianlu.timeless.Main.DrawerManager;
import com.gianlu.timeless.Main.MainFragment;
import com.gianlu.timeless.Main.PagerAdapter;
import com.gianlu.timeless.Objects.User;

public class MainActivity extends AppCompatActivity {
    private static User user;
    private DrawerManager drawerManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.main_toolbar);
        setSupportActionBar(toolbar);
        final ViewPager pager = (ViewPager) findViewById(R.id.main_pager);
        TabLayout tabLayout = (TabLayout) findViewById(R.id.main_tabs);

        if (user == null)
            user = (User) getIntent().getSerializableExtra("user");

        drawerManager = new DrawerManager(this, (DrawerLayout) findViewById(R.id.main_drawer))
                .setToolbar(toolbar)
                .setUser(user)
                .buildMenu()
                .setDrawerListener(new DrawerManager.IDrawerListener() {
                    @Override
                    public boolean onListItemSelected(DrawerManager.DrawerListItems which) {
                        switch (which) {
                            case HOME:
                                return true;
                            case COMMITS:
                                startActivity(new Intent(MainActivity.this, CommitsActivity.class));
                                return false;
                            case PREFERENCES:
                                // TODO: Preferences
                                return false;
                            case SUPPORT:
                                CommonUtils.sendEmail(MainActivity.this, getString(R.string.app_name));
                                return true;
                            default:
                                return true;
                        }
                    }

                    @Override
                    public void onLogOut() {
                        deleteFile("token");
                        PreferenceManager.getDefaultSharedPreferences(MainActivity.this).edit().putBoolean("firstRun", true).apply();
                        startActivity(new Intent(MainActivity.this, GrantActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
                        finish();
                    }
                });

        pager.setAdapter(new PagerAdapter(getSupportFragmentManager(),
                MainFragment.getInstance(this, MainFragment.Range.LAST_7_DAYS),
                MainFragment.getInstance(this, MainFragment.Range.LAST_30_DAYS)));

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
}
