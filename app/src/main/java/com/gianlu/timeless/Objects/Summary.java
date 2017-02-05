package com.gianlu.timeless.Objects;

import android.content.Context;
import android.support.annotation.StringRes;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.gianlu.commonutils.CommonUtils;
import com.gianlu.timeless.R;
import com.gianlu.timeless.Utils;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.LegendEntry;
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
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.ViewPortHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

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

    public static Summary createRangeSummary(List<Summary> summaries) {
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
        container.addView(CommonUtils.fastTextView(context, Html.fromHtml(context.getString(R.string.totalTimeSpent, Utils.timeFormatterHours(summary.total_seconds, true)))));

        return card;
    }

    public static CardView createProjectsBarChartCard(Context context, LayoutInflater inflater, ViewGroup parent, @StringRes int titleRes, final List<Summary> summaries) {
        CardView card = (CardView) inflater.inflate(R.layout.bar_chart_card, parent, false);
        final TextView title = (TextView) card.findViewById(R.id.barChartCard_title);
        title.setText(titleRes);

        final BarChart chart = (BarChart) card.findViewById(R.id.barChartCard_chart);
        chart.setDescription(null);
        chart.setTouchEnabled(false);

        XAxis xAxis = chart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setValueFormatter(new IAxisValueFormatter() {
            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                return String.valueOf((int) (value - summaries.size() + 1));
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

        final Legend legend = chart.getLegend();
        legend.setWordWrapEnabled(true);

        final List<BarEntry> entries = new ArrayList<>();
        final Map<String, Integer> colorsMap = new HashMap<>();
        final List<Integer> colors = new ArrayList<>();
        List<LegendEntry> legendEntries = new ArrayList<>();
        int colorCount = 0;
        for (int i = 0; i < summaries.size(); i++) {
            Summary summary = summaries.get(i);
            float[] array = new float[summary.projects.size()];

            for (int j = 0; j < summary.projects.size(); j++) {
                LoggedEntity entity = summary.projects.get(j);
                if (colorsMap.containsKey(entity.name)) {
                    colors.add(colorsMap.get(entity.name));
                } else {
                    int color = ContextCompat.getColor(context, Utils.getColor(colorCount));
                    colors.add(color);
                    colorsMap.put(entity.name, color);

                    LegendEntry legendEntry = new LegendEntry();
                    legendEntry.label = entity.name;
                    legendEntry.formColor = color;
                    legendEntries.add(legendEntry);

                    colorCount++;
                }

                array[j] = summary.projects.get(j).total_seconds;
            }

            entries.add(new BarEntry(i, array));
        }
        Collections.reverse(legendEntries);
        legend.setCustom(legendEntries);

        BarDataSet set = new BarDataSet(entries, null);
        set.setDrawValues(false);
        set.setColors(colors);

        chart.setData(new BarData(set));
        chart.setFitBars(true);

        return card;
    }

    @SuppressWarnings("deprecation")
    public static CardView createPieChartCard(final Context context, LayoutInflater inflater, ViewGroup parent, @StringRes int titleRes, List<LoggedEntity> entities) {
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

        final LinearLayout container = (LinearLayout) card.findViewById(R.id.pieChartCard_container);
        final ImageButton expand = (ImageButton) card.findViewById(R.id.pieChartCard_expand);
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

        long total_seconds = LoggedEntity.sumSeconds(entities);
        final LinearLayout details = (LinearLayout) card.findViewById(R.id.pieChartCard_details);
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

        // TODO: Should scroll a bit when opened
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

        return card;
    }
}
