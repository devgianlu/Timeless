package com.gianlu.timeless.Listing;

import android.text.Html;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.gianlu.timeless.Listing.CardsAdapter.SummaryContext;
import com.gianlu.timeless.Models.GlobalSummary;
import com.gianlu.timeless.R;
import com.gianlu.timeless.Utils;

import java.text.SimpleDateFormat;
import java.util.Locale;

class SummaryViewHolder extends RecyclerView.ViewHolder {
    private final TextView text;

    SummaryViewHolder(@NonNull LayoutInflater inflater, @NonNull ViewGroup parent) {
        super(inflater.inflate(R.layout.item_summary, parent, false));
        text = (TextView) itemView;
    }

    void bind(@NonNull GlobalSummary summary, @NonNull SummaryContext ctx) {
        TimeInterval interval = TimeInterval.fromDays(summary.days, ctx);
        String str = "You spent a total of <b>" + Utils.timeFormatterHours(summary.total_seconds, true) + "</b> coding " + interval.format(summary) + ".";

        if (summary.days > 1) {
            long average = summary.total_seconds / summary.days;
            str += " This is a daily average of <b>" + Utils.timeFormatterHours(average, true) + "</b>.";
        }

        text.setText(Html.fromHtml(str));
    }

    public enum TimeInterval {
        TODAY("today"), THIS_WEEK("this week"), THIS_MONTH("this month"), THIS_PERIOD("this period"),
        DATE_ONLY("on %s"), DATE_RANGE_ONLY("from %s to %s");

        private final String str;

        TimeInterval(String str) {
            this.str = str;
        }

        @NonNull
        private static TimeInterval returnMain(int days) {
            switch (days) {
                case 1:
                    return TODAY;
                case 7:
                    return THIS_WEEK;
                case 30:
                    return THIS_MONTH;
                default:
                    return THIS_PERIOD;
            }
        }

        @NonNull
        private static TimeInterval fromDays(int days, @NonNull SummaryContext ctx) {
            switch (ctx) {
                default:
                    throw new IllegalArgumentException(ctx.toString());
                case MAIN:
                    return returnMain(days);
                case DAILY_STATS:
                    if (days != 1) throw new IllegalStateException();
                    return DATE_ONLY;
                case CUSTOM_RANGE:
                case PROJECTS:
                    if (days == 1) return DATE_ONLY;
                    else return DATE_RANGE_ONLY;
            }
        }

        @NonNull
        public String format(@NonNull GlobalSummary summary) {
            if (summary.days == 1) return format(summary.start);
            else return format(summary.start, summary.end);
        }

        @NonNull
        public String format(long date) {
            return String.format(Locale.getDefault(), str, Utils.getOnlyDateFormatter().format(date));
        }

        @NonNull
        public String format(long from, long to) {
            SimpleDateFormat sdf = Utils.getOnlyDateFormatter();
            return String.format(Locale.getDefault(), str, sdf.format(from), sdf.format(to));
        }
    }
}
