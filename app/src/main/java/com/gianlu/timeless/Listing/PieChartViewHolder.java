package com.gianlu.timeless.Listing;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.gianlu.timeless.R;
import com.github.mikephil.charting.charts.PieChart;

public class PieChartViewHolder extends RecyclerView.ViewHolder {
    public final TextView title;
    public final PieChart chart;
    public final LinearLayout container;
    public final ImageButton expand;
    public final LinearLayout details;

    public PieChartViewHolder(LayoutInflater inflater, ViewGroup parent) {
        super(inflater.inflate(R.layout.pie_chart_card, parent, false));

        title = (TextView) itemView.findViewById(R.id.pieChartCard_title);
        chart = (PieChart) itemView.findViewById(R.id.pieChartCard_chart);
        container = (LinearLayout) itemView.findViewById(R.id.pieChartCard_container);
        expand = (ImageButton) itemView.findViewById(R.id.pieChartCard_expand);
        details = (LinearLayout) itemView.findViewById(R.id.pieChartCard_details);
    }
}
