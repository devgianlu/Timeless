package com.gianlu.timeless.Listing;

import android.support.annotation.StringRes;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.gianlu.timeless.R;

import java.util.Locale;

class PercentageViewHolder extends RecyclerView.ViewHolder {
    private final TextView title;
    private final ImageView trending;
    private final TextView percentage;

    PercentageViewHolder(LayoutInflater inflater, ViewGroup parent) {
        super(inflater.inflate(R.layout.card_percentage, parent, false));

        title = itemView.findViewById(R.id.percentageCard_title);
        trending = itemView.findViewById(R.id.percentageCard_trending);
        percentage = itemView.findViewById(R.id.percentageCard_percentage);
    }

    void bind(@StringRes int title, float roundedPercent) {
        this.title.setText(title);

        if (roundedPercent == 0) {
            percentage.setText(String.format(Locale.getDefault(), "%.2f%%", roundedPercent));
            trending.setImageResource(R.drawable.baseline_trending_flat_24);
        } else if (roundedPercent > 0) {
            percentage.setText(String.format(Locale.getDefault(), "+%.2f%%", roundedPercent));
            trending.setImageResource(R.drawable.baseline_trending_up_24);
        } else {
            percentage.setText(String.format(Locale.getDefault(), "%.2f%%", roundedPercent));
            trending.setImageResource(R.drawable.baseline_trending_down_24);
        }
    }
}
