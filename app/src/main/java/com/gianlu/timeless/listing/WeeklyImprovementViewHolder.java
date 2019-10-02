package com.gianlu.timeless.listing;

import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.StringRes;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.gianlu.commonutils.CommonUtils;
import com.gianlu.timeless.R;

import java.util.Locale;

class WeeklyImprovementViewHolder extends RecyclerView.ViewHolder {
    private final TextView text;
    private final TextView percentage;
    private final ImageView icon;

    WeeklyImprovementViewHolder(LayoutInflater inflater, ViewGroup parent) {
        super(inflater.inflate(R.layout.item_improvement, parent, false));
        text = itemView.findViewById(R.id.improvementCard_text);
        percentage = itemView.findViewById(R.id.improvementCard_percentage);
        icon = itemView.findViewById(R.id.improvementCard_icon);
    }

    private String getString(@StringRes int res) {
        return itemView.getContext().getString(res);
    }

    void bind(float roundedPercent) {
        int color;
        int iconRes;
        String str;
        if (roundedPercent > 0) {
            color = ContextCompat.getColor(icon.getContext(), R.color.green);
            iconRes = R.drawable.baseline_trending_up_24;
            str = getString(R.string.weeklyAverage_better);
        } else if (roundedPercent < 0) {
            color = ContextCompat.getColor(icon.getContext(), R.color.red);
            iconRes = R.drawable.baseline_trending_down_24;
            str = getString(R.string.weeklyAverage_worse);
        } else {
            color = CommonUtils.resolveAttrAsColor(icon.getContext(), android.R.attr.colorControlNormal);
            iconRes = R.drawable.baseline_trending_flat_24;
            str = getString(R.string.weeklyAverage_ok);
        }

        text.setText(str);
        icon.setImageResource(iconRes);
        icon.setImageTintList(ColorStateList.valueOf(color));

        percentage.setText(String.format(Locale.getDefault(), "%.2f%%", roundedPercent));
    }
}
