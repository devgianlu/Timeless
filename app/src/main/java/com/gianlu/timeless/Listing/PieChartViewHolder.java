package com.gianlu.timeless.Listing;

import android.graphics.Color;
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
import com.gianlu.timeless.Models.LoggedEntities;
import com.gianlu.timeless.Models.LoggedEntity;
import com.gianlu.timeless.R;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

class PieChartViewHolder extends RecyclerView.ViewHolder {
    private final TextView title;
    private final ImageButton save;
    private final PieChart chart;

    PieChartViewHolder(LayoutInflater inflater, ViewGroup parent) {
        super(inflater.inflate(R.layout.item_chart_pie, parent, false));

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

        final Legend legend = chart.getLegend();
        legend.setWordWrapEnabled(true);
        legend.setTextColor(CommonUtils.resolveAttrAsColor(chart.getContext(), android.R.attr.textColorPrimary));

        final List<PieEntry> entries = new ArrayList<>();
        for (LoggedEntity entity : entities)
            entries.add(new PieEntry(entity.total_seconds, entity.name));

        PieDataSet set = new PieDataSet(entries, null);
        set.setValueTextSize(15);
        set.setSliceSpace(0);
        set.setValueTextColor(ContextCompat.getColor(chart.getContext(), android.R.color.white));
        set.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                if (value < 10) return "";
                else return String.format(Locale.getDefault(), "%.2f", value) + "%";
            }
        });
        set.setColors(MaterialColors.getShuffledInstance().getColorsRes(), chart.getContext());
        chart.setData(new PieData(set));
        chart.setUsePercentValues(true);

        save.setOnClickListener(v -> listener.saveImage(chart, title));

        chart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, Highlight h) {

            }

            @Override
            public void onNothingSelected() {

            }
        });
    }
}
