package com.gianlu.timeless.Activities;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;

import com.gianlu.commonutils.RecyclerViewLayout;
import com.gianlu.timeless.Activities.PrivateLeaderboards.LeaderboardsAdapter;
import com.gianlu.timeless.Models.Leaderboards;
import com.gianlu.timeless.NetIO.WakaTime;
import com.gianlu.timeless.NetIO.WakaTimeException;
import com.gianlu.timeless.R;

public class PrivateLeaderboardsActivity extends AppCompatActivity implements WakaTime.OnResult<Leaderboards>, LeaderboardsAdapter.Listener {
    private WakaTime wakaTime;
    private RecyclerViewLayout layout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        layout = new RecyclerViewLayout(this);
        setContentView(layout);
        setTitle(R.string.privateLeaderboards);

        layout.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));

        try {
            wakaTime = WakaTime.get();
        } catch (WakaTime.ShouldGetAccessToken ex) {
            ex.resolve(this);
            return;
        }

        wakaTime.getPrivateLeaderboards(1, this);
    }

    @Override
    public void onResult(@NonNull Leaderboards result) {
        if (result.isEmpty()) layout.showInfo(R.string.noPrivateLeaderboards);
        else layout.loadListData(new LeaderboardsAdapter(this, wakaTime, result, this));
    }

    @Override
    public void onException(@NonNull Exception ex) {
        if (ex instanceof WakaTimeException) layout.showError(ex.getMessage());
        else layout.showError(R.string.failedLoading_reason, ex.getMessage());
    }

    @Override
    public void onLeaderboardSelected(@NonNull Leaderboards.Item item) {
        LeadersActivity.startActivity(this, item.id, item.name);
    }
}
