package com.gianlu.timeless;

import android.content.Context;
import android.util.AttributeSet;

import com.github.mikephil.charting.charts.RadarChart;

public class SquareRadarChart extends RadarChart {
    public SquareRadarChart(Context context) {
        super(context);
    }

    public SquareRadarChart(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SquareRadarChart(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, (int) (widthMeasureSpec + getLegend().mNeededHeight));
    }
}
