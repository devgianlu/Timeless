package com.gianlu.timeless.Listing;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.gianlu.timeless.R;

public class SummaryViewHolder extends RecyclerView.ViewHolder {
    public final LinearLayout container;

    public SummaryViewHolder(LayoutInflater inflater, ViewGroup parent) {
        super(inflater.inflate(R.layout.summary_card, parent, false));
        container = (LinearLayout) itemView.findViewById(R.id.summaryCard_container);
    }
}
