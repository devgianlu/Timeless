package com.gianlu.timeless.Listing;

import android.support.v4.util.Pair;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.gianlu.timeless.R;

public class PercentageViewHolder extends RecyclerView.ViewHolder {
    private final TextView title;
    private final ImageView trending;
    private final TextView percentage;

    public PercentageViewHolder(LayoutInflater inflater, ViewGroup parent) {
        super(inflater.inflate(R.layout.percentage_card, parent, false));

        title = (TextView) itemView.findViewById(R.id.percentageCard_title);
        trending = (ImageView) itemView.findViewById(R.id.percentageCard_trending);
        percentage = (TextView) itemView.findViewById(R.id.percentageCard_percentage);
    }

    // FIXME: That's not the way to do this
    public void bind(String title, Pair<Float, Float> averages) {
        this.title.setText(title);

        System.out.println(averages);
        float percent = (averages.first / averages.second) * 100;

        if (averages.first > averages.second) {
            percentage.setText("-" + percent + "%");
        } else {
            percentage.setText("+" + percent + "%");
        }
    }
}
