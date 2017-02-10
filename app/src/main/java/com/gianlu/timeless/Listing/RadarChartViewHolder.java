package com.gianlu.timeless.Listing;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;

import com.gianlu.timeless.Objects.Duration;
import com.gianlu.timeless.R;
import com.github.mikephil.charting.charts.RadarChart;
import com.github.mikephil.charting.data.RadarData;
import com.github.mikephil.charting.data.RadarDataSet;
import com.github.mikephil.charting.data.RadarEntry;

import java.util.ArrayList;
import java.util.List;

class RadarChartViewHolder extends RecyclerView.ViewHolder {
    public final TextView title;
    public final RadarChart chart;

    RadarChartViewHolder(LayoutInflater inflater, ViewGroup parent) {
        super(inflater.inflate(R.layout.radar_chart_card, parent, false));
        title = (TextView) itemView.findViewById(R.id.radarChartCard_title);
        chart = (RadarChart) itemView.findViewById(R.id.radarChartCard_chart);
    }

    // TODO: There's a lot of work to do here
    public void bind(String title, List<Duration> durations) {
        this.title.setText(title);

        chart.setDescription(null);
        chart.setTouchEnabled(false);

        List<RadarEntry> entries = new ArrayList<>();
        for (Duration duration : durations)
            entries.add(new RadarEntry(duration.duration));

        RadarDataSet set = new RadarDataSet(entries, null);
        RadarData data = new RadarData(set);
        chart.setData(data);
    }
}
