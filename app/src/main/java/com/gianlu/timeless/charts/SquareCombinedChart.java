package com.gianlu.timeless.charts;

import android.content.Context;
import android.util.AttributeSet;

import com.github.mikephil.charting.charts.CombinedChart;

public class SquareCombinedChart extends CombinedChart {
    public SquareCombinedChart(Context context) {
        super(context);
    }

    public SquareCombinedChart(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SquareCombinedChart(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, (int) (widthMeasureSpec + getLegend().mNeededHeight - getLegend().getYOffset()));
    }
}
