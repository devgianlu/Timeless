package com.gianlu.timeless.Listing;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;

import com.gianlu.timeless.Objects.Duration;
import com.gianlu.timeless.R;
import com.gianlu.timeless.Utils;
import com.github.mikephil.charting.charts.BubbleChart;
import com.github.mikephil.charting.data.BubbleData;
import com.github.mikephil.charting.data.BubbleDataSet;
import com.github.mikephil.charting.data.BubbleEntry;

import java.util.ArrayList;
import java.util.List;

class BubbleChartViewHolder extends RecyclerView.ViewHolder {
    public final TextView title;
    public final BubbleChart chart;

    BubbleChartViewHolder(LayoutInflater inflater, ViewGroup parent) {
        super(inflater.inflate(R.layout.bubble_chart_card, parent, false));

        title = (TextView) itemView.findViewById(R.id.bubbleChartCard_title);
        chart = (BubbleChart) itemView.findViewById(R.id.bubbleChartCard_chart);
    }

    // TODO: IDK
    public void bind(Context context, String title, List<Duration> durations) {
        this.title.setText(title);

        chart.setDescription(null);
        chart.setTouchEnabled(false);

        List<BubbleEntry> entries = new ArrayList<>();
        for (Duration duration : durations)
            entries.add(new BubbleEntry(duration.time, 0, duration.time));

        BubbleDataSet set = new BubbleDataSet(entries, null);
        set.setColors(Utils.getColors(), context);
        BubbleData data = new BubbleData(set);
        chart.setData(data);
    }
}
