package com.gianlu.timeless.Charting;

import android.content.Context;
import android.util.AttributeSet;

import com.github.mikephil.charting.charts.BubbleChart;

public class SquareBubbleChart extends BubbleChart {
    public SquareBubbleChart(Context context) {
        super(context);
    }

    public SquareBubbleChart(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SquareBubbleChart(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, (int) (widthMeasureSpec + getLegend().mNeededHeight));
    }
}
