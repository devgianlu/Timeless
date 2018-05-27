package com.gianlu.timeless;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v4.content.ContextCompat;
import android.view.View;

import com.gianlu.commonutils.FontsManager;
import com.gianlu.commonutils.Toaster;
import com.gianlu.timeless.Models.Project;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

public class Utils {
    public static final String ACTION_DATE_RANGE = "changed_date_range";
    public static final String ACTION_SAVED_CHART = "saved_chart_as_image";
    public static final String ACTION_CHANGE_SELECTED_BRANCHES = "changed_selected_branches";
    public static final String ACTION_FILTER_LEADERS = "filtered_leaderboards";
    public static final String ACTION_SHOW_ME_LEADER = "show_me_in_leaderboards";

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static File getImageDirectory(@Nullable Project project) {
        File dir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "Timeless");
        dir.mkdir();

        if (project != null) {
            File subDir = new File(dir, project.name.replaceAll("[^a-zA-Z0-9.\\-]", "_"));
            subDir.mkdir();
            return subDir;
        } else {
            return dir;
        }
    }

    @NonNull
    public static Bitmap createBitmap(@NonNull View view) {
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
        textPaint.setTypeface(FontsManager.get().get(view.getContext(), FontsManager.ROBOTO_LIGHT));

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

    @NonNull
    public static String getFileName(@NonNull Context context, @StringRes int title) {
        return context.getString(title) + " (" + new SimpleDateFormat("HH:mm:ss dd-MM-yyyy", Locale.getDefault()).format(new Date()) + ")";
    }

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

    @SuppressWarnings("WeakerAccess")
    public static class Messages {
        public static final Toaster.Message CANT_CHECK_GRANT = new Toaster.Message(R.string.failedCheckingPermissions, true);
        public static final Toaster.Message FAILED_LOADING = new Toaster.Message(R.string.failedLoading, true);
        public static final Toaster.Message CANT_REFRESH_TOKEN = new Toaster.Message(R.string.failedRefreshingToken, true);
        public static final Toaster.Message USER_NOT_FOUND = new Toaster.Message(R.string.userNotFound, false);
        public static final Toaster.Message FAILED_SAVING_CHART = new Toaster.Message(R.string.failedSavingImage, true);
        public static final Toaster.Message WRITE_DENIED = new Toaster.Message(R.string.noWritePermission, false);
        public static final Toaster.Message FUTURE_DATE = new Toaster.Message(R.string.cannotGoFuture, false);
        public static final Toaster.Message NO_BRANCHES_SELECTED = new Toaster.Message(R.string.noBranchesSelected, false);
    }
}
