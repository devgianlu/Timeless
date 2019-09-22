package com.gianlu.timeless.listing;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.recyclerview.widget.RecyclerView;

import com.gianlu.timeless.Models.Durations;
import com.gianlu.timeless.Models.GlobalSummary;
import com.gianlu.timeless.Models.LoggedEntities;
import com.gianlu.timeless.Models.Summaries;
import com.gianlu.timeless.charts.OnSaveChart;

import java.math.BigDecimal;
import java.util.ArrayList;
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
    private final HelperViewHolder.Listener listener;
    private final OnSaveChart saveChartListener;
    private final CardsList objs;

    public CardsAdapter(@NonNull Context context, CardsList objs, HelperViewHolder.Listener listener, OnSaveChart saveChartListener) {
        this.objs = objs;
        this.inflater = LayoutInflater.from(context);
        this.listener = listener;
        this.saveChartListener = saveChartListener;
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
            CardsList.SummaryItem s = (CardsList.SummaryItem) objs.objs.get(position);
            ((SummaryViewHolder) holder).bind(s.summary, s.context);
        } else if (holder instanceof LineChartViewHolder) {
            ((LineChartViewHolder) holder).bind(objs.titles.get(position), (Summaries) objs.objs.get(position), saveChartListener);
        } else if (holder instanceof BarChartViewHolder) {
            ((BarChartViewHolder) holder).bind(objs.titles.get(position), (Summaries) objs.objs.get(position), saveChartListener);
        } else if (holder instanceof PieChartViewHolder) {
            ((PieChartViewHolder) holder).bind(objs.titles.get(position), (LoggedEntities) objs.objs.get(position), saveChartListener);
        } else if (holder instanceof ListViewHolder) {
            ((ListViewHolder) holder).bind(objs.titles.get(position), (LoggedEntities) objs.objs.get(position));
        } else if (holder instanceof DurationsViewHolder) {
            ((DurationsViewHolder) holder).bind((Durations) objs.objs.get(position));
        } else if (holder instanceof WeeklyImprovementViewHolder) {
            ((WeeklyImprovementViewHolder) holder).bind((Float) objs.objs.get(position));
        } else if (holder instanceof BranchSelectorViewHolder) {
            ((BranchSelectorViewHolder) holder).bind((BranchSelectorViewHolder.Config) objs.objs.get(position));
        }
    }

    @Override
    public int getItemCount() {
        return objs.objs.size();
    }

    public enum SummaryContext {
        MAIN, DAILY_STATS, CUSTOM_RANGE, PROJECTS
    }

    public interface OnBranches {
        void onBranchesChanged(@NonNull List<String> branches);
    }

    public static class CardsList {
        private final List<Integer> titles;
        private final List<Integer> types;
        private final List<Object> objs;

        public CardsList() {
            titles = new ArrayList<>();
            types = new ArrayList<>();
            objs = new ArrayList<>();
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
                titles.add(0, null);
                types.add(0, TYPE_BRANCH_SELECTOR);
                objs.add(0, new BranchSelectorViewHolder.Config(branches, selectedBranches, listener));
            }

            return this;
        }

        public CardsList addImprovement(long today, float beforeAverage) {
            return addImprovement(titles.size(), today, beforeAverage);
        }

        public CardsList addImprovement(int index, long today, float beforeAverage) {
            BigDecimal bd = new BigDecimal(today);
            if (beforeAverage > 0)
                bd = bd.divide(new BigDecimal(beforeAverage), 10, BigDecimal.ROUND_HALF_UP);

            bd = bd.multiply(new BigDecimal(100));
            bd = bd.subtract(new BigDecimal(100));

            titles.add(index, null);
            types.add(index, TYPE_IMPROVEMENT);
            objs.add(index, bd.floatValue());

            return this;
        }

        public CardsList addGlobalSummary(GlobalSummary summary, SummaryContext ctx) {
            return addGlobalSummary(titles.size(), summary, ctx);
        }

        public CardsList addGlobalSummary(int index, GlobalSummary summary, SummaryContext ctx) {
            titles.add(index, null);
            types.add(index, TYPE_SUMMARY);
            objs.add(index, new SummaryItem(summary, ctx));

            return this;
        }

        public CardsList addProjectsBarChart(@StringRes int title, Summaries summaries) {
            return addProjectsBarChart(titles.size(), title, summaries);
        }

        public CardsList addProjectsBarChart(int index, @StringRes int title, Summaries summaries) {
            titles.add(index, title);
            types.add(index, TYPE_PROJECTS_BAR);
            objs.add(index, summaries);

            return this;
        }

        public CardsList addFileList(@StringRes int title, LoggedEntities entities) {
            return addFileList(titles.size(), title, entities);
        }

        public CardsList addFileList(int index, @StringRes int title, LoggedEntities entities) {
            if (entities.size() > 0) {
                titles.add(index, title);
                types.add(index, TYPE_FILE_LIST);
                objs.add(index, entities);
            }

            return this;
        }

        public CardsList addPieChart(@StringRes int title, LoggedEntities entities) {
            return addPieChart(titles.size(), title, entities);
        }

        public CardsList addPieChart(int index, @StringRes int title, LoggedEntities entities) {
            titles.add(index, title);
            types.add(index, TYPE_PIE);
            objs.add(index, entities);

            return this;
        }

        public CardsList addDurations(Durations durations) {
            return addDurations(titles.size(), durations);
        }

        public CardsList addDurations(int index, Durations durations) {
            titles.add(index, null);
            types.add(index, TYPE_DURATIONS);
            objs.add(index, durations);

            return this;
        }

        public CardsList addLineChart(@StringRes int title, Summaries summaries) {
            return addLineChart(titles.size(), title, summaries);
        }

        public CardsList addLineChart(int index, @StringRes int title, Summaries summaries) {
            titles.add(index, title);
            types.add(index, TYPE_LINE);
            objs.add(index, summaries);

            return this;
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
