package com.gianlu.timeless;

import android.content.Context;
import android.util.AttributeSet;

import com.github.mikephil.charting.charts.BarChart;

public class SquareBarChart extends BarChart {
    public SquareBarChart(Context context) {
        super(context);
    }

    public SquareBarChart(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SquareBarChart(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, (int) (widthMeasureSpec + getLegend().mNeededHeight));
    }
}
