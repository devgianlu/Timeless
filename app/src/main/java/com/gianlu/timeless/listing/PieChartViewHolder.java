package com.gianlu.timeless.listing;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.core.content.ContextCompat;

import com.gianlu.commonutils.CommonUtils;
import com.gianlu.timeless.Models.LoggedEntities;
import com.gianlu.timeless.Models.LoggedEntity;
import com.gianlu.timeless.R;
import com.gianlu.timeless.Utils;
import com.gianlu.timeless.charts.OnSaveChart;
import com.gianlu.timeless.charts.PieChartColorHelper;
import com.gianlu.timeless.colors.ProjectsColorMapper;
import com.gianlu.timeless.dialogs.LoggedEntityDialog;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

class PieChartViewHolder extends HelperViewHolder {
    private final TextView title;
    private final ImageButton save;
    private final PieChart chart;

    PieChartViewHolder(Listener listener, LayoutInflater inflater, ViewGroup parent) {
        super(listener, inflater, parent, R.layout.item_chart_pie);

        title = itemView.findViewById(R.id.pieChartCard_title);
        save = itemView.findViewById(R.id.pieChartCard_save);
        chart = itemView.findViewById(R.id.pieChartCard_chart);
    }

    void bind(@StringRes int title, @NonNull LoggedEntities entities, OnSaveChart listener) {
        this.title.setText(title);

        chart.setDescription(null);
        chart.setHoleColor(Color.argb(0, 0, 0, 0));
        chart.setNoDataText(chart.getContext().getString(R.string.noData));
        chart.setDrawEntryLabels(false);
        chart.setRotationEnabled(false);
        chart.setUsePercentValues(true);

        Legend legend = chart.getLegend();
        legend.setWordWrapEnabled(true);
        legend.setTextColor(CommonUtils.resolveAttrAsColor(getContext(), android.R.attr.textColorPrimary));

        List<PieEntry> entries = new ArrayList<>();
        for (LoggedEntity entity : entities) {
            if (entity.total_seconds < 1000) continue;

            entries.add(new PieEntry(entity.total_seconds, entity.name));
        }

        PieDataSet set = new PieDataSet(entries, null);
        set.setValueTextSize(15);
        set.setSliceSpace(0);
        set.setValueTextColor(ContextCompat.getColor(getContext(), android.R.color.white));
        set.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                if (value < 10) return "";
                else return String.format(Locale.getDefault(), "%.2f", value) + "%";
            }
        });

        PieChartColorHelper helper = new PieChartColorHelper(chart, ProjectsColorMapper.get());
        helper.setData(new PieData(set));

        helper.setOnChartValueSelectedListener(new PieChartColorHelper.OnValueSelectedListener() {
            @Override
            public void onValueSelected(@NonNull Entry e, @NonNull Highlight h, @ColorInt int color) {
                showDialog(LoggedEntityDialog.get(((PieEntry) e).getLabel(), color, (int) ((PieEntry) e).getValue())
                        .setOnDismissListener((d) -> chart.highlightValue(null)), null);
            }

            @Override
            public void onNothingSelected() {
            }
        });

        Utils.addTimeToLegendEntries(chart, entities);

        save.setOnClickListener(v -> listener.saveImage(chart, title));
    }
}