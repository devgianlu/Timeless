package com.gianlu.timeless.Charting;

import android.content.Context;
import android.util.AttributeSet;

import com.github.mikephil.charting.charts.PieChart;

public class SquarePieChart extends PieChart {
    public SquarePieChart(Context context) {
        super(context);
    }

    public SquarePieChart(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SquarePieChart(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec,
                MeasureSpec.makeMeasureSpec((int) (MeasureSpec.getSize(widthMeasureSpec) + getLegend().mNeededHeight),
                        MeasureSpec.EXACTLY)); // FIXME
    }
}
