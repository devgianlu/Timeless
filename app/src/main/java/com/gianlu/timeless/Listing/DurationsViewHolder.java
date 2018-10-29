package com.gianlu.timeless.Listing;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;

import com.gianlu.timeless.Charting.DurationsView;
import com.gianlu.timeless.Models.Durations;
import com.gianlu.timeless.R;

import androidx.annotation.StringRes;
import androidx.recyclerview.widget.RecyclerView;

class DurationsViewHolder extends RecyclerView.ViewHolder {
    public final DurationsView durationsView;
    public final TextView title;

    DurationsViewHolder(LayoutInflater inflater, ViewGroup parent) {
        super(inflater.inflate(R.layout.card_durations, parent, false));

        title = itemView.findViewById(R.id.durationsCard_title);
        durationsView = itemView.findViewById(R.id.durationsCard_view);
    }

    void bind(final @StringRes int title, Durations durations) {
        this.title.setText(title);

        durationsView.setDurations(durations);
    }
}
