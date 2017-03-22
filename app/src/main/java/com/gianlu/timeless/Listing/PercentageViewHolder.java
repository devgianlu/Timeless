package com.gianlu.timeless.Listing;

import android.annotation.SuppressLint;
import android.support.v4.util.Pair;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.gianlu.timeless.R;

import java.math.BigDecimal;
import java.util.Locale;

class PercentageViewHolder extends RecyclerView.ViewHolder {
    private final TextView title;
    private final ImageView trending;
    private final TextView percentage;

    PercentageViewHolder(LayoutInflater inflater, ViewGroup parent) {
        super(inflater.inflate(R.layout.percentage_card, parent, false));

        title = (TextView) itemView.findViewById(R.id.percentageCard_title);
        trending = (ImageView) itemView.findViewById(R.id.percentageCard_trending);
        percentage = (TextView) itemView.findViewById(R.id.percentageCard_percentage);
    }

    @SuppressLint("SetTextI18n")
    void bind(String title, Pair<Long, Float> values) {
        this.title.setText(title);

        BigDecimal bd = new BigDecimal(values.first);
        bd = bd.divide(new BigDecimal(values.second), 10, BigDecimal.ROUND_HALF_UP);
        bd = bd.multiply(new BigDecimal(100));
        bd = bd.subtract(new BigDecimal(100));
        float roundedPercent = bd.floatValue();

        if (roundedPercent == 0) {
            percentage.setText(String.format(Locale.getDefault(), "%.2f", roundedPercent) + "%");
            trending.setImageResource(R.drawable.ic_trending_flat_black_48dp);
        } else if (roundedPercent > 0) {
            percentage.setText("+" + String.format(Locale.getDefault(), "%.2f", roundedPercent) + "%");
            trending.setImageResource(R.drawable.ic_trending_up_black_48dp);
        } else {
            percentage.setText(String.format(Locale.getDefault(), "%.2f", roundedPercent) + "%");
            trending.setImageResource(R.drawable.ic_trending_down_black_48dp);
        }
    }
}
