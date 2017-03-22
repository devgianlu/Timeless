package com.gianlu.timeless.Listing;

import android.content.Context;
import android.support.v4.util.Pair;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.gianlu.timeless.Objects.Duration;
import com.gianlu.timeless.Objects.LoggedEntity;
import com.gianlu.timeless.Objects.Summary;

import java.util.ArrayList;
import java.util.List;

public class CardsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int TYPE_SUMMARY = 0;
    private static final int TYPE_PROJECTS_BAR = 1;
    private static final int TYPE_PIE = 2;
    private static final int TYPE_LINE = 3;
    private static final int TYPE_FILE_LIST = 4;
    private static final int TYPE_DURATIONS = 5;
    private static final int TYPE_PERCENTAGE = 6;
    private final Context context;
    private final LayoutInflater inflater;
    private final ISaveChart handler;
    private final CardsList objs;

    public CardsAdapter(Context context, CardsList objs, ISaveChart handler) {
        this.context = context;
        this.objs = objs;

        this.inflater = LayoutInflater.from(context);
        this.handler = handler;
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
            case TYPE_DURATIONS:
                return new DurationsViewHolder(inflater, parent);
            case TYPE_PERCENTAGE:
                return new PercentageViewHolder(inflater, parent);
            default:
                return null;
        }
    }

    @Override
    public int getItemViewType(int position) {
        return objs.types.get(position);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof SummaryViewHolder) {
            ((SummaryViewHolder) holder).bind(context, (Summary) objs.objs.get(position));
        } else if (holder instanceof LineChartViewHolder) {
            ((LineChartViewHolder) holder).bind(context, objs.titles.get(position), (List<Summary>) objs.objs.get(position), handler);
        } else if (holder instanceof BarChartViewHolder) {
            ((BarChartViewHolder) holder).bind(context, objs.titles.get(position), (List<Summary>) objs.objs.get(position), handler);
        } else if (holder instanceof PieChartViewHolder) {
            ((PieChartViewHolder) holder).bind(context, objs.titles.get(position), (List<LoggedEntity>) objs.objs.get(position), handler);
        } else if (holder instanceof ListViewHolder) {
            ((ListViewHolder) holder).bind(context, objs.titles.get(position), (List<LoggedEntity>) objs.objs.get(position), handler);
        } else if (holder instanceof DurationsViewHolder) {
            ((DurationsViewHolder) holder).bind(objs.titles.get(position), (List<Duration>) objs.objs.get(position));
        } else if (holder instanceof PercentageViewHolder) {
            ((PercentageViewHolder) holder).bind(objs.titles.get(position), (Pair<Float, Float>) objs.objs.get(position));
        }
    }

    @Override
    public int getItemCount() {
        return objs.objs.size();
    }

    public interface ISaveChart {
        void onWritePermissionRequested(IPermissionRequest handler);

        void onSaveRequested(View chart, String name);
    }

    public interface IPermissionRequest {
        void onGranted();
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

        public CardsList addPercentage(int index, String title, Float before, Float now) {
            titles.add(index, title);
            types.add(index, TYPE_PERCENTAGE);
            objs.add(index, new Pair<>(before, now));

            return this;
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

        public CardsList addDurations(String title, List<Duration> durations) {
            titles.add(title);
            types.add(TYPE_DURATIONS);
            objs.add(durations);

            return this;
        }

        public CardsList addDurations(int index, String title, List<Duration> durations) {
            titles.add(index, title);
            types.add(index, TYPE_DURATIONS);
            objs.add(index, durations);

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
