package com.gianlu.timeless.Activities.Leaders;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Typeface;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.gianlu.commonutils.InfiniteRecyclerView;
import com.gianlu.timeless.Models.Leader;
import com.gianlu.timeless.Models.User;
import com.gianlu.timeless.NetIO.WakaTime;
import com.gianlu.timeless.NetIO.WakaTimeException;
import com.gianlu.timeless.R;
import com.gianlu.timeless.Utils;

import java.util.Date;
import java.util.List;
import java.util.Objects;

public class LeadersAdapter extends InfiniteRecyclerView.InfiniteAdapter<LeadersAdapter.ViewHolder, Leader> {
    private final String language;
    private final IAdapter listener;
    private final User me;
    private final Typeface roboto;
    private final WakaTime wakaTime;

    public LeadersAdapter(Context context, List<Leader> items, int maxPages, @Nullable Leader me, @Nullable String language, IAdapter listener) {
        super(context, items, maxPages, -1, false);
        this.language = language;
        this.listener = listener;
        this.wakaTime = WakaTime.getInstance();
        this.roboto = Typeface.createFromAsset(context.getAssets(), "fonts/Roboto-Light.ttf");
        if (me != null) this.me = me.user;
        else this.me = null;
    }

    @Nullable
    @Override
    protected Date getDateFromItem(Leader item) {
        return null;
    }

    @Override
    protected void userBindViewHolder(ViewHolder holder, int position) {
        final ItemEnclosure<Leader> leader = items.get(position);

        holder.rank.setTypeface(roboto);
        holder.rank.setText(String.valueOf(leader.getItem().rank));
        holder.name.setText(leader.getItem().user.getDisplayName());
        holder.total.setText(Utils.timeFormatterHours(leader.getItem().total_seconds, true));

        if (me != null && Objects.equals(leader.getItem().user.id, me.id)) {
            holder.setIsRecyclable(false);
            holder.itemView.setBackgroundResource(R.color.colorAccent_shadow);
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @SuppressWarnings("deprecation")
            @SuppressLint("InflateParams")
            @Override
            public void onClick(View v) {
                if (listener != null) listener.onLeaderSelected(leader.getItem());
            }
        });
    }

    @Override
    protected RecyclerView.ViewHolder createViewHolder(ViewGroup parent) {
        return new ViewHolder(inflater.inflate(R.layout.item_leader, parent, false));
    }

    @Override
    protected void moreContent(final int page, final IContentProvider<Leader> provider) {
        wakaTime.getLeaders(language, page, new WakaTime.ILeaders() {
            @Override
            public void onLeaders(List<Leader> leaders, Leader me, int maxPages) {
                provider.onMoreContent(leaders);
            }

            @Override
            public void onException(Exception ex) {
                provider.onFailed(ex);
            }

            @Override
            public void onInvalidToken(WakaTimeException ex) {
                provider.onFailed(ex);
            }
        });
    }

    public interface IAdapter {
        void onLeaderSelected(Leader leader);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        final TextView rank;
        final TextView name;
        final TextView total;

        public ViewHolder(View itemView) {
            super(itemView);

            rank = itemView.findViewById(R.id.leader_rank);
            name = itemView.findViewById(R.id.leader_name);
            total = itemView.findViewById(R.id.leader_total);
        }
    }
}
