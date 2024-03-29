package com.gianlu.timeless.activities.privateleaderboards;

import android.content.Context;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.gianlu.commonutils.CommonUtils;
import com.gianlu.commonutils.misc.InfiniteRecyclerView;
import com.gianlu.timeless.R;
import com.gianlu.timeless.api.WakaTime;
import com.gianlu.timeless.api.models.Leaderboards;

import java.util.Date;

public class LeaderboardsAdapter extends InfiniteRecyclerView.InfiniteAdapter<LeaderboardsAdapter.ViewHolder, Leaderboards.Item> {
    private final WakaTime wakaTime;
    private final Listener listener;

    public LeaderboardsAdapter(@NonNull Context context, WakaTime wakaTime, Leaderboards leaderboards, Listener listener) {
        super(context, new Config<Leaderboards.Item>().items(leaderboards).noSeparators().maxPages(leaderboards.maxPages));
        this.wakaTime = wakaTime;
        this.listener = listener;
    }

    @Override
    protected void userBindViewHolder(@NonNull ViewHolder holder, @NonNull ItemEnclosure<Leaderboards.Item> item, int position) {
        final Leaderboards.Item l = item.getItem();
        holder.name.setText(l.name);
        CommonUtils.setTextPlural(holder.members, R.plurals.members, l.members, l.members);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onLeaderboardSelected(l);
        });
    }

    @NonNull
    @Override
    protected RecyclerView.ViewHolder createViewHolder(@NonNull ViewGroup parent) {
        return new ViewHolder(parent);
    }

    @Override
    protected void moreContent(int page, @NonNull ContentProvider<Leaderboards.Item> provider) {
        wakaTime.getPrivateLeaderboards(page, null, new WakaTime.OnResult<Leaderboards>() {
            @Override
            public void onResult(@NonNull Leaderboards result) {
                maxPages(result.maxPages);
                provider.onMoreContent(result);
            }

            @Override
            public void onException(@NonNull Exception ex) {
                provider.onFailed(ex);
            }
        });
    }

    @Nullable
    @Override
    protected Date getDateFromItem(Leaderboards.Item item) {
        return null;
    }

    public interface Listener {
        void onLeaderboardSelected(@NonNull Leaderboards.Item item);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        final TextView name;
        final TextView members;

        public ViewHolder(@NonNull ViewGroup parent) {
            super(inflater.inflate(R.layout.item_leaderboard, parent, false));

            name = itemView.findViewById(R.id.leaderboard_name);
            members = itemView.findViewById(R.id.leaderboard_members);
        }
    }
}
