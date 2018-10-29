package com.gianlu.timeless.Listing;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.gianlu.commonutils.SuperTextView;
import com.gianlu.timeless.Models.Summary;
import com.gianlu.timeless.R;
import com.gianlu.timeless.Utils;

import androidx.recyclerview.widget.RecyclerView;

class SummaryViewHolder extends RecyclerView.ViewHolder {
    private final LinearLayout container;

    SummaryViewHolder(LayoutInflater inflater, ViewGroup parent) {
        super(inflater.inflate(R.layout.card_summary, parent, false));
        container = itemView.findViewById(R.id.summaryCard_container);
    }

    void bind(Context context, Summary summary) {
        container.removeAllViews();
        container.addView(new SuperTextView(context, R.string.totalTimeSpent, Utils.timeFormatterHours(summary.total_seconds, true)));

        if (summary.sumNumber > 1) {
            long average = summary.total_seconds / summary.sumNumber;
            container.addView(new SuperTextView(context, R.string.averageTimeSpent, Utils.timeFormatterHours(average, true)));
        }
    }
}
