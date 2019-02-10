package com.gianlu.timeless.Listing;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.gianlu.timeless.Charting.BarChartPrepare;
import com.gianlu.timeless.Charting.OnSaveChart;
import com.gianlu.timeless.Models.Summaries;
import com.gianlu.timeless.R;
import com.github.mikephil.charting.charts.BarChart;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.recyclerview.widget.RecyclerView;

class BarChartViewHolder extends RecyclerView.ViewHolder {
    private final TextView title;
    private final BarChart chart;
    private final ImageButton save;
    private final BarChartPrepare prepare;

    BarChartViewHolder(LayoutInflater inflater, ViewGroup parent) {
        super(inflater.inflate(R.layout.card_chart_bar, parent, false));

        title = itemView.findViewById(R.id.barChartCard_title);
        chart = itemView.findViewById(R.id.barChartCard_chart);
        save = itemView.findViewById(R.id.barChartCard_save);

        prepare = new BarChartPrepare(chart);
    }

    void bind(@NonNull Context context, @StringRes int title, @NonNull Summaries summaries, @Nullable OnSaveChart listener) {
        this.title.setText(title);
        prepare.setup(context, summaries);
        if (listener != null)
            save.setOnClickListener(v -> listener.saveImage(chart, title));
    }
}
