package com.gianlu.timeless.Activities;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ScrollView;
import android.widget.TextView;

import com.gianlu.commonutils.CommonUtils;
import com.gianlu.commonutils.InfiniteRecyclerView;
import com.gianlu.commonutils.SuperTextView;
import com.gianlu.commonutils.Toaster;
import com.gianlu.timeless.Activities.Leaders.LeadersAdapter;
import com.gianlu.timeless.Activities.Leaders.PickLanguageAdapter;
import com.gianlu.timeless.GrantActivity;
import com.gianlu.timeless.Models.Leader;
import com.gianlu.timeless.Models.Summary;
import com.gianlu.timeless.NetIO.WakaTime;
import com.gianlu.timeless.NetIO.WakaTimeException;
import com.gianlu.timeless.R;
import com.gianlu.timeless.SquarePieChart;
import com.gianlu.timeless.ThisApplication;
import com.gianlu.timeless.Utils;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IValueFormatter;
import com.github.mikephil.charting.utils.ViewPortHandler;
import com.google.android.gms.analytics.HitBuilders;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class LeadersActivity extends AppCompatActivity implements WakaTime.ILeaders, LeadersAdapter.IAdapter {
    private LeadersAdapter adapter;
    private TextView currFilter;
    private String currLang;
    private InfiniteRecyclerView list;
    private Leader me;
    private ProgressDialog pd;
    private SwipeRefreshLayout layout;
    private WakaTime wakaTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leaders);
        setTitle(R.string.leaderboards);

        Toolbar toolbar = findViewById(R.id.leaders_toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) actionBar.setDisplayHomeAsUpEnabled(true);

        layout = findViewById(R.id.leaders_swipeRefresh);
        layout.setColorSchemeResources(Utils.getColors());
        currFilter = findViewById(R.id.leaders_rankingText);
        list = findViewById(R.id.leaders_list);
        list.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));

        wakaTime = WakaTime.getInstance();

        layout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                wakaTime.getLeaders(LeadersActivity.this);
            }
        });

        pd = CommonUtils.fastIndeterminateProgressDialog(this, R.string.loadingData);
        CommonUtils.showDialog(this, pd);

        wakaTime.getLeaders(this);
    }

    @Override
    public void onLeaders(final List<Leader> leaders, Leader me, int maxPages) {
        LeadersActivity.this.me = me;
        adapter = new LeadersAdapter(LeadersActivity.this, leaders, maxPages, me, this);
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
            Toaster.show(LeadersActivity.this, Utils.ToastMessages.FAILED_REFRESHING, ex, new Runnable() {
                @Override
                public void run() {
                    layout.setRefreshing(false);
                }
            });
        } else {
            Toaster.show(LeadersActivity.this, Utils.ToastMessages.FAILED_LOADING, ex, new Runnable() {
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
        Toaster.show(LeadersActivity.this, Utils.ToastMessages.INVALID_TOKEN, ex, new Runnable() {
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
        wakaTime.getLeaders(language, new WakaTime.ILeaders() {
            @Override
            public void onLeaders(List<Leader> leaders, Leader me, int maxPages) {
                LeadersActivity.this.me = me;
                LeadersActivity.this.adapter = new LeadersAdapter(LeadersActivity.this, leaders, maxPages, me, LeadersActivity.this);
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
                Toaster.show(LeadersActivity.this, Utils.ToastMessages.FAILED_LOADING, ex);
                pd.dismiss();
            }

            @Override
            public void onWakaTimeException(WakaTimeException ex) {
                Toaster.show(LeadersActivity.this, Utils.ToastMessages.INVALID_TOKEN, ex);
                startActivity(new Intent(LeadersActivity.this, GrantActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
            }
        });
    }

    @SuppressLint("InflateParams")
    private void displayRankDialog(Leader leader) {
        ScrollView layout = (ScrollView) getLayoutInflater().inflate(R.layout.leader_dialog, null, false);

        SuperTextView rank = layout.findViewById(R.id.leaderDialog_rank);
        rank.setHtml(R.string.rank, leader.rank);

        SuperTextView weekTotal = layout.findViewById(R.id.leaderDialog_weekTotal);
        weekTotal.setHtml(R.string.last7DaysTimeSpent, Utils.timeFormatterHours(leader.total_seconds, true));

        SuperTextView dailyAverage = layout.findViewById(R.id.leaderDialog_dailyAverage);
        dailyAverage.setHtml(R.string.dailyTimeSpent, Utils.timeFormatterHours(leader.daily_average, true));

        SquarePieChart chart = layout.findViewById(R.id.leaderDialog_chart);
        chart.setDescription(null);
        chart.setDrawEntryLabels(false);
        chart.setRotationEnabled(false);

        final Legend legend = chart.getLegend();
        legend.setWordWrapEnabled(true);

        final List<PieEntry> entries = new ArrayList<>();
        for (Map.Entry<String, Long> entry : leader.languages.entrySet())
            entries.add(new PieEntry(entry.getValue(), entry.getKey()));

        PieDataSet set = new PieDataSet(entries, null);
        set.setValueTextSize(15);
        set.setSliceSpace(0);
        set.setValueTextColor(ContextCompat.getColor(this, android.R.color.white));
        set.setValueFormatter(new IValueFormatter() {
            @Override
            public String getFormattedValue(float value, Entry entry, int dataSetIndex, ViewPortHandler viewPortHandler) {
                if (value < 10) return "";
                else return String.format(Locale.getDefault(), "%.2f", value) + "%";
            }
        });
        set.setColors(Utils.getColors(), this);
        chart.setData(new PieData(set));
        chart.setUsePercentValues(true);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(leader.user.getDisplayName())
                .setView(layout)
                .setPositiveButton(android.R.string.ok, null);

        CommonUtils.showDialog(this, builder);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
            case R.id.leaders_me:
                if (me != null && me.rank != -1) displayRankDialog(me);
                else Toaster.show(LeadersActivity.this, Utils.ToastMessages.USER_NOT_FOUND);

                ThisApplication.sendAnalytics(this, new HitBuilders.EventBuilder()
                        .setCategory(ThisApplication.CATEGORY_USER_INPUT)
                        .setAction(ThisApplication.ACTION_SHOW_ME_LEADER)
                        .build());
                break;
            case R.id.leaders_filter:
                final ProgressDialog pd = CommonUtils.fastIndeterminateProgressDialog(this, R.string.loadingData);
                CommonUtils.showDialog(this, pd);

                wakaTime.getRangeSummary(WakaTime.Range.LAST_7_DAYS.getStartAndEnd(), new WakaTime.ISummary() {
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
                        Toaster.show(LeadersActivity.this, Utils.ToastMessages.INVALID_TOKEN, ex);
                        startActivity(new Intent(LeadersActivity.this, GrantActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
                    }

                    @Override
                    public void onException(Exception ex) {
                        Toaster.show(LeadersActivity.this, Utils.ToastMessages.FAILED_LOADING, ex);
                        pd.dismiss();
                    }
                });
                break;
        }
        return true;
    }

    @Override
    public void onLeaderSelected(Leader leader) {
        displayRankDialog(leader);
    }
}
