package com.gianlu.timeless;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.TextView;

import com.gianlu.commonutils.CommonUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
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
        while (i >= COLORS.length)
            i = i - COLORS.length;

        return COLORS[i];
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static File getImageDirectory(@Nullable String project) {
        File dir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "Timeless");
        dir.mkdir();

        if (project != null) {
            File subDir = new File(dir, project.replaceAll("[^a-zA-Z0-9\\.\\-]", "_"));
            subDir.mkdir();
            return subDir;
        } else {
            return dir;
        }
    }

    public static Bitmap createBitmap(View view) {
        Bitmap chartBitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas chartCanvas = new Canvas(chartBitmap);
        chartCanvas.drawColor(Color.WHITE);
        view.draw(chartCanvas);

        Bitmap bitmap = Bitmap.createBitmap(view.getWidth() + 20, view.getHeight() + 65, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        canvas.drawColor(Color.WHITE);
        canvas.drawBitmap(chartBitmap, 10, 10, null);

        Paint textPaint = new Paint();
        textPaint.setColor(Color.WHITE);
        textPaint.setAntiAlias(true);
        textPaint.setTextSize(38);
        textPaint.setTypeface(Typeface.createFromAsset(view.getContext().getAssets(), "fonts/Roboto-Light.ttf"));

        String text = view.getContext().getString(R.string.watermark);
        Rect textBounds = new Rect();
        boolean ok = false;
        while (!ok) {
            textPaint.getTextBounds(text, 0, text.length(), textBounds);
            if (textBounds.width() >= canvas.getWidth() - 24)
                textPaint.setTextSize(textPaint.getTextSize() - 1);
            else
                ok = true;
        }

        Paint rectPaint = new Paint();
        rectPaint.setColor(ContextCompat.getColor(view.getContext(), R.color.colorPrimaryDark));

        canvas.drawRect(0, view.getHeight() + 10, canvas.getWidth(), canvas.getHeight(), rectPaint);
        canvas.drawText(text, (canvas.getWidth() - textBounds.width()) / 2, view.getHeight() + ((canvas.getHeight() - view.getHeight() - 10 + textBounds.height()) / 2) + 6, textPaint);

        return bitmap;
    }

    public static String getFileName(String title) {
        return title + " (" + new SimpleDateFormat("HH:mm:ss dd-MM-yyyy", Locale.getDefault()).format(new Date()) + ")";
    }

    public static ColorStateList getTextViewDefaultColor(Context context) {
        if (textViewDefaultColor == null)
            textViewDefaultColor = new TextView(context).getTextColors();

        return textViewDefaultColor;
    }

    public static SimpleDateFormat getDateTimeFormatter() {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss dd/MM/yyyy", Locale.getDefault());
        sdf.setTimeZone(TimeZone.getDefault());
        return sdf;
    }

    public static SimpleDateFormat getOnlyDateFormatter() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
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
        String value = obj.optString(name);

        if (Objects.equals(value, "null")) return null;
        else return value;
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
        public static final CommonUtils.ToastMessage USER_NOT_FOUND = new CommonUtils.ToastMessage("You have not been found. Keep scrolling down.", false);
        public static final CommonUtils.ToastMessage INVALID_TOKEN = new CommonUtils.ToastMessage("Please grant access again.", false);
        public static final CommonUtils.ToastMessage FAILED_SAVING_CHART = new CommonUtils.ToastMessage("Failed saving the chart image!", true);
        public static final CommonUtils.ToastMessage WRITE_DENIED = new CommonUtils.ToastMessage("You have denied the write permission.", false);
        public static final CommonUtils.ToastMessage FUTURE_DATE = new CommonUtils.ToastMessage("You cannot go into the future!", false);
    }
}
