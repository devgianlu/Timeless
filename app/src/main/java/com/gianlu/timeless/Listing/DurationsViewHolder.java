package com.gianlu.timeless.Listing;

import android.support.annotation.StringRes;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;

import com.gianlu.timeless.Charting.DurationsView;
import com.gianlu.timeless.Models.Duration;
import com.gianlu.timeless.R;

import java.util.List;

class DurationsViewHolder extends RecyclerView.ViewHolder {
    public final DurationsView durationsView;
    public final TextView title;

    DurationsViewHolder(LayoutInflater inflater, ViewGroup parent) {
        super(inflater.inflate(R.layout.durations_card, parent, false));

        title = itemView.findViewById(R.id.durationsCard_title);
        durationsView = itemView.findViewById(R.id.durationsCard_view);
    }

    void bind(final @StringRes int title, List<Duration> durations) {
        this.title.setText(title);

        durationsView.setDurations(durations);
    }
}
