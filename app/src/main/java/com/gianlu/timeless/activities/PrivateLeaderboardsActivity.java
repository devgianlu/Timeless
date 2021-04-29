package com.gianlu.timeless.activities;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;

import com.gianlu.commonutils.dialogs.ActivityWithDialog;
import com.gianlu.commonutils.misc.RecyclerMessageView;
import com.gianlu.timeless.R;
import com.gianlu.timeless.activities.privateleaderboards.LeaderboardsAdapter;
import com.gianlu.timeless.api.WakaTime;
import com.gianlu.timeless.api.WakaTimeException;
import com.gianlu.timeless.api.models.Leaderboards;

public class PrivateLeaderboardsActivity extends ActivityWithDialog implements WakaTime.OnResult<Leaderboards>, LeaderboardsAdapter.Listener {
    private WakaTime wakaTime;
    private RecyclerMessageView layout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leaderboards_private);
        setTitle(R.string.privateLeaderboards);

        layout = findViewById(R.id.privateLeaderboards_rmv);
        layout.linearLayoutManager(RecyclerView.VERTICAL, false);

        Toolbar toolbar = findViewById(R.id.privateLeaderboards_toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) actionBar.setDisplayHomeAsUpEnabled(true);

        try {
            wakaTime = WakaTime.get();
        } catch (WakaTime.MissingCredentialsException ex) {
            ex.resolve(this);
            return;
        }

        wakaTime.getPrivateLeaderboards(1, null, this);
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
