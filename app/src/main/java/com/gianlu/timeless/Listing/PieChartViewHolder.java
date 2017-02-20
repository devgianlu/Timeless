package com.gianlu.timeless.Listing;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.gianlu.commonutils.CommonUtils;
import com.gianlu.timeless.Objects.LoggedEntity;
import com.gianlu.timeless.R;
import com.gianlu.timeless.Utils;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.ViewPortHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

class PieChartViewHolder extends RecyclerView.ViewHolder {
    private final TextView title;
    private final ImageButton save;
    private final PieChart chart;
    private final LinearLayout container;
    private final ImageButton expand;
    private final LinearLayout details;

    PieChartViewHolder(LayoutInflater inflater, ViewGroup parent) {
        super(inflater.inflate(R.layout.pie_chart_card, parent, false));

        title = (TextView) itemView.findViewById(R.id.pieChartCard_title);
        save = (ImageButton) itemView.findViewById(R.id.pieChartCard_save);
        chart = (PieChart) itemView.findViewById(R.id.pieChartCard_chart);
        container = (LinearLayout) itemView.findViewById(R.id.pieChartCard_container);
        expand = (ImageButton) itemView.findViewById(R.id.pieChartCard_expand);
        details = (LinearLayout) itemView.findViewById(R.id.pieChartCard_details);
    }

    @SuppressWarnings("deprecation")
    void bind(final Context context, final String title, List<LoggedEntity> entities, final CardsAdapter.ISaveChart handler) {
        this.title.setText(title);

        chart.setDescription(null);
        chart.setNoDataText(context.getString(R.string.noData));
        chart.setDrawEntryLabels(false);
        chart.setRotationEnabled(false);

        final Legend legend = chart.getLegend();
        legend.setWordWrapEnabled(true);

        final List<PieEntry> entries = new ArrayList<>();
        for (LoggedEntity entity : entities)
            entries.add(new PieEntry(entity.total_seconds, entity.name));

        PieDataSet set = new PieDataSet(entries, null);
        set.setValueTextSize(15);
        set.setSliceSpace(0);
        set.setValueTextColor(ContextCompat.getColor(context, android.R.color.white));
        set.setValueFormatter(new IValueFormatter() {
            @Override
            public String getFormattedValue(float value, Entry entry, int dataSetIndex, ViewPortHandler viewPortHandler) {
                if (value < 10)
                    return "";
                else
                    return String.format(Locale.getDefault(), "%.2f", value) + "%";
            }
        });
        set.setColors(Utils.getColors(), context);
        chart.setData(new PieData(set));
        chart.setUsePercentValues(true);

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

        expand.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CommonUtils.animateCollapsingArrowBellows(expand, CommonUtils.isExpanded(container));

                if (CommonUtils.isExpanded(container))
                    CommonUtils.collapse(container);
                else
                    CommonUtils.expand(container);
            }
        });

        details.removeAllViews();
        long total_seconds = LoggedEntity.sumSeconds(entities);
        for (LoggedEntity entity : entities) {
            TextView text = CommonUtils.fastTextView(context,
                    Html.fromHtml(
                            context.getString(
                                    R.string.cardDetailsEntity,
                                    entity.name,
                                    Utils.timeFormatterHours(entity.total_seconds, true),
                                    String.format(Locale.getDefault(),
                                            "%.2f",
                                            ((float) entity.total_seconds) / ((float) total_seconds) * 100))));
            text.setTag(entity.name);
            details.addView(text);
        }

        chart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, Highlight h) {
                if (!CommonUtils.isExpanded(container))
                    expand.callOnClick();

                for (int i = 0; i < details.getChildCount(); i++) {
                    View view = details.getChildAt(i);
                    if (view instanceof TextView) {
                        if (Objects.equals(view.getTag(), ((PieEntry) e).getLabel()))
                            ((TextView) view).setTextColor(ContextCompat.getColor(context, R.color.colorAccent));
                        else
                            ((TextView) view).setTextColor(Utils.getTextViewDefaultColor(context));
                    }
                }
            }

            @Override
            public void onNothingSelected() {
                if (CommonUtils.isExpanded(container))
                    expand.callOnClick();

                for (int i = 0; i < details.getChildCount(); i++) {
                    View view = details.getChildAt(i);
                    if (view instanceof TextView)
                        ((TextView) view).setTextColor(Utils.getTextViewDefaultColor(context));
                }
            }
        });

        if (total_seconds == 0) {
            expand.setVisibility(View.GONE);
            chart.clear();
        }
    }
}
