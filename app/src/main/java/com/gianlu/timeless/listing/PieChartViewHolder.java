package com.gianlu.timeless.listing;

import android.graphics.Color;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.core.content.ContextCompat;

import com.gianlu.commonutils.CommonUtils;
import com.gianlu.timeless.R;
import com.gianlu.timeless.Utils;
import com.gianlu.timeless.activities.ProjectsActivity;
import com.gianlu.timeless.api.models.LoggedEntities;
import com.gianlu.timeless.api.models.LoggedEntity;
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
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class PieChartViewHolder extends HelperViewHolder {
    private final TextView title;
    private final ImageButton save;
    private final PieChart chart;

    PieChartViewHolder(Listener listener, LayoutInflater inflater, ViewGroup parent) {
        super(listener, inflater, parent, R.layout.item_chart_pie);

        title = itemView.findViewById(R.id.pieChartCard_title);
        save = itemView.findViewById(R.id.pieChartCard_save);
        chart = itemView.findViewById(R.id.pieChartCard_chart);
    }

    void bind(@StringRes int title, @NonNull LoggedEntities entities, @NonNull ChartContext chartContext, @NonNull Pair<Date, Date> interval, OnSaveChart listener) {
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
                showDialog(prepareDialog(((PieEntry) e).getLabel(), (int) ((PieEntry) e).getValue(), color, interval, chartContext), null);
            }

            @Override
            public void onNothingSelected() {
                // Unused
            }
        });

        Utils.addTimeToLegendEntries(chart, entities);
        if (entries.isEmpty()) chart.clear();

        save.setOnClickListener(v -> listener.saveImage(chart, title));
    }

    @NonNull
    private LoggedEntityDialog prepareDialog(@NonNull String label, int value, @ColorInt int color, @NonNull Pair<Date, Date> interval, @NonNull ChartContext chartContext) {
        LoggedEntityDialog dialog;
        if (chartContext == ChartContext.PROJECTS) {
            dialog = LoggedEntityDialog.get(label, color, value,
                    new LoggedEntityDialog.Action(R.string.goToProject,
                            ProjectsActivity.startIntent(getContext(), interval, label)));
        } else {
            dialog = LoggedEntityDialog.get(label, color, value, null);
        }

        dialog.setOnDismissListener((d) -> chart.highlightValue(null));

        return dialog;
    }

    public enum ChartContext {
        PROJECTS, IRRELEVANT
    }
}
