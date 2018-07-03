package com.gianlu.timeless.Activities;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.gianlu.commonutils.Analytics.AnalyticsApplication;
import com.gianlu.commonutils.Dialogs.ActivityWithDialog;
import com.gianlu.commonutils.Dialogs.DialogUtils;
import com.gianlu.commonutils.MaterialColors;
import com.gianlu.commonutils.RecyclerViewLayout;
import com.gianlu.commonutils.Toaster;
import com.gianlu.timeless.Activities.Leaders.LeaderSheet;
import com.gianlu.timeless.Activities.Leaders.LeadersAdapter;
import com.gianlu.timeless.Activities.Leaders.PickLanguageAdapter;
import com.gianlu.timeless.Models.Leader;
import com.gianlu.timeless.Models.Leaders;
import com.gianlu.timeless.Models.Summaries;
import com.gianlu.timeless.NetIO.WakaTime;
import com.gianlu.timeless.NetIO.WakaTimeException;
import com.gianlu.timeless.R;
import com.gianlu.timeless.ThisApplication;
import com.gianlu.timeless.Utils;

public class LeadersActivity extends ActivityWithDialog implements LeadersAdapter.Listener {
    private LeadersAdapter adapter;
    private TextView currFilter;
    private String currLang;
    private Leader me;
    private WakaTime wakaTime;
    private RecyclerViewLayout recyclerViewLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leaders);
        setTitle(R.string.leaderboards);

        Toolbar toolbar = findViewById(R.id.leaders_toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) actionBar.setDisplayHomeAsUpEnabled(true);

        recyclerViewLayout = findViewById(R.id.leaders_recyclerViewLayout);
        recyclerViewLayout.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        recyclerViewLayout.enableSwipeRefresh(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                wakaTime.skipNextRequestCache();
                gatherAndUpdate(currLang);
            }
        }, MaterialColors.getInstance().getColorsRes());

        currFilter = findViewById(R.id.leaders_rankingText);

        try {
            wakaTime = WakaTime.get();
            gatherAndUpdate(currLang);
        } catch (WakaTime.ShouldGetAccessToken ex) {
            ex.resolve(this);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.leaders, menu);
        return true;
    }

    private void gatherAndUpdate(@Nullable final String language) {
        recyclerViewLayout.startLoading();
        wakaTime.getLeaders(language, 1, new WakaTime.OnResult<Leaders>() {
            @Override
            public void onResult(@NonNull Leaders leaders) {
                me = leaders.me;
                adapter = new LeadersAdapter(LeadersActivity.this, leaders, leaders.maxPages, me, language, wakaTime, LeadersActivity.this);

                currFilter.setText(language == null ? getString(R.string.global_rank) : language);
                recyclerViewLayout.loadListData(adapter);

                currLang = language;
            }

            @Override
            public void onException(@NonNull Exception ex) {
                if (ex instanceof WakaTimeException)
                    recyclerViewLayout.showError(ex.getMessage());
                else
                    recyclerViewLayout.showError(R.string.failedLoading_reason, ex.getMessage());
            }
        });
    }

    private void displayRankDialog(Leader leader) {
        LeaderSheet.get().show(this, leader);
    }

    @Override
    public void onBackPressed() {
        if (hasVisibleDialog()) {
            dismissDialog();
            return;
        }

        super.onBackPressed();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
            case R.id.leaders_me:
                if (me != null && me.rank != -1) displayRankDialog(me);
                else
                    Toaster.with(this).message(R.string.userNotFound).extra(me != null ? me.user : null).show();
                AnalyticsApplication.sendAnalytics(this, Utils.ACTION_SHOW_ME_LEADER);
                break;
            case R.id.leaders_filter:
                showFilterDialog();
                break;
        }

        return true;
    }

    private void showFilterDialog() {
        showDialog(DialogUtils.progressDialog(this, R.string.loadingData));

        wakaTime.getRangeSummary(WakaTime.Range.LAST_7_DAYS.getStartAndEnd(), new WakaTime.OnSummary() {
            @Override
            public void onSummary(@NonNull Summaries summaries) {
                final PickLanguageAdapter adapter = new PickLanguageAdapter(LeadersActivity.this, currLang, summaries.globalSummary.languages);
                AlertDialog.Builder builder = new AlertDialog.Builder(LeadersActivity.this);
                builder.setTitle(R.string.filterByLanguage)
                        .setAdapter(adapter, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                gatherAndUpdate(adapter.getItem(which).name);
                                ThisApplication.sendAnalytics(LeadersActivity.this, Utils.ACTION_FILTER_LEADERS);
                            }
                        })
                        .setNeutralButton(R.string.unsetFilter, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                gatherAndUpdate(null);
                            }
                        })
                        .setNegativeButton(android.R.string.cancel, null);

                dismissDialog();
                showDialog(builder);
            }

            @Override
            public void onWakaTimeError(@NonNull WakaTimeException ex) {
                onException(ex); // Shouldn't happen
            }

            @Override
            public void onException(@NonNull Exception ex) {
                Toaster.with(LeadersActivity.this).message(R.string.failedLoading).ex(ex).show();
                dismissDialog();
            }
        });
    }

    @Override
    public void onLeaderSelected(@NonNull Leader leader) {
        displayRankDialog(leader);
    }
}
