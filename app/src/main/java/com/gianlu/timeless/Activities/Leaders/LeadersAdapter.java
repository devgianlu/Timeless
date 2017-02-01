package com.gianlu.timeless.Activities.Leaders;

import android.content.Context;
import android.graphics.Typeface;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.gianlu.timeless.Objects.Leader;
import com.gianlu.timeless.R;
import com.gianlu.timeless.Utils;

import java.util.List;

public class LeadersAdapter extends RecyclerView.Adapter<LeadersAdapter.ViewHolder> {
    private final List<Leader> leaders;
    private final LayoutInflater inflater;
    private final Typeface roboto;

    public LeadersAdapter(Context context, List<Leader> leaders) {
        this.leaders = leaders;

        inflater = LayoutInflater.from(context);
        roboto = Typeface.createFromAsset(context.getAssets(), "fonts/Roboto-Light.ttf");
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(inflater.inflate(R.layout.leader_item, parent, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Leader leader = leaders.get(position);

        holder.rank.setTypeface(roboto);
        holder.rank.setText(String.valueOf(leader.rank));
        holder.name.setText(leader.user.getDisplayName());
        holder.total.setText(Utils.timeFormatterHours(leader.total_seconds));
    }

    @Override
    public int getItemCount() {
        return leaders.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        final TextView rank;
        final TextView name;
        final TextView total;

        public ViewHolder(View itemView) {
            super(itemView);

            rank = (TextView) itemView.findViewById(R.id.leader_rank);
            name = (TextView) itemView.findViewById(R.id.leader_name);
            total = (TextView) itemView.findViewById(R.id.leader_total);
        }
    }
}
