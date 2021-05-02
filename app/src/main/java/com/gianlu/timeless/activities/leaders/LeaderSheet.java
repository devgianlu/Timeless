package com.gianlu.timeless.activities.leaders;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.gianlu.commonutils.CommonUtils;
import com.gianlu.commonutils.bottomsheet.ModalBottomSheetHeaderView;
import com.gianlu.commonutils.bottomsheet.ThemedModalBottomSheet;
import com.gianlu.commonutils.dialogs.DialogUtils;
import com.gianlu.commonutils.misc.SuperTextView;
import com.gianlu.commonutils.ui.Toaster;
import com.gianlu.timeless.R;
import com.gianlu.timeless.Utils;
import com.gianlu.timeless.api.models.Leader;
import com.gianlu.timeless.charts.PieChartColorHelper;
import com.gianlu.timeless.charts.SquarePieChart;
import com.gianlu.timeless.colors.LookupColorMapper;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class LeaderSheet extends ThemedModalBottomSheet<Leader, Void> {
    @NonNull
    public static LeaderSheet get() {
        return new LeaderSheet();
    }

    @Override
    protected void onCreateHeader(@NonNull LayoutInflater inflater, @NonNull ModalBottomSheetHeaderView parent, @NonNull Leader leader) {
        parent.setBackgroundColorRes(R.color.colorPrimary);
        parent.setTitle(leader.user.getDisplayName());
    }

    @Override
    protected void onCreateBody(@NonNull LayoutInflater inflater, @NonNull ViewGroup parent, @NonNull Leader leader) {
        inflater.inflate(R.layout.sheet_leader, parent, true);

        SuperTextView rank = parent.findViewById(R.id.leaderSheet_rank);
        SuperTextView weekTotal = parent.findViewById(R.id.leaderSheet_weekTotal);
        SuperTextView dailyAverage = parent.findViewById(R.id.leaderSheet_dailyAverage);
        SquarePieChart chart = parent.findViewById(R.id.leaderSheet_chart);

        rank.setHtml(R.string.rank, leader.rank);
        weekTotal.setHtml(R.string.last7DaysTimeSpent, Utils.timeFormatterHours(leader.total_seconds, true));
        dailyAverage.setHtml(R.string.dailyTimeSpent, Utils.timeFormatterHours(leader.daily_average, true));

        chart.clear();
        chart.setDescription(null);
        chart.setTouchEnabled(false);
        chart.setHoleColor(Color.argb(0, 0, 0, 0));
        chart.setDrawEntryLabels(false);
        chart.setRotationEnabled(false);
        chart.setUsePercentValues(true);

        Legend legend = chart.getLegend();
        legend.setWordWrapEnabled(true);
        legend.setTextColor(CommonUtils.resolveAttrAsColor(requireContext(), android.R.attr.textColorPrimary));

        List<PieEntry> entries = new ArrayList<>();
        for (Map.Entry<String, Long> entry : leader.languages.entrySet())
            entries.add(new PieEntry(entry.getValue(), entry.getKey()));

        PieDataSet set = new PieDataSet(entries, null);
        set.setValueTextSize(15);
        set.setSliceSpace(0);
        set.setValueTextColor(ContextCompat.getColor(parent.getContext(), android.R.color.white));
        set.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                if (value < 10) return "";
                else return String.format(Locale.getDefault(), "%.2f", value) + "%";
            }
        });

        PieChartColorHelper helper = new PieChartColorHelper(chart, LookupColorMapper.get(requireContext(), LookupColorMapper.Type.LANGUAGES));
        helper.setData(new PieData(set));

        isLoading(false);
    }

    @Override
    protected boolean onCustomizeAction(@NonNull final FloatingActionButton action, @NonNull final Leader leader) {
        if (leader.user.getWebsite() == null) return false;

        action.setImageResource(R.drawable.baseline_web_24);
        action.setColorFilter(Color.WHITE);
        action.setOnClickListener(v -> {
            try {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(leader.user.getWebsite())));
            } catch (ActivityNotFoundException ex) {
                DialogUtils.showToast(getContext(), Toaster.build().message(R.string.failedLoading));
            }
        });
        return true;
    }
}
