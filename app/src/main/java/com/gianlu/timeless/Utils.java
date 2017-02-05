package com.gianlu.timeless;

import android.content.Context;
import android.content.res.ColorStateList;
import android.support.annotation.Nullable;
import android.widget.TextView;

import com.gianlu.commonutils.CommonUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Objects;
import java.util.Random;
import java.util.TimeZone;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

public class Utils {
    private static final int[] COLORS = new int[]{R.color.red, R.color.pink, R.color.purple, R.color.deepPurple, R.color.indigo, R.color.blue, R.color.lightBlue, R.color.cyan, R.color.teal, R.color.green, R.color.lightGreen, R.color.lime, R.color.yellow, R.color.amber, R.color.orange, R.color.deepOrange, R.color.brown};
    private static ColorStateList textViewDefaultColor;

    public static int getColor(int pos) {
        int i = pos;
        while (i > COLORS.length)
            i = i - COLORS.length;

        return COLORS[i];
    }

    public static ColorStateList getTextViewDefaultColor(Context context) {
        if (textViewDefaultColor == null)
            textViewDefaultColor = new TextView(context).getTextColors();

        return textViewDefaultColor;
    }

    public static SimpleDateFormat getDateFormatter() {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss dd/MM/yyyy", Locale.getDefault());
        sdf.setTimeZone(TimeZone.getDefault());
        return sdf;
    }

    public static SimpleDateFormat getISOParser() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault());
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        return sdf;
    }

    @Nullable
    public static String parseStupidNullJSON(JSONObject obj, String name) throws JSONException {
        String value = obj.getString(name);

        if (Objects.equals(value, "null"))
            return null;
        else
            return value;
    }

    public static int[] getColors() {
        shuffleArray(COLORS);
        return COLORS;
    }

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

    private static void shuffleArray(int[] ar) {
        Random rnd = ThreadLocalRandom.current();
        for (int i = ar.length - 1; i > 0; i--) {
            int index = rnd.nextInt(i + 1);
            int a = ar[index];
            ar[index] = ar[i];
            ar[i] = a;
        }
    }

    @SuppressWarnings("WeakerAccess")
    public static class ToastMessages {
        public static final CommonUtils.ToastMessage CANT_CHECK_GRANT = new CommonUtils.ToastMessage("Failed checking permissions!", true);
        public static final CommonUtils.ToastMessage FAILED_REFRESHING = new CommonUtils.ToastMessage("Failed refreshing data.", true);
        public static final CommonUtils.ToastMessage FAILED_LOADING = new CommonUtils.ToastMessage("Failed loading data.", true);
        public static final CommonUtils.ToastMessage TOKEN_REJECTED = new CommonUtils.ToastMessage("Server rejected your request. Try again or contact me!", false);
        public static final CommonUtils.ToastMessage CANT_REFRESH_TOKEN = new CommonUtils.ToastMessage("Failed refreshing the token!", true);
        public static final CommonUtils.ToastMessage USER_NOT_FOUND = new CommonUtils.ToastMessage("You have not been found.", false);
        public static final CommonUtils.ToastMessage OFFLINE = new CommonUtils.ToastMessage("You're offline!", false);
    }
}
