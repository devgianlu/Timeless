package com.gianlu.timeless.Objects;

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
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
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

public class Summary {
    private static final SimpleDateFormat parser = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    public final List<LoggedEntity> projects;
    public final List<LoggedEntity> languages;
    public final List<LoggedEntity> editors;
    public final List<LoggedEntity> operating_systems;
    public long total_seconds;
    public long date;

    private Summary() {
        total_seconds = 0;
        date = -1;
        projects = new ArrayList<>();
        languages = new ArrayList<>();
        editors = new ArrayList<>();
        operating_systems = new ArrayList<>();
    }

    public Summary(JSONObject obj) throws JSONException, ParseException {
        total_seconds = obj.getJSONObject("grand_total").getLong("total_seconds");

        date = parser.parse(obj.getJSONObject("range").getString("date")).getTime();

        JSONArray projectsArray = obj.getJSONArray("projects");
        projects = new ArrayList<>();
        for (int i = 0; i < projectsArray.length(); i++)
            projects.add(new LoggedEntity(projectsArray.getJSONObject(i)));

        JSONArray languagesArray = obj.getJSONArray("languages");
        languages = new ArrayList<>();
        for (int i = 0; i < languagesArray.length(); i++)
            languages.add(new LoggedEntity(languagesArray.getJSONObject(i)));

        JSONArray editorsArray = obj.getJSONArray("editors");
        editors = new ArrayList<>();
        for (int i = 0; i < editorsArray.length(); i++)
            editors.add(new LoggedEntity(editorsArray.getJSONObject(i)));

        JSONArray operatingSystemsArray = obj.getJSONArray("operating_systems");
        operating_systems = new ArrayList<>();
        for (int i = 0; i < operatingSystemsArray.length(); i++)
            operating_systems.add(new LoggedEntity(operatingSystemsArray.getJSONObject(i)));
    }

    public static List<Summary> fromJSON(String json) throws JSONException, ParseException {
        JSONArray array = new JSONObject(json).getJSONArray("data");

        List<Summary> summaries = new ArrayList<>();
        for (int i = 0; i < array.length(); i++)
            summaries.add(new Summary(array.getJSONObject(i)));

        return summaries;
    }

    public static Summary createRangeSummary(List<Summary> summaries) throws JSONException {
        Summary rangeSummary = new Summary();

        for (Summary summary : summaries) {
            rangeSummary.total_seconds += summary.total_seconds;
            LoggedEntity.sum(rangeSummary.editors, summary.editors);
            LoggedEntity.sum(rangeSummary.languages, summary.languages);
            LoggedEntity.sum(rangeSummary.projects, summary.projects);
            LoggedEntity.sum(rangeSummary.operating_systems, summary.operating_systems);
        }

        return rangeSummary;
    }

    @SuppressWarnings("deprecation")
    public static CardView createSummaryCard(Context context, LayoutInflater inflater, ViewGroup parent, Summary summary) {
        CardView card = (CardView) inflater.inflate(R.layout.summary_card, parent, false);
        LinearLayout container = (LinearLayout) card.findViewById(R.id.summaryCard_container);
        container.addView(CommonUtils.fastTextView(context, Html.fromHtml(context.getString(R.string.totalTimeSpent, CommonUtils.timeFormatter(summary.total_seconds)))));
        return card;
    }

    // TODO: Not working
    public static CardView createProjectsBarChartCard(Context context, LayoutInflater inflater, ViewGroup parent, @StringRes int titleRes, List<Summary> summaries) {
        CardView card = (CardView) inflater.inflate(R.layout.bar_chart_card, parent, false);
        final TextView title = (TextView) card.findViewById(R.id.barChartCard_title);
        title.setText(titleRes);

        final BarChart chart = (BarChart) card.findViewById(R.id.barChartCard_chart);
        chart.setDescription(null);
        chart.setTouchEnabled(false);
        XAxis xAxis = chart.getXAxis();
        xAxis.setLabelCount(4);
        xAxis.setValueFormatter(new IAxisValueFormatter() {
            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                return Utils.dateFormatter.format(new Date((long) value));
            }
        });

        chart.getAxisRight().setEnabled(false);
        YAxis yAxis = chart.getAxisLeft();
        yAxis.setAxisMinimum(0f);
        yAxis.setValueFormatter(new IAxisValueFormatter() {
            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                return CommonUtils.timeFormatter((long) value);
            }
        });

        final Legend legend = chart.getLegend();
        legend.setWordWrapEnabled(true);

        final List<BarEntry> entries = new ArrayList<>();
        for (Summary summary : summaries)
            entries.add(new BarEntry(summary.date, LoggedEntity.secondsToFloatArray(summary.projects)));

        BarDataSet set = new BarDataSet(entries, null);
        set.setStackLabels(new String[]{"Births", "Divorces", "Marriages"});
        set.setDrawValues(false);
        Utils.shuffleArray(Utils.COLORS);
        set.setColors(Utils.COLORS, context);

        chart.setData(new BarData(set));
        chart.setFitBars(true);

        return card;
    }

    // TODO: Small LoggedEntity issue
    public static CardView createPieChartCard(Context context, LayoutInflater inflater, ViewGroup parent, @StringRes int titleRes, List<LoggedEntity> entities) {
        CardView card = (CardView) inflater.inflate(R.layout.pie_chart_card, parent, false);
        final TextView title = (TextView) card.findViewById(R.id.pieChartCard_title);
        title.setText(titleRes);

        final PieChart chart = (PieChart) card.findViewById(R.id.pieChartCard_chart);
        chart.setDescription(null);
        chart.setDrawEntryLabels(false);
        chart.setRotationEnabled(false);

        final Legend legend = chart.getLegend();
        legend.setWordWrapEnabled(true);

        final List<PieEntry> entries = new ArrayList<>();
        for (LoggedEntity entity : entities)
            entries.add(new PieEntry(entity.total_seconds, entity.name));

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
        chart.setUsePercentValues(true);
        return card;
    }
}
