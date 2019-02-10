package com.gianlu.timeless.Charting;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.view.View;

import com.github.mikephil.charting.charts.Chart;

import androidx.annotation.NonNull;

public abstract class AbsChartPrepare<C extends Chart> {
    protected final C chart;

    public AbsChartPrepare(@NonNull C chart) {
        this.chart = chart;
    }

    @NonNull
    public final Bitmap createBitmap(int width, int height) {
        chart.measure(View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(height, View.MeasureSpec.EXACTLY));
        Bitmap b = Bitmap.createBitmap(chart.getMeasuredWidth(), chart.getMeasuredHeight(), Bitmap.Config.ARGB_4444);
        Canvas c = new Canvas(b);

        chart.layout(0, 0, chart.getMeasuredWidth(), chart.getMeasuredHeight());
        chart.draw(c);

        return b;
    }

    public abstract void setup(@NonNull Context context, @NonNull Object data);
}
