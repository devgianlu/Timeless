package com.gianlu.timeless.Listing;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.gianlu.commonutils.CommonUtils;
import com.gianlu.timeless.Activities.Projects.FilesAdapter;
import com.gianlu.timeless.Objects.LoggedEntity;
import com.gianlu.timeless.Objects.Summary;
import com.gianlu.timeless.R;
import com.gianlu.timeless.Utils;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.LegendEntry;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.formatter.IValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.ViewPortHandler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public class CardsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int TYPE_SUMMARY = 0;
    private static final int TYPE_PROJECTS_BAR = 1;
    private static final int TYPE_PIE = 2;
    private static final int TYPE_LINE = 3;
    private static final int TYPE_FILE_LIST = 4;
    private final Context context;
    private final LayoutInflater inflater;
    private final CardsList objs;

    public CardsAdapter(Context context, CardsList objs) {
        this.context = context;
        this.objs = objs;

        this.inflater = LayoutInflater.from(context);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case TYPE_SUMMARY:
                return new SummaryViewHolder(inflater, parent);
            case TYPE_PROJECTS_BAR:
                return new BarChartViewHolder(inflater, parent);
            case TYPE_PIE:
                return new PieChartViewHolder(inflater, parent);
            case TYPE_LINE:
                return new LineChartViewHolder(inflater, parent);
            case TYPE_FILE_LIST:
                return new ListViewHolder(inflater, parent);
            default:
                return null;
        }
    }

    @Override
    public int getItemViewType(int position) {
        return objs.types.get(position);
    }

    @Override
    @SuppressWarnings({"unchecked", "deprecation"})
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof SummaryViewHolder) {
            Summary summary = (Summary) objs.objs.get(position);
            SummaryViewHolder castHolder = (SummaryViewHolder) holder;
            castHolder.container.addView(CommonUtils.fastTextView(context, Html.fromHtml(context.getString(R.string.totalTimeSpent, Utils.timeFormatterHours(summary.total_seconds, true)))));
        } else if (holder instanceof LineChartViewHolder) {
            final List<Summary> summaries = (List<Summary>) objs.objs.get(position);
            LineChartViewHolder castHolder = (LineChartViewHolder) holder;
            castHolder.title.setText(objs.titles.get(position));

            castHolder.chart.setDescription(null);
            castHolder.chart.setTouchEnabled(false);

            XAxis xAxis = castHolder.chart.getXAxis();
            xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
            xAxis.setValueFormatter(new IAxisValueFormatter() {
                @Override
                public String getFormattedValue(float value, AxisBase axis) {
                    return String.valueOf((int) (value - summaries.size() + 1));
                }
            });

            castHolder.chart.getAxisRight().setEnabled(false);
            YAxis leftAxis = castHolder.chart.getAxisLeft();
            leftAxis.setEnabled(true);
            leftAxis.setAxisMinimum(0f);
            leftAxis.setValueFormatter(new IAxisValueFormatter() {
                @Override
                public String getFormattedValue(float value, AxisBase axis) {
                    return Utils.timeFormatterHours((long) value, false);
                }
            });

            castHolder.chart.getLegend().setEnabled(false);

            List<Entry> entries = new ArrayList<>();
            for (int i = 0; i < summaries.size(); i++) {
                Summary summary = summaries.get(i);
                entries.add(new Entry(i, summary.total_seconds));
            }

            LineDataSet set = new LineDataSet(entries, null);
            set.setDrawValues(false);
            set.setDrawCircles(false);
            set.setFillColor(ContextCompat.getColor(context, R.color.colorAccent));
            set.setFillAlpha(100);
            set.setMode(LineDataSet.Mode.CUBIC_BEZIER);
            set.setColor(ContextCompat.getColor(context, R.color.colorAccent));
            set.setDrawFilled(true);
            castHolder.chart.setData(new LineData(set));
        } else if (holder instanceof BarChartViewHolder) {
            final List<Summary> summaries = (List<Summary>) objs.objs.get(position);
            BarChartViewHolder castHolder = (BarChartViewHolder) holder;

            castHolder.title.setText(objs.titles.get(position));

            castHolder.chart.setDescription(null);
            castHolder.chart.setTouchEnabled(false);

            XAxis xAxis = castHolder.chart.getXAxis();
            xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
            xAxis.setValueFormatter(new IAxisValueFormatter() {
                @Override
                public String getFormattedValue(float value, AxisBase axis) {
                    return String.valueOf((int) (value - summaries.size() + 1));
                }
            });

            castHolder.chart.getAxisRight().setEnabled(false);
            YAxis leftAxis = castHolder.chart.getAxisLeft();
            leftAxis.setEnabled(true);
            leftAxis.setAxisMinimum(0f);
            leftAxis.setValueFormatter(new IAxisValueFormatter() {
                @Override
                public String getFormattedValue(float value, AxisBase axis) {
                    return Utils.timeFormatterHours((long) value, false);
                }
            });

            final Legend legend = castHolder.chart.getLegend();
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

            castHolder.chart.setData(new BarData(set));
            castHolder.chart.setFitBars(true);
        } else if (holder instanceof PieChartViewHolder) {
            final List<LoggedEntity> entities = (List<LoggedEntity>) objs.objs.get(position);
            final PieChartViewHolder castHolder = (PieChartViewHolder) holder;
            castHolder.title.setText(objs.titles.get(position));

            castHolder.chart.setDescription(null);
            castHolder.chart.setNoDataText(context.getString(R.string.noData));
            castHolder.chart.setDrawEntryLabels(false);
            castHolder.chart.setRotationEnabled(false);

            final Legend legend = castHolder.chart.getLegend();
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
            castHolder.chart.setData(new PieData(set));
            castHolder.chart.setUsePercentValues(true);

            castHolder.expand.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    CommonUtils.animateCollapsingArrowBellows(castHolder.expand, CommonUtils.isExpanded(castHolder.container));

                    if (CommonUtils.isExpanded(castHolder.container))
                        CommonUtils.collapse(castHolder.container);
                    else
                        CommonUtils.expand(castHolder.container);
                }
            });

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
                castHolder.details.addView(text);
            }

            castHolder.chart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
                @Override
                public void onValueSelected(Entry e, Highlight h) {
                    if (!CommonUtils.isExpanded(castHolder.container))
                        castHolder.expand.callOnClick();

                    for (int i = 0; i < castHolder.details.getChildCount(); i++) {
                        View view = castHolder.details.getChildAt(i);
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
                    if (CommonUtils.isExpanded(castHolder.container))
                        castHolder.expand.callOnClick();

                    for (int i = 0; i < castHolder.details.getChildCount(); i++) {
                        View view = castHolder.details.getChildAt(i);
                        if (view instanceof TextView)
                            ((TextView) view).setTextColor(Utils.getTextViewDefaultColor(context));
                    }
                }
            });

            if (total_seconds == 0) {
                castHolder.expand.setVisibility(View.GONE);
                castHolder.chart.clear();
            }
        } else if (holder instanceof ListViewHolder) {
            final List<LoggedEntity> entities = (List<LoggedEntity>) objs.objs.get(position);
            final ListViewHolder castHolder = (ListViewHolder) holder;
            castHolder.title.setText(objs.titles.get(position));

            castHolder.list.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false));
            castHolder.list.setAdapter(new FilesAdapter(context, entities));
        }
    }

    @Override
    public int getItemCount() {
        return objs.objs.size();
    }

    public static class CardsList {
        private final List<String> titles;
        private final List<Integer> types;
        private final List<Object> objs;

        public CardsList() {
            titles = new ArrayList<>();
            types = new ArrayList<>();
            objs = new ArrayList<>();
        }

        public CardsList addSummary(Summary summary) {
            titles.add(null);
            types.add(TYPE_SUMMARY);
            objs.add(summary);

            return this;
        }

        public CardsList addProjectsBarChart(String title, List<Summary> summaries) {
            titles.add(title);
            types.add(TYPE_PROJECTS_BAR);
            objs.add(summaries);

            return this;
        }

        public CardsList addFileList(String title, List<LoggedEntity> entities) {
            if (entities.size() > 0) {
                titles.add(title);
                types.add(TYPE_FILE_LIST);
                objs.add(entities);
            }

            return this;
        }

        public CardsList addPieChart(String title, List<LoggedEntity> entities) {
            titles.add(title);
            types.add(TYPE_PIE);
            objs.add(entities);

            return this;
        }

        public CardsList addLineChart(String title, List<Summary> summaries) {
            titles.add(title);
            types.add(TYPE_LINE);
            objs.add(summaries);

            return this;
        }
    }
}
