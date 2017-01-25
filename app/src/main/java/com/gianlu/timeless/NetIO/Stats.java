package com.gianlu.timeless.NetIO;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;

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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class Stats {
    final long total_seconds;
    final long daily_average;
    final List<LoggedEntity> projects;
    final List<LoggedEntity> languages;

    public Stats(JSONObject obj) throws JSONException {
        total_seconds = obj.getLong("total_seconds");
        daily_average = obj.getLong("daily_average");

        projects = new ArrayList<>();
        JSONArray projectsArray = obj.getJSONArray("projects");
        for (int i = 0; i < projectsArray.length(); i++)
            projects.add(new LoggedEntity(projectsArray.getJSONObject(i)));

        languages = new ArrayList<>();
        JSONArray languagesArray = obj.getJSONArray("languages");
        for (int i = 0; i < languagesArray.length(); i++)
            languages.add(new LoggedEntity(languagesArray.getJSONObject(i)));
    }

    public static CardView createRangeProjectsSummary(Context context, LayoutInflater inflater, ViewGroup container, Stats stats) {
        CardView card = (CardView) inflater.inflate(R.layout.stat_card, container, false);
        final TextView title = (TextView) card.findViewById(R.id.statsCard_title);
        title.setText(context.getString(R.string.projectsSummary));
        final PieChart chart = (PieChart) card.findViewById(R.id.statsCard_chart);

        chart.setDescription(null);
        chart.setDrawEntryLabels(false);
        chart.setRotationEnabled(false);
        final Legend legend = chart.getLegend();
        legend.setWordWrapEnabled(true);

        List<PieEntry> entries = new ArrayList<>();
        for (LoggedEntity project : stats.projects) {
            entries.add(new PieEntry(project.percent, project.name + " (" + String.format(Locale.getDefault(), "%.2f", project.percent) + "%)"));
        }

        PieDataSet set = new PieDataSet(entries, null);
        set.setValueTextSize(15);
        set.setSliceSpace(2);
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
        Utils.shuffleArray(Utils.COLORS);
        set.setColors(Utils.COLORS, context);
        PieData data = new PieData(set);
        chart.setData(data);
        chart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, Highlight h) {
                System.out.println("e = [" + e + "], h = [" + h + "]");
            }

            @Override
            public void onNothingSelected() {
                System.out.println("Stats.onNothingSelected");
            }
        });

        return card;
    }

    public static CardView createRangeLanguagesSummary(Context context, LayoutInflater inflater, ViewGroup container, Stats stats) {
        CardView card = (CardView) inflater.inflate(R.layout.stat_card, container, false);
        final TextView title = (TextView) card.findViewById(R.id.statsCard_title);
        title.setText(context.getString(R.string.languagesSummary));
        final PieChart chart = (PieChart) card.findViewById(R.id.statsCard_chart);

        chart.setDescription(null);
        chart.setDrawEntryLabels(false);
        chart.setRotationEnabled(false);
        final Legend legend = chart.getLegend();
        legend.setWordWrapEnabled(true);

        List<PieEntry> entries = new ArrayList<>();
        for (LoggedEntity language : stats.languages) {
            entries.add(new PieEntry(language.percent, language.name + " (" + String.format(Locale.getDefault(), "%.2f", language.percent) + "%)"));
        }

        PieDataSet set = new PieDataSet(entries, null);
        set.setValueTextSize(15);
        set.setSliceSpace(2);
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
        Utils.shuffleArray(Utils.COLORS);
        set.setColors(Utils.COLORS, context);
        PieData data = new PieData(set);
        chart.setData(data);
        chart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, Highlight h) {
                System.out.println("e = [" + e + "], h = [" + h + "]");
            }

            @Override
            public void onNothingSelected() {
                System.out.println("Stats.onNothingSelected");
            }
        });

        return card;
    }

    public enum Range {
        LAST_7_DAYS,
        LAST_30_DAYS,
        LAST_6_MONTHS,
        LAST_YEAR;

        public String toValidFormat() {
            switch (this) {
                default:
                case LAST_7_DAYS:
                    return "last_7_days";
                case LAST_30_DAYS:
                    return "last_30_days";
                case LAST_6_MONTHS:
                    return "last_6_months";
                case LAST_YEAR:
                    return "last_year";
            }
        }

        public int getDaysNumber() {
            switch (this) {
                default:
                case LAST_7_DAYS:
                    return 7;
                case LAST_30_DAYS:
                    return 30;
                case LAST_6_MONTHS:
                    return 180;
                case LAST_YEAR:
                    return 365;
            }
        }

        public String getFormal(Context context) {
            switch (this) {
                default:
                case LAST_7_DAYS:
                    return context.getString(R.string.last_7_days);
                case LAST_30_DAYS:
                    return context.getString(R.string.last_30_days);
                case LAST_6_MONTHS:
                    return context.getString(R.string.last_6_months);
                case LAST_YEAR:
                    return context.getString(R.string.last_year);
            }
        }
    }
}
