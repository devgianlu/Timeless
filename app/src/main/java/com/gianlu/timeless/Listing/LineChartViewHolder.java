package com.gianlu.timeless.Listing;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.annotation.StringRes;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.gianlu.timeless.Charting.OnSaveChart;
import com.gianlu.timeless.Models.Summary;
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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

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

    void bind(final Context context, final @StringRes int title, final List<Summary> summaries, final OnSaveChart handler) {
        this.title.setText(title);

        chart.setDescription(null);
        chart.setTouchEnabled(false);

        XAxis xAxis = chart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
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

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    handler.onSaveRequested(chart, Utils.getFileName(context, title));
                } else {
                    handler.onWritePermissionRequested(new CardsAdapter.IPermissionRequest() {
                        @Override
                        public void onGranted() {
                            handler.onSaveRequested(chart, Utils.getFileName(context, title));
                        }
                    });
                }
            }
        });
    }
}
