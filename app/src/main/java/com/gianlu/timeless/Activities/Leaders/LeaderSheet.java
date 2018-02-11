package com.gianlu.timeless.Activities.Leaders;

import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.gianlu.commonutils.MaterialColors;
import com.gianlu.commonutils.NiceBaseBottomSheet;
import com.gianlu.commonutils.SuperTextView;
import com.gianlu.timeless.Charting.SquarePieChart;
import com.gianlu.timeless.Models.Leader;
import com.gianlu.timeless.R;
import com.gianlu.timeless.Utils;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IValueFormatter;
import com.github.mikephil.charting.utils.ViewPortHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class LeaderSheet extends NiceBaseBottomSheet {

    public LeaderSheet(ViewGroup parent) {
        super(parent, R.layout.sheet_header_leader, R.layout.sheet_leader, false);
    }

    @Override
    protected boolean onPrepareAction(@NonNull FloatingActionButton fab, Object... payloads) {
        final String website = ((Leader) payloads[0]).user.getWebsite();
        if (website != null) {
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    v.getContext().startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(website)));
                }
            });

            return true;
        }

        return false;
    }

    @Override
    protected void onCreateHeaderView(@NonNull ViewGroup parent, Object... payloads) {
        TextView name = parent.findViewById(R.id.leaderSheet_name);
        name.setText(((Leader) payloads[0]).user.getDisplayName());

        parent.setBackgroundResource(R.color.colorPrimary);
    }

    @Override
    protected void onCreateContentView(@NonNull ViewGroup parent, Object... payloads) {
        SuperTextView rank = parent.findViewById(R.id.leaderSheet_rank);
        SuperTextView weekTotal = parent.findViewById(R.id.leaderSheet_weekTotal);
        SuperTextView dailyAverage = parent.findViewById(R.id.leaderSheet_dailyAverage);
        SquarePieChart chart = parent.findViewById(R.id.leaderSheet_chart);

        Leader leader = (Leader) payloads[0];

        rank.setHtml(R.string.rank, leader.rank);
        weekTotal.setHtml(R.string.last7DaysTimeSpent, Utils.timeFormatterHours(leader.total_seconds, true));
        dailyAverage.setHtml(R.string.dailyTimeSpent, Utils.timeFormatterHours(leader.daily_average, true));

        chart.clear();
        chart.setDescription(null);
        chart.setDrawEntryLabels(false);
        chart.setRotationEnabled(false);

        Legend legend = chart.getLegend();
        legend.setWordWrapEnabled(true);

        List<PieEntry> entries = new ArrayList<>();
        for (Map.Entry<String, Long> entry : leader.languages.entrySet())
            entries.add(new PieEntry(entry.getValue(), entry.getKey()));

        PieDataSet set = new PieDataSet(entries, null);
        set.setValueTextSize(15);
        set.setSliceSpace(0);
        set.setValueTextColor(ContextCompat.getColor(parent.getContext(), android.R.color.white));
        set.setValueFormatter(new IValueFormatter() {
            @Override
            public String getFormattedValue(float value, Entry entry, int dataSetIndex, ViewPortHandler viewPortHandler) {
                if (value < 10) return "";
                else return String.format(Locale.getDefault(), "%.2f", value) + "%";
            }
        });
        set.setColors(MaterialColors.getShuffledInstance().getColorsRes(), parent.getContext());
        chart.setData(new PieData(set));
        chart.setUsePercentValues(true);
        chart.invalidate();
    }
}