package com.gianlu.timeless.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;

import com.gianlu.commonutils.Analytics.AnalyticsApplication;
import com.gianlu.commonutils.CasualViews.RecyclerMessageView;
import com.gianlu.commonutils.Dialogs.ActivityWithDialog;
import com.gianlu.commonutils.Dialogs.DialogUtils;
import com.gianlu.commonutils.MaterialColors;
import com.gianlu.commonutils.Toaster;
import com.gianlu.timeless.R;
import com.gianlu.timeless.ThisApplication;
import com.gianlu.timeless.Utils;
import com.gianlu.timeless.activities.leaders.LeaderSheet;
import com.gianlu.timeless.activities.leaders.LeadersAdapter;
import com.gianlu.timeless.activities.leaders.PickLanguageAdapter;
import com.gianlu.timeless.api.WakaTime;
import com.gianlu.timeless.api.WakaTimeException;
import com.gianlu.timeless.api.models.Leader;
import com.gianlu.timeless.api.models.Leaders;
import com.gianlu.timeless.api.models.LeadersWithMe;
import com.gianlu.timeless.api.models.Summaries;

public class LeadersActivity extends ActivityWithDialog implements LeadersAdapter.Listener {
    private LeadersAdapter adapter;
    private TextView currFilter;
    private String currLang = null;
    private Leader me = null;
    private WakaTime wakaTime;
    private String id = null;
    private RecyclerMessageView rmv;

    public static void startActivity(Context context, @Nullable String id, @Nullable String name) {
        context.startActivity(new Intent(context, LeadersActivity.class)
                .putExtra("id", id).putExtra("name", name));
    }

    public static void startActivity(Context context) {
        startActivity(context, null, null);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leaders);

        String name = getIntent().getStringExtra("name");
        setTitle(name == null ? getString(R.string.publicLeaderboard) : name);

        Toolbar toolbar = findViewById(R.id.leaders_toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) actionBar.setDisplayHomeAsUpEnabled(true);

        id = getIntent().getStringExtra("id");

        rmv = findViewById(R.id.leaders_recyclerViewLayout);
        rmv.linearLayoutManager(RecyclerView.VERTICAL, false);
        rmv.enableSwipeRefresh(() -> {
            wakaTime.skipNextRequestCache();
            gatherAndUpdate(currLang);
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

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.leaders_me).setVisible(me != null);
        return true;
    }

    private void updateLanguage(@Nullable String language) {
        currLang = language;
        currFilter.setText(language == null ? getString(R.string.allLanguages) : language);
    }

    private void gatherAndUpdate(@Nullable String language) {
        rmv.startLoading();
        if (id == null) {
            wakaTime.getLeaders(language, 1, this, new WakaTime.OnResult<LeadersWithMe>() {
                @Override
                public void onResult(@NonNull LeadersWithMe leaders) {
                    me = leaders.me;
                    adapter = new LeadersAdapter(LeadersActivity.this, wakaTime, leaders, language, LeadersActivity.this);

                    updateLanguage(language);
                    rmv.loadListData(adapter);
                }

                @Override
                public void onException(@NonNull Exception ex) {
                    if (ex instanceof WakaTimeException)
                        rmv.showError(ex.getMessage());
                    else
                        rmv.showError(R.string.failedLoading_reason, ex.getMessage());
                }
            });
        } else {
            wakaTime.getLeaders(id, language, 1, this, new WakaTime.OnResult<Leaders>() {
                @Override
                public void onResult(@NonNull Leaders leaders) {
                    adapter = new LeadersAdapter(LeadersActivity.this, wakaTime, leaders, id, language, LeadersActivity.this);

                    updateLanguage(language);
                    rmv.loadListData(adapter);
                }

                @Override
                public void onException(@NonNull Exception ex) {
                    if (ex instanceof WakaTimeException)
                        rmv.showError(ex.getMessage());
                    else
                        rmv.showError(R.string.failedLoading_reason, ex.getMessage());
                }
            });
        }
    }

    private void displayRankDialog(@NonNull Leader leader) {
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
                AnalyticsApplication.sendAnalytics(Utils.ACTION_SHOW_ME_LEADER);
                if (me != null && me.rank != -1)
                    displayRankDialog(me);
                else
                    Toaster.with(this).message(R.string.userNotFound).extra(me != null ? me.user : null).show();
                break;
            case R.id.leaders_filter:
                showFilterDialog();
                break;
        }

        return true;
    }

    private void showFilterDialog() {
        showDialog(DialogUtils.progressDialog(this, R.string.loadingData));

        wakaTime.getRangeSummary(WakaTime.Range.LAST_7_DAYS.getStartAndEnd(), this, new WakaTime.OnSummary() {
            @Override
            public void onSummary(@NonNull Summaries summaries) {
                final PickLanguageAdapter adapter = new PickLanguageAdapter(LeadersActivity.this, currLang, summaries.globalSummary.languages);
                AlertDialog.Builder builder = new AlertDialog.Builder(LeadersActivity.this);
                builder.setTitle(R.string.filterByLanguage)
                        .setAdapter(adapter, (dialog, which) -> {
                            gatherAndUpdate(adapter.getItem(which).name);
                            ThisApplication.sendAnalytics(Utils.ACTION_FILTER_LEADERS);
                        })
                        .setNeutralButton(R.string.unsetFilter, (dialog, which) -> gatherAndUpdate(null))
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
