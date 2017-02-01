package com.gianlu.timeless.Activities;

import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.gianlu.timeless.Activities.Leaders.LeadersAdapter;
import com.gianlu.timeless.NetIO.WakaTime;
import com.gianlu.timeless.Objects.Leader;
import com.gianlu.timeless.R;

import java.util.List;

public class LeadersActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leaders);

        SwipeRefreshLayout layout = (SwipeRefreshLayout) findViewById(R.id.leaders_swipeRefresh);
        final RecyclerView list = (RecyclerView) findViewById(R.id.leaders_list);
        list.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));

        WakaTime.getInstance().getLeaders(new WakaTime.ILeaders() {
            @Override
            public void onLeaders(final List<Leader> leaders) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        list.setAdapter(new LeadersAdapter(LeadersActivity.this, leaders));
                    }
                });
            }

            @Override
            public void onException(Exception ex) {
                ex.printStackTrace();
            }
        });
    }
}
