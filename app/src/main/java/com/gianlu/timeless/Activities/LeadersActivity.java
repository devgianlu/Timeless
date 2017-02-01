package com.gianlu.timeless.Activities;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;

import com.gianlu.commonutils.CommonUtils;
import com.gianlu.timeless.Activities.Leaders.LeadersAdapter;
import com.gianlu.timeless.CurrentUser;
import com.gianlu.timeless.NetIO.WakaTime;
import com.gianlu.timeless.Objects.Leader;
import com.gianlu.timeless.R;
import com.gianlu.timeless.Utils;

import java.util.List;

public class LeadersActivity extends AppCompatActivity {
    private LeadersAdapter adapter;
    private RecyclerView list;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leaders);
        setTitle(R.string.leaderboards);

        final SwipeRefreshLayout layout = (SwipeRefreshLayout) findViewById(R.id.leaders_swipeRefresh);
        layout.setColorSchemeResources(Utils.getColors());
        list = (RecyclerView) findViewById(R.id.leaders_list);
        list.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));

        layout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                WakaTime.getInstance().getLeaders(new WakaTime.ILeaders() {
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

        WakaTime.getInstance().getLeaders(new WakaTime.ILeaders() {
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
        }
        return true;
    }
}
