package com.gianlu.timeless.Activities.Leaders;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Typeface;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ScrollView;
import android.widget.TextView;

import com.gianlu.commonutils.CommonUtils;
import com.gianlu.commonutils.InfiniteRecyclerView;
import com.gianlu.commonutils.SuperTextView;
import com.gianlu.timeless.Models.Leader;
import com.gianlu.timeless.Models.User;
import com.gianlu.timeless.NetIO.WakaTime;
import com.gianlu.timeless.NetIO.WakaTimeException;
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

    public LeadersAdapter(Activity context, List<Leader> items, int maxPages, @Nullable Leader me) {
        super(context, items, maxPages, -1, false);
        this.activity = context;
        this.roboto = Typeface.createFromAsset(activity.getAssets(), "fonts/Roboto-Light.ttf");
        if (me != null) this.me = me.user;
        else this.me = null;
    }

    @SuppressWarnings("deprecation")
    @SuppressLint("InflateParams")
    public static void displayRankDialog(Activity activity, Leader leader) {
        ScrollView layout = (ScrollView) activity.getLayoutInflater().inflate(R.layout.leader_dialog, null, false);

        SuperTextView rank = layout.findViewById(R.id.leaderDialog_rank);
        rank.setHtml(R.string.rank, leader.rank);

        SuperTextView weekTotal = layout.findViewById(R.id.leaderDialog_weekTotal);
        weekTotal.setHtml(R.string.last7DaysTimeSpent, Utils.timeFormatterHours(leader.total_seconds, true));

        SuperTextView dailyAverage = layout.findViewById(R.id.leaderDialog_dailyAverage);
        dailyAverage.setHtml(R.string.dailyTimeSpent, Utils.timeFormatterHours(leader.daily_average, true));

        SquarePieChart chart = layout.findViewById(R.id.leaderDialog_chart);
        chart.setDescription(null);
        chart.setDrawEntryLabels(false);
        chart.setRotationEnabled(false);

        final Legend legend = chart.getLegend();
        legend.setWordWrapEnabled(true);

        final List<PieEntry> entries = new ArrayList<>();
        for (Map.Entry<String, Long> entry : leader.languages.entrySet())
            entries.add(new PieEntry(entry.getValue(), entry.getKey()));

        PieDataSet set = new PieDataSet(entries, null);
        set.setValueTextSize(15);
        set.setSliceSpace(0);
        set.setValueTextColor(ContextCompat.getColor(activity, android.R.color.white));
        set.setValueFormatter(new IValueFormatter() {
            @Override
            public String getFormattedValue(float value, Entry entry, int dataSetIndex, ViewPortHandler viewPortHandler) {
                if (value < 10) return "";
                else return String.format(Locale.getDefault(), "%.2f", value) + "%";
            }
        });
        set.setColors(Utils.getColors(), activity);
        chart.setData(new PieData(set));
        chart.setUsePercentValues(true);

        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle(leader.user.getDisplayName())
                .setView(layout)
                .setPositiveButton(android.R.string.ok, null);

        CommonUtils.showDialog(activity, builder);
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

        if (me != null && Objects.equals(leader.getItem().user.id, me.id)) {
            holder.setIsRecyclable(false);
            holder.itemView.setBackgroundResource(R.color.colorAccent_shadow);
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @SuppressWarnings("deprecation")
            @SuppressLint("InflateParams")
            @Override
            public void onClick(View v) {
                displayRankDialog(activity, leader.getItem());
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
            public void onLeaders(List<Leader> leaders, Leader me, int maxPages) {
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

    public class ViewHolder extends RecyclerView.ViewHolder {
        final TextView rank;
        final TextView name;
        final TextView total;

        public ViewHolder(View itemView) {
            super(itemView);

            rank = itemView.findViewById(R.id.leader_rank);
            name = itemView.findViewById(R.id.leader_name);
            total = itemView.findViewById(R.id.leader_total);
        }
    }
}
