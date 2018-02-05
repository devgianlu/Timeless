package com.gianlu.timeless.Activities;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.TextView;

import com.gianlu.commonutils.Analytics.AnalyticsApplication;
import com.gianlu.commonutils.CommonUtils;
import com.gianlu.commonutils.RecyclerViewLayout;
import com.gianlu.commonutils.Toaster;
import com.gianlu.timeless.Activities.Leaders.LeaderSheet;
import com.gianlu.timeless.Activities.Leaders.LeadersAdapter;
import com.gianlu.timeless.Activities.Leaders.PickLanguageAdapter;
import com.gianlu.timeless.Models.GlobalSummary;
import com.gianlu.timeless.Models.Leader;
import com.gianlu.timeless.Models.Summary;
import com.gianlu.timeless.NetIO.WakaTime;
import com.gianlu.timeless.NetIO.WakaTimeException;
import com.gianlu.timeless.R;
import com.gianlu.timeless.ThisApplication;
import com.gianlu.timeless.Utils;

import java.util.List;

public class LeadersActivity extends AppCompatActivity implements WakaTime.ILeaders, LeadersAdapter.IAdapter {
    private LeadersAdapter adapter;
    private TextView currFilter;
    private String currLang;
    private Leader me;
    private WakaTime wakaTime;
    private RecyclerViewLayout recyclerViewLayout;
    private LeaderSheet sheet;

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
        recyclerViewLayout.disableSwipeRefresh();
        recyclerViewLayout.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        currFilter = findViewById(R.id.leaders_rankingText);
        sheet = new LeaderSheet((ViewGroup) findViewById(R.id.leaders));

        wakaTime = WakaTime.get();
        wakaTime.getLeaders(this);
    }

    @Override
    public void onLeaders(final List<Leader> leaders, Leader me, int maxPages) {
        LeadersActivity.this.me = me;
        adapter = new LeadersAdapter(LeadersActivity.this, leaders, maxPages, me, currLang, this);
        recyclerViewLayout.loadListData(adapter);
    }

    @Override
    public void onException(Exception ex) {
        if (ex instanceof WakaTimeException) {
            recyclerViewLayout.showMessage(ex.getMessage(), false);
        } else {
            recyclerViewLayout.showMessage(R.string.failedLoading_reason, true, ex.getMessage());
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.leaders, menu);
        return true;
    }

    private void gatherAndUpdate(@Nullable final String language) {
        recyclerViewLayout.startLoading();
        wakaTime.getLeaders(language, new WakaTime.ILeaders() {
            @Override
            public void onLeaders(List<Leader> leaders, Leader me, int maxPages) {
                LeadersActivity.this.me = me;
                LeadersActivity.this.adapter = new LeadersAdapter(LeadersActivity.this, leaders, maxPages, me, language, LeadersActivity.this);

                currFilter.setText(language == null ? getString(R.string.global_rank) : language);
                recyclerViewLayout.loadListData(LeadersActivity.this.adapter);

                currLang = language;
            }

            @Override
            public void onException(Exception ex) {
                LeadersActivity.this.onException(ex);
            }
        });
    }

    private void displayRankDialog(Leader leader) {
        sheet.expand(leader);
    }

    @Override
    public void onBackPressed() {
        if (sheet != null && sheet.isExpanded()) {
            sheet.collapse();
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
                else Toaster.show(LeadersActivity.this, Utils.Messages.USER_NOT_FOUND);
                AnalyticsApplication.sendAnalytics(this, Utils.ACTION_SHOW_ME_LEADER);
                break;
            case R.id.leaders_filter:
                showFilterDialog();
                break;
        }
        return true;
    }

    private void showFilterDialog() {
        final ProgressDialog pd = CommonUtils.fastIndeterminateProgressDialog(this, R.string.loadingData);
        CommonUtils.showDialog(this, pd);

        wakaTime.getRangeSummary(WakaTime.Range.LAST_7_DAYS.getStartAndEnd(), new WakaTime.ISummary() {
            @Override
            public void onSummary(List<Summary> summaries, GlobalSummary globalSummary, @Nullable List<String> branches, @Nullable final List<String> selectedBranches) {
                final PickLanguageAdapter adapter = new PickLanguageAdapter(LeadersActivity.this, currLang, globalSummary.languages);
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

                CommonUtils.showDialog(LeadersActivity.this, builder);
                pd.dismiss();
            }

            @Override
            public void onException(Exception ex) {
                Toaster.show(LeadersActivity.this, Utils.Messages.FAILED_LOADING, ex);
                pd.dismiss();
            }
        });
    }

    @Override
    public void onLeaderSelected(Leader leader) {
        displayRankDialog(leader);
    }
}
