package com.gianlu.timeless.Charting;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;

import com.gianlu.timeless.Models.Project;
import com.gianlu.timeless.ThisApplication;
import com.gianlu.timeless.Utils;
import com.google.android.gms.analytics.HitBuilders;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

class SaveChartUtils {

    @NonNull
    static File save(Context context, View chart, @Nullable Project project, String name) throws IOException {
        File dest = new File(Utils.getImageDirectory(project), name + ".png");
        try (OutputStream out = new FileOutputStream(dest)) {
            Bitmap bitmap = Utils.createBitmap(chart);

            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
            out.flush();
        }

        ThisApplication.sendAnalytics(context, new HitBuilders.EventBuilder()
                .setCategory(ThisApplication.CATEGORY_USER_INPUT)
                .setAction(ThisApplication.ACTION_SAVED_CHART)
                .build());

        return dest;
    }
}
