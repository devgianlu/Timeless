package com.gianlu.timeless.Listing;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.gianlu.timeless.Objects.LoggedEntity;
import com.gianlu.timeless.Objects.Summary;
import com.gianlu.timeless.R;
import com.gianlu.timeless.Utils;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.LegendEntry;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class BarChartViewHolder extends RecyclerView.ViewHolder {
    private final TextView title;
    private final BarChart chart;
    private final ImageButton save;

    BarChartViewHolder(LayoutInflater inflater, ViewGroup parent) {
        super(inflater.inflate(R.layout.bar_chart_card, parent, false));

        title = (TextView) itemView.findViewById(R.id.barChartCard_title);
        chart = (BarChart) itemView.findViewById(R.id.barChartCard_chart);
        save = (ImageButton) itemView.findViewById(R.id.barChartCard_save);
    }

    void bind(final Context context, final String title, final List<Summary> summaries, final CardsAdapter.ISaveChart handler) {
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

        final Legend legend = chart.getLegend();
        legend.setWordWrapEnabled(true);

        final List<BarEntry> entries = new ArrayList<>();
        final Map<String, Integer> colorsMap = new HashMap<>();
        final List<Integer> colors = new ArrayList<>();
        List<LegendEntry> legendEntries = new ArrayList<>();
        int colorCount = 0;
        for (int i = 0; i < summaries.size(); i++) {
            Summary summary = summaries.get(i);
            float[] array = new float[summary.projects.size()];

            for (int j = 0; j < summary.projects.size(); j++) {
                LoggedEntity entity = summary.projects.get(j);
                if (colorsMap.containsKey(entity.name)) {
                    colors.add(colorsMap.get(entity.name));
                } else {
                    int color = ContextCompat.getColor(context, Utils.getColor(colorCount));
                    colors.add(color);
                    colorsMap.put(entity.name, color);

                    LegendEntry legendEntry = new LegendEntry();
                    legendEntry.label = entity.name;
                    legendEntry.formColor = color;
                    legendEntries.add(legendEntry);

                    colorCount++;
                }

                array[j] = summary.projects.get(j).total_seconds;
            }

            entries.add(new BarEntry(i, array));
        }
        Collections.reverse(legendEntries);
        legend.setCustom(legendEntries);

        BarDataSet set = new BarDataSet(entries, null);
        set.setDrawValues(false);
        set.setColors(colors);

        chart.setData(new BarData(set));
        chart.setFitBars(true);

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    handler.onSaveRequested(chart, Utils.getFileName(title));
                } else {
                    handler.onWritePermissionRequested(new CardsAdapter.IPermissionRequest() {
                        @Override
                        public void onGranted() {
                            handler.onSaveRequested(chart, Utils.getFileName(title));
                        }
                    });
                }
            }
        });
    }
}
