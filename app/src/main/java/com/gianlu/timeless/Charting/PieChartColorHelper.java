package com.gianlu.timeless.Charting;

import android.content.Context;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.gianlu.timeless.colors.ColorsMapper;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.datasets.IPieDataSet;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;

public class PieChartColorHelper {
    private final PieChart chart;
    private final ColorsMapper colors;

    public PieChartColorHelper(@NonNull PieChart chart, @NonNull ColorsMapper colors) {
        this.chart = chart;
        this.colors = colors;
    }

    public void setData(@NonNull PieData data) {
        for (IPieDataSet set : data.getDataSets()) {
            if (set instanceof PieDataSet) prepareSet(chart.getContext(), (PieDataSet) set);
        }

        chart.setData(data);
    }

    private void prepareSet(@NonNull Context context, @NonNull PieDataSet set) {
        int[] setColors = new int[set.getEntryCount()];

        for (int i = 0; i < set.getEntryCount(); i++) {
            PieEntry e = set.getEntryForIndex(i);
            if (e.getData() != null) throw new IllegalArgumentException();

            int color = ContextCompat.getColor(context, colors.getColor(e.getLabel()));
            setColors[i] = color;
            e.setData(new EntryColorMeta(color));
        }

        set.setColors(setColors);
    }

    public void setOnChartValueSelectedListener(OnValueSelectedListener listener) {
        if (listener == null) {
            chart.setOnChartValueSelectedListener(null);
            return;
        }

        chart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, Highlight h) {
                EntryColorMeta meta = (EntryColorMeta) e.getData();
                listener.onValueSelected(e, h, meta.color);
            }

            @Override
            public void onNothingSelected() {
                listener.onNothingSelected();
            }
        });
    }

    public interface OnValueSelectedListener {
        void onValueSelected(@NonNull Entry e, @NonNull Highlight h, @ColorInt int color);

        void onNothingSelected();
    }

    private static class EntryColorMeta {
        final int color;

        EntryColorMeta(@ColorInt int color) {
            this.color = color;
        }
    }
}
