package com.gianlu.timeless.listing;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.gianlu.timeless.Models.Durations;
import com.gianlu.timeless.R;
import com.gianlu.timeless.charts.DurationsView;
import com.gianlu.timeless.colors.ProjectsColorMapper;

class DurationsViewHolder extends RecyclerView.ViewHolder {
    public final DurationsView view;

    DurationsViewHolder(LayoutInflater inflater, ViewGroup parent) {
        super(inflater.inflate(R.layout.item_durations, parent, false));
        view = (DurationsView) itemView;
    }

    void bind(@NonNull Durations durations) {
        view.setDurations(durations, ProjectsColorMapper.get());
    }
}