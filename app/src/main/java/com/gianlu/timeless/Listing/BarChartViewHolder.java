package com.gianlu.timeless.Listing;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;

import com.gianlu.timeless.R;
import com.github.mikephil.charting.charts.BarChart;

public class BarChartViewHolder extends RecyclerView.ViewHolder {
    public final TextView title;
    public final BarChart chart;

    public BarChartViewHolder(LayoutInflater inflater, ViewGroup parent) {
        super(inflater.inflate(R.layout.bar_chart_card, parent, false));

        title = (TextView) itemView.findViewById(R.id.barChartCard_title);
        chart = (BarChart) itemView.findViewById(R.id.barChartCard_chart);
    }
}
