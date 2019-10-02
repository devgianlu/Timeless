package com.gianlu.timeless.charts;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Environment;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.core.content.ContextCompat;

import com.gianlu.commonutils.logging.Logging;
import com.gianlu.commonutils.typography.FontsManager;
import com.gianlu.timeless.R;
import com.gianlu.timeless.ThisApplication;
import com.gianlu.timeless.Utils;
import com.gianlu.timeless.api.models.Project;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

final class SaveChartUtils {

    private SaveChartUtils() {
    }

    @Nullable
    static File save(View view, @StringRes int title, @Nullable Project project) {
        String name = view.getContext().getString(title) + " (" + new SimpleDateFormat("HH:mm:ss dd-MM-yyyy", Locale.getDefault()).format(new Date()) + ")";

        try {
            return save(view, project, name);
        } catch (IOException ex) {
            Logging.log(ex);
            return null;
        }
    }

    @NonNull
    private static File save(View chart, @Nullable Project project, String name) throws IOException {
        File dest = new File(getImageDirectory(project), name + ".png");
        try (OutputStream out = new FileOutputStream(dest)) {
            Bitmap bitmap = createBitmap(chart);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
        }

        ThisApplication.sendAnalytics(Utils.ACTION_SAVED_CHART);
        return dest;
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private static File getImageDirectory(@Nullable Project project) {
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
    private static Bitmap createBitmap(@NonNull View view) {
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
        FontsManager.set(view.getContext(), textPaint, FontsManager.ROBOTO_LIGHT);

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
        canvas.drawText(text, (canvas.getWidth() - textBounds.width()) / 2f, view.getHeight() + ((canvas.getHeight() - view.getHeight() - 10 + textBounds.height()) / 2f) + 6, textPaint);

        return bitmap;
    }
}
