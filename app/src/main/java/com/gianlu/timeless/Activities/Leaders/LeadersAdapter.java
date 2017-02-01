package com.gianlu.timeless.Activities.Leaders;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Typeface;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ScrollView;
import android.widget.TextView;

import com.gianlu.commonutils.CommonUtils;
import com.gianlu.timeless.Objects.Leader;
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
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class LeadersAdapter extends RecyclerView.Adapter<LeadersAdapter.ViewHolder> {
    private final Activity activity;
    private final List<Leader> leaders;
    private final LayoutInflater inflater;
    private final Typeface roboto;

    public LeadersAdapter(Activity activity, List<Leader> leaders) {
        this.activity = activity;
        this.leaders = leaders;

        inflater = LayoutInflater.from(activity);
        roboto = Typeface.createFromAsset(activity.getAssets(), "fonts/Roboto-Light.ttf");
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(inflater.inflate(R.layout.leader_item, parent, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final Leader leader = leaders.get(position);

        holder.rank.setTypeface(roboto);
        holder.rank.setText(String.valueOf(leader.rank));
        holder.name.setText(leader.user.getDisplayName());
        holder.total.setText(Utils.timeFormatterHours(leader.total_seconds));

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @SuppressWarnings("deprecation")
            @SuppressLint("InflateParams")
            @Override
            public void onClick(View v) {
                ScrollView layout = (ScrollView) inflater.inflate(R.layout.leader_dialog, null, false);

                TextView weekTotal = (TextView) layout.findViewById(R.id.leaderDialog_weekTotal);
                weekTotal.setText(Html.fromHtml(activity.getString(R.string.last7DaysTimeSpent, Utils.timeFormatterHours(leader.total_seconds))));

                TextView dailyAverage = (TextView) layout.findViewById(R.id.leaderDialog_dailyAverage);
                dailyAverage.setText(Html.fromHtml(activity.getString(R.string.dailyTimeSpent, Utils.timeFormatterHours(leader.daily_average))));

                SquarePieChart chart = (SquarePieChart) layout.findViewById(R.id.leaderDialog_chart);
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
                builder.setTitle(leader.user.getDisplayName())
                        .setView(layout)
                        .setPositiveButton(android.R.string.ok, null);

                CommonUtils.showDialog(activity, builder);
            }
        });
    }

    @Override
    public int getItemCount() {
        return leaders.size();
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
