package com.gianlu.timeless.listing;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.core.content.ContextCompat;

import com.gianlu.commonutils.CommonUtils;
import com.gianlu.commonutils.typography.MaterialColors;
import com.gianlu.timeless.R;
import com.gianlu.timeless.SaveChartUtils;
import com.gianlu.timeless.Utils;
import com.gianlu.timeless.api.models.LoggedEntity;
import com.gianlu.timeless.api.models.Project;
import com.gianlu.timeless.api.models.Summaries;
import com.gianlu.timeless.api.models.Summary;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.LegendEntry;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.ValueFormatter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

class BarChartViewHolder extends HelperViewHolder {
    private final TextView title;
    private final BarChart chart;
    private final ImageButton save;

    BarChartViewHolder(Listener listener, LayoutInflater inflater, ViewGroup parent) {
        super(listener, inflater, parent, R.layout.item_chart_bar);

        title = itemView.findViewById(R.id.barChartCard_title);
        chart = itemView.findViewById(R.id.barChartCard_chart);
        save = itemView.findViewById(R.id.barChartCard_save);
    }

    void bind(@StringRes int title, @NonNull Summaries summaries, @Nullable Project project) {
        this.title.setText(title);

        chart.setDescription(null);
        chart.setTouchEnabled(false);
        chart.setNoDataText(chart.getContext().getString(R.string.noData));

        int textColor = CommonUtils.resolveAttrAsColor(getContext(), android.R.attr.textColorPrimary);
        chart.getLegend().setTextColor(textColor);

        XAxis xAxis = chart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setTextColor(textColor);
        xAxis.setValueFormatter(new ValueFormatter() {
            private final SimpleDateFormat formatter = new SimpleDateFormat("EEE", Locale.getDefault());

            @Override
            public String getFormattedValue(float value) {
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
        leftAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return Utils.timeFormatterHours((long) value, false);
            }
        });

        Legend legend = chart.getLegend();
        legend.setWordWrapEnabled(true);

        List<BarEntry> entries = new ArrayList<>();
        Map<String, Integer> colorsMap = new HashMap<>();
        List<Integer> colors = new ArrayList<>();
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
                    int color = ContextCompat.getColor(getContext(), materialColors.getColor(colorCount));
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

        if (entries.isEmpty()) {
            save.setVisibility(View.INVISIBLE);
            chart.clear();
        } else {
            save.setVisibility(View.VISIBLE);
            save.setOnClickListener(v -> SaveChartUtils.share(chart, title, project));
        }
    }
}
