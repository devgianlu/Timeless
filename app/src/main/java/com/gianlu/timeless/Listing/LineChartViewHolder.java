package com.gianlu.timeless.Listing;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;

import com.gianlu.timeless.Objects.Summary;
import com.gianlu.timeless.R;
import com.gianlu.timeless.Utils;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;

import java.util.ArrayList;
import java.util.List;

class LineChartViewHolder extends RecyclerView.ViewHolder {
    private final TextView title;
    private final LineChart chart;

    LineChartViewHolder(LayoutInflater inflater, ViewGroup parent) {
        super(inflater.inflate(R.layout.line_chart_card, parent, false));

        title = (TextView) itemView.findViewById(R.id.lineChartCard_title);
        chart = (LineChart) itemView.findViewById(R.id.lineChartCard_chart);
    }

    void bind(Context context, String title, final List<Summary> summaries) {
        this.title.setText(title);

        chart.setDescription(null);
        chart.setTouchEnabled(false);

        XAxis xAxis = chart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setValueFormatter(new IAxisValueFormatter() {
            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                return String.valueOf((int) (value - summaries.size() + 1));
            }
        });

        chart.getAxisRight().setEnabled(false);
        YAxis leftAxis = chart.getAxisLeft();
        leftAxis.setEnabled(true);
        leftAxis.setAxisMinimum(0f);
        leftAxis.setValueFormatter(new IAxisValueFormatter() {
            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                return Utils.timeFormatterHours((long) value, false);
            }
        });

        chart.getLegend().setEnabled(false);

        List<Entry> entries = new ArrayList<>();
        for (int i = 0; i < summaries.size(); i++) {
            Summary summary = summaries.get(i);
            entries.add(new Entry(i, summary.total_seconds));
        }

        LineDataSet set = new LineDataSet(entries, null);
        set.setDrawValues(false);
        set.setDrawCircles(entries.size() == 1);
        set.setDrawCircleHole(false);
        set.setFillColor(ContextCompat.getColor(context, R.color.colorAccent));
        set.setFillAlpha(100);
        set.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        set.setColor(ContextCompat.getColor(context, R.color.colorAccent));
        set.setDrawFilled(true);
        chart.setData(new LineData(set));
    }
}
