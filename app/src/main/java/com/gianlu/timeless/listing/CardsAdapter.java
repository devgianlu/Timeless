package com.gianlu.timeless.listing;

import android.content.Context;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.recyclerview.widget.RecyclerView;

import com.gianlu.timeless.api.models.Durations;
import com.gianlu.timeless.api.models.GlobalSummary;
import com.gianlu.timeless.api.models.LoggedEntities;
import com.gianlu.timeless.api.models.Project;
import com.gianlu.timeless.api.models.Summaries;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class CardsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int TYPE_SUMMARY = 0;
    private static final int TYPE_PROJECTS_BAR = 1;
    private static final int TYPE_PIE = 2;
    private static final int TYPE_LINE = 3;
    private static final int TYPE_FILE_LIST = 4;
    private static final int TYPE_DURATIONS = 5;
    private static final int TYPE_IMPROVEMENT = 6;
    private static final int TYPE_BRANCH_SELECTOR = 7;
    private final LayoutInflater inflater;
    @Nullable
    private final Project project;
    private final HelperViewHolder.Listener listener;
    private final CardsList objs;

    public CardsAdapter(@NonNull Context context, @NonNull CardsList objs, @Nullable Project project, @NonNull HelperViewHolder.Listener listener) {
        this.objs = objs;
        this.inflater = LayoutInflater.from(context);
        this.project = project;
        this.listener = listener;
    }

    @Override
    @NonNull
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        switch (viewType) {
            case TYPE_SUMMARY:
                return new SummaryViewHolder(inflater, parent);
            case TYPE_PROJECTS_BAR:
                return new BarChartViewHolder(listener, inflater, parent);
            case TYPE_PIE:
                return new PieChartViewHolder(listener, inflater, parent);
            case TYPE_LINE:
                return new LineChartViewHolder(listener, inflater, parent);
            case TYPE_FILE_LIST:
                return new ListViewHolder(inflater, parent);
            case TYPE_DURATIONS:
                return new DurationsViewHolder(inflater, parent);
            case TYPE_IMPROVEMENT:
                return new WeeklyImprovementViewHolder(inflater, parent);
            case TYPE_BRANCH_SELECTOR:
                return new BranchSelectorViewHolder(listener, inflater, parent);
            default:
                throw new IllegalStateException("Unknown view type: " + viewType);
        }
    }

    @Override
    public int getItemViewType(int position) {
        return objs.types.get(position);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof SummaryViewHolder) {
            CardsList.SummaryItem s = (CardsList.SummaryItem) objs.payloads.get(position);
            ((SummaryViewHolder) holder).bind(s.summary, s.context);
        } else if (holder instanceof LineChartViewHolder) {
            ((LineChartViewHolder) holder).bind(objs.titles.get(position), (Summaries) objs.payloads.get(position), project);
        } else if (holder instanceof BarChartViewHolder) {
            ((BarChartViewHolder) holder).bind(objs.titles.get(position), (Summaries) objs.payloads.get(position), project);
        } else if (holder instanceof PieChartViewHolder) {
            CardsList.PieItem p = (CardsList.PieItem) objs.payloads.get(position);
            ((PieChartViewHolder) holder).bind(objs.titles.get(position), p.entities, p.ctx, p.interval, project);
        } else if (holder instanceof ListViewHolder) {
            ((ListViewHolder) holder).bind(objs.titles.get(position), (LoggedEntities) objs.payloads.get(position));
        } else if (holder instanceof DurationsViewHolder) {
            ((DurationsViewHolder) holder).bind((Durations) objs.payloads.get(position));
        } else if (holder instanceof WeeklyImprovementViewHolder) {
            ((WeeklyImprovementViewHolder) holder).bind((Float) objs.payloads.get(position));
        } else if (holder instanceof BranchSelectorViewHolder) {
            ((BranchSelectorViewHolder) holder).bind((BranchSelectorViewHolder.Config) objs.payloads.get(position));
        }
    }

    @Override
    public int getItemCount() {
        return objs.types.size();
    }

    public enum SummaryContext {
        MAIN, DAILY_STATS, CUSTOM_RANGE, PROJECTS
    }

    public interface OnBranches {
        void onBranchesChanged(@NonNull List<String> branches);
    }

    public static class CardsList {
        private final List<Object> payloads;
        private final List<Integer> types;
        private final List<Integer> titles;

        public CardsList() {
            payloads = new ArrayList<>();
            types = new ArrayList<>();
            titles = new ArrayList<>();
        }

        public boolean hasBranchSelector() {
            for (int type : types)
                if (type == TYPE_BRANCH_SELECTOR)
                    return true;

            return false;
        }

        /**
         * Always on top
         */
        public CardsList addBranchSelector(@NonNull List<String> branches, @NonNull List<String> selectedBranches, @NonNull OnBranches listener) {
            if (!branches.isEmpty()) {
                types.add(0, TYPE_BRANCH_SELECTOR);
                titles.add(0, null);
                payloads.add(0, new BranchSelectorViewHolder.Config(branches, selectedBranches, listener));
            }

            return this;
        }

        public CardsList addImprovement(long today, float beforeAverage) {
            return addImprovement(types.size(), today, beforeAverage);
        }

        public CardsList addImprovement(int index, long today, float beforeAverage) {
            BigDecimal bd = new BigDecimal(today);
            if (beforeAverage > 0)
                bd = bd.divide(new BigDecimal(beforeAverage), 10, BigDecimal.ROUND_HALF_UP);

            bd = bd.multiply(new BigDecimal(100));
            bd = bd.subtract(new BigDecimal(100));

            types.add(index, TYPE_IMPROVEMENT);
            titles.add(index, null);
            payloads.add(index, bd.floatValue());

            return this;
        }

        public CardsList addGlobalSummary(GlobalSummary summary, SummaryContext ctx) {
            return addGlobalSummary(types.size(), summary, ctx);
        }

        public CardsList addGlobalSummary(int index, GlobalSummary summary, SummaryContext ctx) {
            types.add(index, TYPE_SUMMARY);
            titles.add(index, null);
            payloads.add(index, new SummaryItem(summary, ctx));

            return this;
        }

        public CardsList addProjectsBarChart(@StringRes int title, Summaries summaries) {
            return addProjectsBarChart(types.size(), title, summaries);
        }

        public CardsList addProjectsBarChart(int index, @StringRes int title, Summaries summaries) {
            types.add(index, TYPE_PROJECTS_BAR);
            titles.add(index, title);
            payloads.add(index, summaries);

            return this;
        }

        public CardsList addFileList(@StringRes int title, LoggedEntities entities) {
            return addFileList(types.size(), title, entities);
        }

        public CardsList addFileList(int index, @StringRes int title, LoggedEntities entities) {
            if (entities.size() > 0) {
                titles.add(index, title);
                types.add(index, TYPE_FILE_LIST);
                payloads.add(index, entities);
            }

            return this;
        }

        public CardsList addPieChart(@StringRes int title, PieChartViewHolder.ChartContext ctx, Pair<Date, Date> interval, LoggedEntities entities) {
            return addPieChart(types.size(), title, ctx, interval, entities);
        }

        public CardsList addPieChart(int index, @StringRes int title, PieChartViewHolder.ChartContext ctx, Pair<Date, Date> interval, LoggedEntities entities) {
            titles.add(index, title);
            types.add(index, TYPE_PIE);
            payloads.add(index, new PieItem(ctx, interval, entities));

            return this;
        }

        public CardsList addDurations(Durations durations) {
            return addDurations(types.size(), durations);
        }

        public CardsList addDurations(int index, Durations durations) {
            titles.add(index, null);
            types.add(index, TYPE_DURATIONS);
            payloads.add(index, durations);

            return this;
        }

        public CardsList addLineChart(@StringRes int title, Summaries summaries) {
            return addLineChart(types.size(), title, summaries);
        }

        public CardsList addLineChart(int index, @StringRes int title, Summaries summaries) {
            titles.add(index, title);
            types.add(index, TYPE_LINE);
            payloads.add(index, summaries);

            return this;
        }

        private static class PieItem {
            private final PieChartViewHolder.ChartContext ctx;
            private final Pair<Date, Date> interval;
            private final LoggedEntities entities;

            PieItem(PieChartViewHolder.ChartContext ctx, Pair<Date, Date> interval, LoggedEntities entities) {
                this.ctx = ctx;
                this.interval = interval;
                this.entities = entities;
            }
        }

        private static class SummaryItem {
            final GlobalSummary summary;
            final SummaryContext context;

            SummaryItem(GlobalSummary summary, SummaryContext context) {
                this.summary = summary;
                this.context = context;
            }
        }
    }
}
