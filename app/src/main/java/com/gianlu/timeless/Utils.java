package com.gianlu.timeless;

import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import androidx.annotation.NonNull;

public class Utils {
    public static final String ACTION_DATE_RANGE = "changed_date_range";
    public static final String ACTION_SAVED_CHART = "saved_chart_as_image";
    public static final String ACTION_CHANGE_SELECTED_BRANCHES = "changed_selected_branches";
    public static final String ACTION_FILTER_LEADERS = "filtered_leaderboards";
    public static final String ACTION_SHOW_ME_LEADER = "show_me_in_leaderboards";

    @NonNull
    public static SimpleDateFormat getDateTimeFormatter() {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss dd/MM/yyyy", Locale.getDefault());
        sdf.setTimeZone(TimeZone.getDefault());
        return sdf;
    }

    @NonNull
    public static SimpleDateFormat getOnlyDateFormatter() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        sdf.setTimeZone(TimeZone.getDefault());
        return sdf;
    }

    @NonNull
    public static SimpleDateFormat getISOParser() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault());
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        return sdf;
    }

    @NonNull
    public static String timeFormatterHours(long sec, boolean seconds) {
        long hours = TimeUnit.SECONDS.toHours(sec);
        long minute = TimeUnit.SECONDS.toMinutes(sec) - TimeUnit.HOURS.toMinutes(TimeUnit.SECONDS.toHours(sec));
        long second = TimeUnit.SECONDS.toSeconds(sec) - TimeUnit.MINUTES.toSeconds(TimeUnit.SECONDS.toMinutes(sec));

        if (hours > 0) {
            if (seconds)
                return String.format(Locale.getDefault(), "%02d", hours) + "h " + String.format(Locale.getDefault(), "%02d", minute) + "m " + String.format(Locale.getDefault(), "%02d", second) + "s";
            else
                return String.format(Locale.getDefault(), "%02d", hours) + "h " + String.format(Locale.getDefault(), "%02d", minute) + "m";
        } else {
            if (minute > 0) {
                if (seconds)
                    return String.format(Locale.getDefault(), "%02d", minute) + "m " + String.format(Locale.getDefault(), "%02d", second) + "s";
                else
                    return String.format(Locale.getDefault(), "%02d", minute) + "m";
            } else {
                if (second > 0) {
                    return String.format(Locale.getDefault(), "%02d", second) + "s";
                } else {
                    return "0s";
                }
            }
        }
    }

    @NonNull
    public static SimpleDateFormat getVerbalDateFormatter() {
        SimpleDateFormat sdf = new SimpleDateFormat("EEE, dd/MM/yyyy", Locale.getDefault());
        sdf.setTimeZone(TimeZone.getDefault());
        return sdf;
    }
}
