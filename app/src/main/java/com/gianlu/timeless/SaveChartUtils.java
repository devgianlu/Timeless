package com.gianlu.timeless;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.net.Uri;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.core.content.FileProvider;

import com.gianlu.commonutils.CommonUtils;
import com.gianlu.timeless.api.models.Project;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public final class SaveChartUtils {
    private static final String TAG = SaveChartUtils.class.getSimpleName();

    private SaveChartUtils() {
    }

    public static void share(@NonNull View view, @StringRes int title, @Nullable Project project) {
        String name = view.getContext().getString(title);
        if (project != null) name += " - " + project;
        name += " (" + new SimpleDateFormat("HH:mm:ss dd-MM-yyyy", Locale.getDefault()).format(new Date()) + ")";

        try {
            save(view, name);
        } catch (IOException ex) {
            Log.e(TAG, "Failed saving chart.", ex);
        }
    }

    private static void save(@NonNull View chart, @NonNull String name) throws IOException {
        Context context = chart.getContext();

        File dest = new File(getChartsDirectory(context), name + ".png");
        try (OutputStream out = new FileOutputStream(dest)) {
            Bitmap bitmap = createBitmap(chart);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
        }

        Uri uri = FileProvider.getUriForFile(context, context.getApplicationContext().getPackageName() + ".charts", dest);

        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
        shareIntent.setType("image/png");
        context.startActivity(Intent.createChooser(shareIntent, context.getText(R.string.shareChartImage)));
        ThisApplication.sendAnalytics(Utils.ACTION_SAVED_CHART);
    }

    @NonNull
    private static File getChartsDirectory(@NonNull Context context) throws IOException {
        File file = new File(context.getCacheDir(), "charts");
        if (!file.exists() && !file.mkdir()) throw new IOException("Failed creating subdirectory.");
        return file;
    }

    @NonNull
    private static Bitmap createBitmap(@NonNull View view) {
        Bitmap chartBitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas chartCanvas = new Canvas(chartBitmap);
        chartCanvas.drawColor(CommonUtils.resolveAttrAsColor(view.getContext(), R.attr.colorSurface));
        view.draw(chartCanvas);

        Bitmap bitmap = Bitmap.createBitmap(view.getWidth() + 20, view.getHeight() + 20, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        canvas.drawColor(CommonUtils.resolveAttrAsColor(view.getContext(), R.attr.colorSurface));
        canvas.drawBitmap(chartBitmap, 10, 10, null);
        return bitmap;
    }
}
