package com.gianlu.timeless.Activities;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.gianlu.commonutils.CommonUtils;
import com.gianlu.commonutils.InfiniteRecyclerView;
import com.gianlu.timeless.Activities.Leaders.LeadersAdapter;
import com.gianlu.timeless.Activities.Leaders.PickLanguageAdapter;
import com.gianlu.timeless.GrantActivity;
import com.gianlu.timeless.Models.Leader;
import com.gianlu.timeless.Models.Summary;
import com.gianlu.timeless.NetIO.WakaTime;
import com.gianlu.timeless.NetIO.WakaTimeException;
import com.gianlu.timeless.R;
import com.gianlu.timeless.ThisApplication;
import com.gianlu.timeless.Utils;
import com.google.android.gms.analytics.HitBuilders;

import java.util.List;

public class LeadersActivity extends AppCompatActivity implements WakaTime.ILeaders {
    private LeadersAdapter adapter;
    private TextView currFilter;
    private String currLang;
    private InfiniteRecyclerView list;
    private Leader me;
    private ProgressDialog pd;
    private SwipeRefreshLayout layout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leaders);
        setTitle(R.string.leaderboards);

        Toolbar toolbar = (Toolbar) findViewById(R.id.leaders_toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) actionBar.setDisplayHomeAsUpEnabled(true);

        layout = (SwipeRefreshLayout) findViewById(R.id.leaders_swipeRefresh);
        layout.setColorSchemeResources(Utils.getColors());
        currFilter = (TextView) findViewById(R.id.leaders_rankingText);
        list = (InfiniteRecyclerView) findViewById(R.id.leaders_list);
        list.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));

        layout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                WakaTime.getInstance().getLeaders(LeadersActivity.this, LeadersActivity.this);
            }
        });


        pd = CommonUtils.fastIndeterminateProgressDialog(this, R.string.loadingData);
        CommonUtils.showDialog(this, pd);

        WakaTime.getInstance().getLeaders(this, this);
    }

    @Override
    public void onLeaders(final List<Leader> leaders, Leader me, int maxPages) {
        LeadersActivity.this.me = me;
        adapter = new LeadersAdapter(LeadersActivity.this, leaders, maxPages, me);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                list.setAdapter(adapter);
                layout.setRefreshing(false);
                pd.dismiss();
            }
        });
    }

    @Override
    public void onException(Exception ex) {
        if (layout.isRefreshing()) {
            CommonUtils.UIToast(LeadersActivity.this, Utils.ToastMessages.FAILED_REFRESHING, ex, new Runnable() {
                @Override
                public void run() {
                    layout.setRefreshing(false);
                }
            });
        } else {
            CommonUtils.UIToast(LeadersActivity.this, Utils.ToastMessages.FAILED_LOADING, ex, new Runnable() {
                @Override
                public void run() {
                    pd.dismiss();
                    onBackPressed();
                }
            });
        }
    }

    @Override
    public void onWakaTimeException(WakaTimeException ex) {
        CommonUtils.UIToast(LeadersActivity.this, Utils.ToastMessages.INVALID_TOKEN, ex, new Runnable() {
            @Override
            public void run() {
                pd.dismiss();
                layout.setRefreshing(false);
                startActivity(new Intent(LeadersActivity.this, GrantActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.leaders, menu);
        return true;
    }

    private void gatherAndUpdate(@Nullable final String language) {
        final ProgressDialog pd = CommonUtils.fastIndeterminateProgressDialog(this, R.string.loadingData);
        CommonUtils.showDialog(LeadersActivity.this, pd);
        WakaTime.getInstance().getLeaders(LeadersActivity.this, language, new WakaTime.ILeaders() {
            @Override
            public void onLeaders(List<Leader> leaders, Leader me, int maxPages) {
                LeadersActivity.this.me = me;
                LeadersActivity.this.adapter = new LeadersAdapter(LeadersActivity.this, leaders, maxPages, me);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        currFilter.setText(language == null ? getString(R.string.global_rank) : language);
                        list.setAdapter(LeadersActivity.this.adapter);
                        pd.dismiss();
                    }
                });

                currLang = language;
            }

            @Override
            public void onException(Exception ex) {
                CommonUtils.UIToast(LeadersActivity.this, Utils.ToastMessages.FAILED_LOADING, ex);
                pd.dismiss();
            }

            @Override
            public void onWakaTimeException(WakaTimeException ex) {
                CommonUtils.UIToast(LeadersActivity.this, Utils.ToastMessages.INVALID_TOKEN, ex);
                startActivity(new Intent(LeadersActivity.this, GrantActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
            case R.id.leaders_me:
                if (me != null) LeadersAdapter.displayRankDialog(this, me);
                else CommonUtils.UIToast(LeadersActivity.this, Utils.ToastMessages.USER_NOT_FOUND);

                ThisApplication.sendAnalytics(this, new HitBuilders.EventBuilder()
                        .setCategory(ThisApplication.CATEGORY_USER_INPUT)
                        .setAction(ThisApplication.ACTION_SHOW_ME_LEADER)
                        .build());
                break;
            case R.id.leaders_filter:
                final ProgressDialog pd = CommonUtils.fastIndeterminateProgressDialog(this, R.string.loadingData);
                CommonUtils.showDialog(this, pd);

                WakaTime.getInstance().getRangeSummary(WakaTime.Range.LAST_7_DAYS.getStartAndEnd(), new WakaTime.ISummary() {
                    @Override
                    public void onSummary(List<Summary> summaries, Summary summary) {
                        pd.dismiss();

                        final PickLanguageAdapter adapter = new PickLanguageAdapter(LeadersActivity.this, currLang, summary.languages);
                        AlertDialog.Builder builder = new AlertDialog.Builder(LeadersActivity.this);
                        builder.setTitle(R.string.filterByLanguage)
                                .setAdapter(adapter, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        gatherAndUpdate(adapter.getItem(which).name);

                                        ThisApplication.sendAnalytics(LeadersActivity.this, new HitBuilders.EventBuilder()
                                                .setCategory(ThisApplication.CATEGORY_USER_INPUT)
                                                .setAction(ThisApplication.ACTION_FILTER_LEADERS)
                                                .build());
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
                    }

                    @Override
                    public void onWakaTimeException(WakaTimeException ex) {
                        CommonUtils.UIToast(LeadersActivity.this, Utils.ToastMessages.INVALID_TOKEN, ex);
                        startActivity(new Intent(LeadersActivity.this, GrantActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
                    }

                    @Override
                    public void onException(Exception ex) {
                        CommonUtils.UIToast(LeadersActivity.this, Utils.ToastMessages.FAILED_LOADING, ex);
                        pd.dismiss();
                    }
                });
                break;
        }
        return true;
    }
}
