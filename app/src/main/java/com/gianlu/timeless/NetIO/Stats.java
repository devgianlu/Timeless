package com.gianlu.timeless.NetIO;

import android.content.Context;

import com.gianlu.timeless.R;

import org.json.JSONObject;

public class Stats {

    public Stats(JSONObject obj) {
    }

    public enum Range {
        LAST_7_DAYS,
        LAST_30_DAYS,
        LAST_6_MONTHS,
        LAST_YEAR;

        public String toValidFormat() {
            switch (this) {
                default:
                case LAST_7_DAYS:
                    return "last_7_days";
                case LAST_30_DAYS:
                    return "last_30_days";
                case LAST_6_MONTHS:
                    return "last_6_months";
                case LAST_YEAR:
                    return "last_year";
            }
        }

        public String getFormal(Context context) {
            switch (this) {
                default:
                case LAST_7_DAYS:
                    return context.getString(R.string.last_7_days);
                case LAST_30_DAYS:
                    return context.getString(R.string.last_30_days);
                case LAST_6_MONTHS:
                    return context.getString(R.string.last_6_months);
                case LAST_YEAR:
                    return context.getString(R.string.last_year);
            }
        }
    }
}
