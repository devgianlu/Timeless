package com.gianlu.timeless.Activities.PrivateLeaderboards;

import android.content.Context;
import android.view.ViewGroup;
import android.widget.TextView;

import com.gianlu.commonutils.InfiniteRecyclerView;
import com.gianlu.timeless.Models.Leaderboards;
import com.gianlu.timeless.NetIO.WakaTime;
import com.gianlu.timeless.R;

import java.util.Date;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

public class LeaderboardsAdapter extends InfiniteRecyclerView.InfiniteAdapter<LeaderboardsAdapter.ViewHolder, Leaderboards.Item> {
    private final Context context;
    private final WakaTime wakaTime;
    private final Listener listener;

    public LeaderboardsAdapter(Context context, WakaTime wakaTime, Leaderboards leaderboards, Listener listener) {
        super(new Config<Leaderboards.Item>(context).items(leaderboards).noSeparators().maxPages(leaderboards.maxPages));
        this.context = context;
        this.wakaTime = wakaTime;
        this.listener = listener;
    }

    @Override
    protected void userBindViewHolder(@NonNull ViewHolder holder, @NonNull ItemEnclosure<Leaderboards.Item> item, int position) {
        final Leaderboards.Item l = item.getItem();
        holder.name.setText(l.name);
        holder.members.setText(context.getResources().getQuantityString(R.plurals.members, l.members, l.members));

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
    protected void moreContent(int page, @NonNull final ContentProvider<Leaderboards.Item> provider) {
        wakaTime.getPrivateLeaderboards(page, new WakaTime.OnResult<Leaderboards>() {
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

        public ViewHolder(ViewGroup parent) {
            super(inflater.inflate(R.layout.item_leaderboard, parent, false));

            name = itemView.findViewById(R.id.leaderboard_name);
            members = itemView.findViewById(R.id.leaderboard_members);
        }
    }
}
