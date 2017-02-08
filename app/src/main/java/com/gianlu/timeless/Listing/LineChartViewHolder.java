package com.gianlu.timeless.Listing;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;

import com.gianlu.timeless.R;
import com.github.mikephil.charting.charts.LineChart;

public class LineChartViewHolder extends RecyclerView.ViewHolder {
    public final TextView title;
    public final LineChart chart;

    public LineChartViewHolder(LayoutInflater inflater, ViewGroup parent) {
        super(inflater.inflate(R.layout.line_chart_card, parent, false));

        title = (TextView) itemView.findViewById(R.id.lineChartCard_title);
        chart = (LineChart) itemView.findViewById(R.id.lineChartCard_chart);
    }
}
