package com.gianlu.timeless.Activities;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.gianlu.commonutils.CommonUtils;
import com.gianlu.timeless.Activities.Leaders.LeadersAdapter;
import com.gianlu.timeless.Activities.Leaders.PickLanguageAdapter;
import com.gianlu.timeless.CurrentUser;
import com.gianlu.timeless.NetIO.WakaTime;
import com.gianlu.timeless.Objects.Leader;
import com.gianlu.timeless.Objects.Summary;
import com.gianlu.timeless.R;
import com.gianlu.timeless.Utils;

import java.util.List;

public class LeadersActivity extends AppCompatActivity {
    private LeadersAdapter adapter;
    private TextView currFilter;
    private String currLang;
    private RecyclerView list;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leaders);
        setTitle(R.string.leaderboards);

        Toolbar toolbar = (Toolbar) findViewById(R.id.leaders_toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null)
            actionBar.setDisplayHomeAsUpEnabled(true);

        final SwipeRefreshLayout layout = (SwipeRefreshLayout) findViewById(R.id.leaders_swipeRefresh);
        layout.setColorSchemeResources(Utils.getColors());
        currFilter = (TextView) findViewById(R.id.leaders_rankingText);
        list = (RecyclerView) findViewById(R.id.leaders_list);
        list.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));

        layout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                WakaTime.getInstance().getLeaders(LeadersActivity.this, new WakaTime.ILeaders() {
                    @Override
                    public void onLeaders(final List<Leader> leaders) {
                        adapter = new LeadersAdapter(LeadersActivity.this, CurrentUser.get(), leaders);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                list.setAdapter(adapter);
                                layout.setRefreshing(false);
                            }
                        });
                    }

                    @Override
                    public void onException(Exception ex) {
                        ex.printStackTrace();
                    }
                });
            }
        });

        final ProgressDialog pd = CommonUtils.fastIndeterminateProgressDialog(this, R.string.loadingData);
        CommonUtils.showDialog(this, pd);

        WakaTime.getInstance().getLeaders(this, new WakaTime.ILeaders() {
            @Override
            public void onLeaders(final List<Leader> leaders) {
                adapter = new LeadersAdapter(LeadersActivity.this, CurrentUser.get(), leaders);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        list.setAdapter(adapter);
                        pd.dismiss();
                    }
                });
            }

            @Override
            public void onException(Exception ex) {
                ex.printStackTrace();
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
            public void onLeaders(List<Leader> leaders) {
                LeadersActivity.this.adapter = new LeadersAdapter(LeadersActivity.this, CurrentUser.get(), leaders);
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
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
            case R.id.leaders_me:
                final int pos = adapter.find(CurrentUser.get().id);

                if (pos >= 0) {
                    list.scrollToPosition(pos);
                } else {
                    CommonUtils.UIToast(LeadersActivity.this, Utils.ToastMessages.USER_NOT_FOUND, CurrentUser.get().id);
                }
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
