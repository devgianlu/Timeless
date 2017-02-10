package com.gianlu.timeless.Listing;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.gianlu.commonutils.CommonUtils;
import com.gianlu.timeless.Objects.Summary;
import com.gianlu.timeless.R;
import com.gianlu.timeless.Utils;

class SummaryViewHolder extends RecyclerView.ViewHolder {
    private final LinearLayout container;

    SummaryViewHolder(LayoutInflater inflater, ViewGroup parent) {
        super(inflater.inflate(R.layout.summary_card, parent, false));
        container = (LinearLayout) itemView.findViewById(R.id.summaryCard_container);
    }

    @SuppressWarnings("deprecation")
    void bind(Context context, Summary summary) {
        container.removeAllViews();
        container.addView(CommonUtils.fastTextView(context,
                Html.fromHtml(
                        context.getString(
                                R.string.totalTimeSpent,
                                Utils.timeFormatterHours(summary.total_seconds, true)))));
    }
}
