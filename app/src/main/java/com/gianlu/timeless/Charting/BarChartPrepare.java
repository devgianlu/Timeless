package com.gianlu.timeless.Charting;

import android.content.Context;

import com.gianlu.commonutils.CommonUtils;
import com.gianlu.commonutils.MaterialColors;
import com.gianlu.timeless.Models.LoggedEntity;
import com.gianlu.timeless.Models.Summaries;
import com.gianlu.timeless.Models.Summary;
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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

public class BarChartPrepare extends AbsChartPrepare<BarChart> {

    public BarChartPrepare(@NonNull BarChart chart) {
        super(chart);
    }

    @Override
    public void setup(@NonNull Context context, @NonNull Object data) {
        Summaries summaries = (Summaries) data;

        chart.setDescription(null);
        chart.setTouchEnabled(false);
        chart.setNoDataText(context.getString(R.string.noData));

        int textColor = CommonUtils.resolveAttrAsColor(context, android.R.attr.textColorPrimary);
        chart.getLegend().setTextColor(textColor);

        XAxis xAxis = chart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setTextColor(textColor);
        xAxis.setValueFormatter(new IAxisValueFormatter() {
            private final SimpleDateFormat formatter = new SimpleDateFormat("EEE", Locale.getDefault());

            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                Calendar calendar = Calendar.getInstance();
                calendar.add(Calendar.DATE, (int) value - summaries.size() + 1);
                return formatter.format(calendar.getTime());
            }
        });

        chart.getAxisRight().setEnabled(false);
        YAxis leftAxis = chart.getAxisLeft();
        leftAxis.setTextColor(textColor);
        leftAxis.setEnabled(true);
        leftAxis.setAxisMinimum(0f);
        leftAxis.setValueFormatter((value, axis) -> Utils.timeFormatterHours((long) value, false));

        final Legend legend = chart.getLegend();
        legend.setWordWrapEnabled(true);

        final List<BarEntry> entries = new ArrayList<>();
        final Map<String, Integer> colorsMap = new HashMap<>();
        final List<Integer> colors = new ArrayList<>();
        List<LegendEntry> legendEntries = new ArrayList<>();
        int colorCount = 0;
        MaterialColors materialColors = MaterialColors.getShuffledInstance();
        for (int i = 0; i < summaries.size(); i++) {
            Summary summary = summaries.get(i);
            float[] array = new float[summary.projects.size()];

            for (int j = 0; j < summary.projects.size(); j++) {
                LoggedEntity entity = summary.projects.get(j);
                if (colorsMap.containsKey(entity.name)) {
                    colors.add(colorsMap.get(entity.name));
                } else {
                    int color = ContextCompat.getColor(context, materialColors.getColor(colorCount));
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
        if (!colors.isEmpty()) set.setColors(colors);

        chart.setData(new BarData(set));
        chart.setFitBars(true);

        if (legendEntries.isEmpty()) chart.clear();
    }
}
