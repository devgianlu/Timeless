package com.gianlu.timeless.Activities.Leaders;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Typeface;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ScrollView;
import android.widget.TextView;

import com.gianlu.commonutils.CommonUtils;
import com.gianlu.commonutils.InfiniteRecyclerView;
import com.gianlu.timeless.NetIO.WakaTime;
import com.gianlu.timeless.NetIO.WakaTimeException;
import com.gianlu.timeless.Objects.Leader;
import com.gianlu.timeless.Objects.User;
import com.gianlu.timeless.R;
import com.gianlu.timeless.SquarePieChart;
import com.gianlu.timeless.Utils;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IValueFormatter;
import com.github.mikephil.charting.utils.ViewPortHandler;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public class LeadersAdapter extends InfiniteRecyclerView.InfiniteAdapter<LeadersAdapter.ViewHolder, Leader> {
    private final Activity activity;
    private final User me;
    private final Typeface roboto;

    public LeadersAdapter(Activity context, List<Leader> items, int maxPages, User me) {
        super(context, items, maxPages, -1);
        this.activity = context;
        this.roboto = Typeface.createFromAsset(activity.getAssets(), "fonts/Roboto-Light.ttf");
        this.me = me;
    }

    @Nullable
    @Override
    protected Date getDateFromItem(Leader item) {
        return null;
    }

    @Override
    protected void userBindViewHolder(ViewHolder holder, int position) {
        final ItemEnclosure<Leader> leader = items.get(position);

        holder.rank.setTypeface(roboto);
        holder.rank.setText(String.valueOf(leader.getItem().rank));
        holder.name.setText(leader.getItem().user.getDisplayName());
        holder.total.setText(Utils.timeFormatterHours(leader.getItem().total_seconds, true));

        if (Objects.equals(leader.getItem().user.id, me.id)) {
            holder.setIsRecyclable(false);
            holder.itemView.setBackgroundResource(R.color.colorAccent_shadow);
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @SuppressWarnings("deprecation")
            @SuppressLint("InflateParams")
            @Override
            public void onClick(View v) {
                ScrollView layout = (ScrollView) inflater.inflate(R.layout.leader_dialog, null, false);

                TextView weekTotal = (TextView) layout.findViewById(R.id.leaderDialog_weekTotal);
                weekTotal.setText(Html.fromHtml(activity.getString(R.string.last7DaysTimeSpent, Utils.timeFormatterHours(leader.getItem().total_seconds, true))));

                TextView dailyAverage = (TextView) layout.findViewById(R.id.leaderDialog_dailyAverage);
                dailyAverage.setText(Html.fromHtml(activity.getString(R.string.dailyTimeSpent, Utils.timeFormatterHours(leader.getItem().daily_average, true))));

                SquarePieChart chart = (SquarePieChart) layout.findViewById(R.id.leaderDialog_chart);
                chart.setDescription(null);
                chart.setDrawEntryLabels(false);
                chart.setRotationEnabled(false);

                final Legend legend = chart.getLegend();
                legend.setWordWrapEnabled(true);

                final List<PieEntry> entries = new ArrayList<>();
                for (Map.Entry<String, Long> entry : leader.getItem().languages.entrySet())
                    entries.add(new PieEntry(entry.getValue(), entry.getKey()));

                PieDataSet set = new PieDataSet(entries, null);
                set.setValueTextSize(15);
                set.setSliceSpace(0);
                set.setValueTextColor(ContextCompat.getColor(activity, android.R.color.white));
                set.setValueFormatter(new IValueFormatter() {
                    @Override
                    public String getFormattedValue(float value, Entry entry, int dataSetIndex, ViewPortHandler viewPortHandler) {
                        if (value < 10)
                            return "";
                        else
                            return String.format(Locale.getDefault(), "%.2f", value) + "%";
                    }
                });
                set.setColors(Utils.getColors(), activity);
                chart.setData(new PieData(set));
                chart.setUsePercentValues(true);

                AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                builder.setTitle(leader.getItem().user.getDisplayName())
                        .setView(layout)
                        .setPositiveButton(android.R.string.ok, null);

                CommonUtils.showDialog(activity, builder);
            }
        });
    }

    @Override
    protected RecyclerView.ViewHolder createViewHolder(ViewGroup parent) {
        return new ViewHolder(inflater.inflate(R.layout.leader_item, parent, false));
    }

    @Override
    protected void moreContent(final int page, final IContentProvider<Leader> provider) {
        WakaTime.getInstance().getLeaders(context, page, new WakaTime.ILeaders() {
            @Override
            public void onLeaders(List<Leader> leaders, int maxPages) {
                provider.onMoreContent(leaders);
            }

            @Override
            public void onException(Exception ex) {
                provider.onFailed(ex);
            }

            @Override
            public void onWakaTimeException(WakaTimeException ex) {
                provider.onFailed(ex);
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public int find(String id) {
        for (int i = 0; i < items.size(); i++)
            if (items.get(i) != null && Objects.equals(items.get(i).getItem().user.id, id))
                return i;

        return -1;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        final TextView rank;
        final TextView name;
        final TextView total;

        public ViewHolder(View itemView) {
            super(itemView);

            rank = (TextView) itemView.findViewById(R.id.leader_rank);
            name = (TextView) itemView.findViewById(R.id.leader_name);
            total = (TextView) itemView.findViewById(R.id.leader_total);
        }
    }
}
