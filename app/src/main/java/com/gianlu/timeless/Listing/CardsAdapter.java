package com.gianlu.timeless.Listing;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

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
    @SuppressWarnings("unchecked")
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof SummaryViewHolder) {
            ((SummaryViewHolder) holder).bind(context, (Summary) objs.objs.get(position));
        } else if (holder instanceof LineChartViewHolder) {
            ((LineChartViewHolder) holder).bind(context, objs.titles.get(position), (List<Summary>) objs.objs.get(position));
        } else if (holder instanceof BarChartViewHolder) {
            ((BarChartViewHolder) holder).bind(context, objs.titles.get(position), (List<Summary>) objs.objs.get(position));
        } else if (holder instanceof PieChartViewHolder) {
            ((PieChartViewHolder) holder).bind(context, objs.titles.get(position), (List<LoggedEntity>) objs.objs.get(position));
        } else if (holder instanceof ListViewHolder) {
            ((ListViewHolder) holder).bind(context, objs.titles.get(position), (List<LoggedEntity>) objs.objs.get(position));
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
