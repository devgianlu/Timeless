package com.gianlu.timeless.NetIO;

import android.content.Context;
import android.support.annotation.StringRes;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.gianlu.commonutils.CommonUtils;
import com.gianlu.timeless.R;
import com.gianlu.timeless.Utils;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IValueFormatter;
import com.github.mikephil.charting.utils.ViewPortHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class Stats {
    public final List<LoggedEntity> projects;
    public final List<LoggedEntity> languages;
    public final List<LoggedEntity> editors;
    final long total_seconds;
    final long daily_average;
    final long best_day;
    final long best_day_total;

    Stats(JSONObject obj) throws JSONException, ParseException {
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

        editors = new ArrayList<>();
        JSONArray editorsArray = obj.getJSONArray("editors");
        for (int i = 0; i < editorsArray.length(); i++)
            editors.add(new LoggedEntity(editorsArray.getJSONObject(i)));

        JSONObject bestDayObject = obj.getJSONObject("best_day");
        SimpleDateFormat parser = new SimpleDateFormat("MM/dd/yyyy", Locale.getDefault());
        best_day = parser.parse(bestDayObject.getString("date")).getTime();
        best_day_total = bestDayObject.getLong("total_seconds");
    }

    @SuppressWarnings("deprecation")
    public static CardView createSummaryCard(Context context, LayoutInflater inflater, ViewGroup parent, Stats stats) {
        CardView card = (CardView) inflater.inflate(R.layout.summary_card, parent, false);
        LinearLayout container = (LinearLayout) card.findViewById(R.id.summaryCard_container);
        container.addView(CommonUtils.fastTextView(context, Html.fromHtml(
                context.getString(R.string.totalTimeSpent, CommonUtils.timeFormatter(stats.total_seconds)))));
        container.addView(CommonUtils.fastTextView(context, Html.fromHtml(
                context.getString(R.string.dailyAverageTimeSpent, CommonUtils.timeFormatter(stats.daily_average)))));
        container.addView(CommonUtils.fastTextView(context, Html.fromHtml(
                context.getString(R.string.bestDay, Utils.dateFormatter.format(new Date(stats.best_day)), CommonUtils.timeFormatter(stats.best_day_total)))));

        return card;
    }

    public static CardView createPieChartCard(Context context, LayoutInflater inflater, ViewGroup parent, @StringRes int titleRes, List<LoggedEntity> entities) {
        CardView card = (CardView) inflater.inflate(R.layout.stat_card, parent, false);
        final TextView title = (TextView) card.findViewById(R.id.statsCard_title);
        title.setText(titleRes);
        final PieChart chart = (PieChart) card.findViewById(R.id.statsCard_chart);

        chart.setDescription(null);
        chart.setDrawEntryLabels(false);
        chart.setRotationEnabled(false);
        final Legend legend = chart.getLegend();
        legend.setWordWrapEnabled(true);

        final List<PieEntry> entries = new ArrayList<>();
        for (LoggedEntity entity : entities)
            if (entity.percent > .01f)
                entries.add(new PieEntry(entity.percent, entity.name + " (" + String.format(Locale.getDefault(), "%.2f", entity.percent) + "%)"));

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
        chart.setData(new PieData(set));

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
