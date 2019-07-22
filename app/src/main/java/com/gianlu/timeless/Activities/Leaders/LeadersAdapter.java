package com.gianlu.timeless.Activities.Leaders;

import android.content.Context;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.gianlu.commonutils.CasualViews.InfiniteRecyclerView;
import com.gianlu.commonutils.FontsManager;
import com.gianlu.timeless.Models.Leader;
import com.gianlu.timeless.Models.Leaders;
import com.gianlu.timeless.Models.LeadersWithMe;
import com.gianlu.timeless.Models.User;
import com.gianlu.timeless.NetIO.WakaTime;
import com.gianlu.timeless.R;
import com.gianlu.timeless.Utils;

import java.util.Date;
import java.util.Objects;

public class LeadersAdapter extends InfiniteRecyclerView.InfiniteAdapter<LeadersAdapter.ViewHolder, Leader> {
    private final String id;
    private final String language;
    private final Listener listener;
    private final User me;
    private final WakaTime wakaTime;

    private LeadersAdapter(Context context, @NonNull WakaTime wakaTime, @NonNull Leaders leaders, @Nullable User me, @Nullable String id, @Nullable String language, Listener listener) {
        super(context, new Config<Leader>().items(leaders).maxPages(leaders.maxPages).noSeparators());
        this.id = id;
        this.language = language;
        this.listener = listener;
        this.wakaTime = wakaTime;
        this.me = me;
    }

    public LeadersAdapter(Context context, @NonNull WakaTime wakaTime, @NonNull Leaders leaders, @NonNull String id, @Nullable String language, Listener listener) {
        this(context, wakaTime, leaders, null, id, language, listener);
    }

    public LeadersAdapter(Context context, @NonNull WakaTime wakaTime, @NonNull LeadersWithMe leaders, @Nullable String language, Listener listener) {
        this(context, wakaTime, leaders, leaders.me != null ? leaders.me.user : null, null, language, listener);
    }

    @Nullable
    @Override
    protected Date getDateFromItem(Leader item) {
        return null;
    }

    @Override
    protected void userBindViewHolder(@NonNull ViewHolder holder, @NonNull ItemEnclosure<Leader> item, int position) {
        final ItemEnclosure<Leader> leader = items.get(position);

        FontsManager.set(FontsManager.ROBOTO_LIGHT, holder.rank);
        holder.rank.setText(String.valueOf(leader.getItem().rank));
        holder.name.setText(leader.getItem().user.getDisplayName());
        holder.total.setText(Utils.timeFormatterHours(leader.getItem().total_seconds, true));

        if (me != null && Objects.equals(leader.getItem().user.id, me.id)) {
            holder.setIsRecyclable(false);
            holder.itemView.setBackgroundResource(R.color.colorAccent_translucent);
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onLeaderSelected(leader.getItem());
        });
    }

    @NonNull
    @Override
    protected RecyclerView.ViewHolder createViewHolder(@NonNull ViewGroup parent) {
        return new ViewHolder(parent);
    }

    @Override
    protected void moreContent(int page, @NonNull ContentProvider<Leader> provider) {
        if (id == null) {
            wakaTime.getLeaders(language, page, null, new WakaTime.OnResult<LeadersWithMe>() {
                @Override
                public void onResult(@NonNull LeadersWithMe leaders) {
                    maxPages(leaders.maxPages);
                    provider.onMoreContent(leaders);
                }

                @Override
                public void onException(@NonNull Exception ex) {
                    provider.onFailed(ex);
                }
            });
        } else {
            wakaTime.getLeaders(id, language, page, null, new WakaTime.OnResult<Leaders>() {
                @Override
                public void onResult(@NonNull Leaders leaders) {
                    maxPages(leaders.maxPages);
                    provider.onMoreContent(leaders);
                }

                @Override
                public void onException(@NonNull Exception ex) {
                    provider.onFailed(ex);
                }
            });
        }
    }

    public interface Listener {
        void onLeaderSelected(@NonNull Leader leader);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        final TextView rank;
        final TextView name;
        final TextView total;

        public ViewHolder(ViewGroup parent) {
            super(inflater.inflate(R.layout.item_leader, parent, false));

            rank = itemView.findViewById(R.id.leader_rank);
            name = itemView.findViewById(R.id.leader_name);
            total = itemView.findViewById(R.id.leader_total);
        }
    }
}
