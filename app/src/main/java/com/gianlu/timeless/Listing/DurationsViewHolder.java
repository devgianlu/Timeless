package com.gianlu.timeless.Listing;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;

import com.gianlu.timeless.DurationsView;
import com.gianlu.timeless.Objects.Duration;
import com.gianlu.timeless.R;

import java.util.List;

class DurationsViewHolder extends RecyclerView.ViewHolder {
    public final DurationsView durationsView;
    public final TextView title;

    DurationsViewHolder(LayoutInflater inflater, ViewGroup parent) {
        super(inflater.inflate(R.layout.durations_card, parent, false));

        title = (TextView) itemView.findViewById(R.id.durationsCard_title);
        durationsView = (DurationsView) itemView.findViewById(R.id.durationsCard_view);
    }

    // TODO: Save as image
    void bind(final String title, List<Duration> durations) {
        this.title.setText(title);

        durationsView.setDurations(durations);
    }
}
