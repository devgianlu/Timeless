package com.gianlu.timeless.Listing;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.v4.util.Pair;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.gianlu.commonutils.CommonUtils;
import com.gianlu.timeless.Charting.OnSaveChart;
import com.gianlu.timeless.Models.Duration;
import com.gianlu.timeless.Models.GlobalSummary;
import com.gianlu.timeless.Models.LoggedEntity;
import com.gianlu.timeless.Models.Summary;

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
    private static final int TYPE_BRANCH_SELECTOR = 7;
    private final Context context;
    private final LayoutInflater inflater;
    private final IAdapter listener;
    private final OnSaveChart saveChartListener;
    private final CardsList objs;

    public CardsAdapter(@NonNull Context context, CardsList objs, IAdapter listener, OnSaveChart saveChartListener) {
        this.context = context;
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
            case TYPE_BRANCH_SELECTOR:
                return new BranchSelectorViewHolder(inflater, parent);
            default:
                throw new IllegalStateException("Unknown view type: " + viewType);
        }
    }

    @Override
    public int getItemViewType(int position) {
        return objs.types.get(position);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof SummaryViewHolder) {
            ((SummaryViewHolder) holder).bind(context, (Summary) objs.objs.get(position));
        } else if (holder instanceof LineChartViewHolder) {
            ((LineChartViewHolder) holder).bind(context, objs.titles.get(position), (List<Summary>) objs.objs.get(position), saveChartListener);
        } else if (holder instanceof BarChartViewHolder) {
            ((BarChartViewHolder) holder).bind(context, objs.titles.get(position), (List<Summary>) objs.objs.get(position), saveChartListener);
        } else if (holder instanceof PieChartViewHolder) {
            ((PieChartViewHolder) holder).bind(context, objs.titles.get(position), (List<LoggedEntity>) objs.objs.get(position), saveChartListener);
        } else if (holder instanceof ListViewHolder) {
            ((ListViewHolder) holder).bind(context, objs.titles.get(position), (List<LoggedEntity>) objs.objs.get(position));
        } else if (holder instanceof DurationsViewHolder) {
            ((DurationsViewHolder) holder).bind(objs.titles.get(position), (List<Duration>) objs.objs.get(position));
        } else if (holder instanceof PercentageViewHolder) {
            ((PercentageViewHolder) holder).bind(objs.titles.get(position), (Pair<Long, Float>) objs.objs.get(position));
        } else if (holder instanceof BranchSelectorViewHolder) {
            ((BranchSelectorViewHolder) holder).bind(context, (BranchSelectorViewHolder.Config) objs.objs.get(position), listener);
        }

        if (!(holder instanceof BranchSelectorViewHolder))
            CommonUtils.setRecyclerViewTopMargin(context, holder);
    }

    @Override
    public int getItemCount() {
        return objs.objs.size();
    }

    public interface IAdapter {
        void showDialog(AlertDialog.Builder builder);
    }

    public interface IPermissionRequest {
        void onGranted();
    }

    public interface IBranches {
        void onBranchesChanged(List<String> branches);
    }

    @SuppressWarnings({"SameParameterValue", "UnusedReturnValue", "unused", "WeakerAccess"})
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
        public CardsList addBranchSelector(List<String> branches, List<String> selectedBranches, IBranches listener) {
            if (!branches.isEmpty()) {
                titles.add(0, null);
                types.add(0, TYPE_BRANCH_SELECTOR);
                objs.add(0, new BranchSelectorViewHolder.Config(branches, selectedBranches, listener));
            }

            return this;
        }

        public CardsList addPercentage(@StringRes int title, long today, float beforeAverage) {
            return addPercentage(titles.size(), title, today, beforeAverage);
        }

        public CardsList addPercentage(int index, @StringRes int title, long today, float beforeAverage) {
            titles.add(index, title);
            types.add(index, TYPE_PERCENTAGE);
            objs.add(index, new Pair<>(today, beforeAverage));

            return this;
        }

        public CardsList addGlobalSummary(GlobalSummary summary) {
            return addGlobalSummary(titles.size(), summary);
        }

        public CardsList addGlobalSummary(int index, GlobalSummary summary) {
            titles.add(index, null);
            types.add(index, TYPE_SUMMARY);
            objs.add(index, summary);

            return this;
        }

        public CardsList addProjectsBarChart(@StringRes int title, List<Summary> summaries) {
            return addProjectsBarChart(titles.size(), title, summaries);
        }

        public CardsList addProjectsBarChart(int index, @StringRes int title, List<Summary> summaries) {
            titles.add(index, title);
            types.add(index, TYPE_PROJECTS_BAR);
            objs.add(index, summaries);

            return this;
        }

        public CardsList addFileList(@StringRes int title, List<LoggedEntity> entities) {
            return addFileList(titles.size(), title, entities);
        }

        public CardsList addFileList(int index, @StringRes int title, List<LoggedEntity> entities) {
            if (entities.size() > 0) {
                titles.add(index, title);
                types.add(index, TYPE_FILE_LIST);
                objs.add(index, entities);
            }

            return this;
        }

        public CardsList addPieChart(@StringRes int title, List<LoggedEntity> entities) {
            return addPieChart(titles.size(), title, entities);
        }

        public CardsList addPieChart(int index, @StringRes int title, List<LoggedEntity> entities) {
            titles.add(index, title);
            types.add(index, TYPE_PIE);
            objs.add(index, entities);

            return this;
        }

        public CardsList addDurations(@StringRes int title, List<Duration> durations) {
            return addDurations(titles.size(), title, durations);
        }

        public CardsList addDurations(int index, @StringRes int title, List<Duration> durations) {
            titles.add(index, title);
            types.add(index, TYPE_DURATIONS);
            objs.add(index, durations);

            return this;
        }

        public CardsList addLineChart(@StringRes int title, List<Summary> summaries) {
            return addLineChart(titles.size(), title, summaries);
        }

        public CardsList addLineChart(int index, @StringRes int title, List<Summary> summaries) {
            titles.add(index, title);
            types.add(index, TYPE_LINE);
            objs.add(index, summaries);

            return this;
        }
    }
}
