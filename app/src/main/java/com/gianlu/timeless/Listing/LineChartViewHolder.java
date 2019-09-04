package com.gianlu.timeless.Listing;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.gianlu.commonutils.CommonUtils;
import com.gianlu.commonutils.MaterialColors;
import com.gianlu.timeless.Charting.OnSaveChart;
import com.gianlu.timeless.Models.LoggedEntity;
import com.gianlu.timeless.Models.SingleSummary;
import com.gianlu.timeless.Models.Summaries;
import com.gianlu.timeless.R;
import com.gianlu.timeless.Utils;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

class LineChartViewHolder extends RecyclerView.ViewHolder {
    private final TextView title;
    private final LineChart chart;
    private final ImageButton save;

    LineChartViewHolder(LayoutInflater inflater, ViewGroup parent) {
        super(inflater.inflate(R.layout.card_chart_line, parent, false));

        title = itemView.findViewById(R.id.lineChartCard_title);
        chart = itemView.findViewById(R.id.lineChartCard_chart);
        save = itemView.findViewById(R.id.lineChartCard_save);
    }

    void bind(@NonNull Context context, @StringRes int title, @NonNull Summaries summaries, OnSaveChart listener) {
        this.title.setText(title);

        chart.setNoDataText(context.getString(R.string.noData));
        chart.setDescription(null);
        chart.setTouchEnabled(false);

        int textColor = CommonUtils.resolveAttrAsColor(context, android.R.attr.textColorPrimary);
        chart.getLegend().setTextColor(textColor);

        XAxis xAxis = chart.getXAxis();
        xAxis.setTextColor(textColor);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setValueFormatter(new ValueFormatter() {
            private final SimpleDateFormat formatter = new SimpleDateFormat("EEE", Locale.getDefault());

            @Override
            public String getFormattedValue(float value) {
                return formatter.format(value);
            }
        });

        chart.getAxisRight().setEnabled(false);
        YAxis leftAxis = chart.getAxisLeft();
        leftAxis.setEnabled(true);
        leftAxis.setTextColor(textColor);
        leftAxis.setAxisMinimum(0f);
        leftAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return Utils.timeFormatterHours((long) value, false);
            }
        });

        MaterialColors colors = MaterialColors.getShuffledInstance();
        Map<String, ILineDataSet> branchToSets = new HashMap<>(summaries.availableBranches.size());
        int maxEntries = 0;
        int i = 0;
        for (SingleSummary summary : summaries) {
            for (LoggedEntity branch : summary.branches) {
                LineDataSet set = (LineDataSet) branchToSets.get(branch.name);
                if (set == null) {
                    int color = ContextCompat.getColor(context, colors.getColor(i));
                    i++;

                    set = new LineDataSet(new ArrayList<>(), branch.name);
                    set.setDrawValues(false);
                    set.setDrawCircles(true);
                    set.setCircleColor(color);
                    set.setDrawCircleHole(false);
                    set.setFillColor(color);
                    set.setFillAlpha(100);
                    set.setMode(LineDataSet.Mode.CUBIC_BEZIER);
                    set.setColor(color);
                    set.setDrawFilled(true);

                    branchToSets.put(branch.name, set);
                }

                set.addEntry(new Entry(summary.date, branch.total_seconds));
                if (set.getEntryCount() > maxEntries)
                    maxEntries = set.getEntryCount();
            }
        }

        if (maxEntries > 10) maxEntries = 10;
        xAxis.setLabelCount(maxEntries, true);

        if (branchToSets.isEmpty()) chart.clear();
        else chart.setData(new LineData(new ArrayList<>(branchToSets.values())));

        save.setOnClickListener(v -> listener.saveImage(chart, title));
    }
}
