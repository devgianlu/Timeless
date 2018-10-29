package com.gianlu.timeless.Listing;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.gianlu.commonutils.MaterialColors;
import com.gianlu.timeless.Charting.OnSaveChart;
import com.gianlu.timeless.Models.LoggedEntity;
import com.gianlu.timeless.Models.Summaries;
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
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import androidx.annotation.StringRes;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

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

    void bind(final Context context, final @StringRes int title, final Summaries summaries, final OnSaveChart listener) {
        this.title.setText(title);

        chart.setNoDataText(context.getString(R.string.noData));
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

        MaterialColors colors = MaterialColors.getShuffledInstance();
        Map<String, ILineDataSet> branchToSets = new HashMap<>(summaries.availableBranches.size());
        int i = 0;
        for (Summary summary : summaries) {
            for (LoggedEntity branch : summary.branches) {
                LineDataSet set = (LineDataSet) branchToSets.get(branch.name);
                if (set == null) {
                    int color = ContextCompat.getColor(context, colors.getColor(i));
                    i++;

                    set = new LineDataSet(new ArrayList<Entry>(), branch.name);
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

                set.addEntry(new Entry(set.getEntryCount() + 1, branch.total_seconds));
                if (set.getEntryCount() > 1) set.setDrawCircles(false);
            }
        }

        if (branchToSets.isEmpty()) chart.clear();
        else chart.setData(new LineData(new ArrayList<>(branchToSets.values())));

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.saveImage(chart, title);
            }
        });
    }
}
