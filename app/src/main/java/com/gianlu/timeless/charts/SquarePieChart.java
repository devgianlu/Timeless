package com.gianlu.timeless.charts;

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
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int width = MeasureSpec.getSize(widthMeasureSpec);
        setMeasuredDimension(MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec((int) (width + getLegend().mNeededHeight / 1.5f - getLegend().getYOffset()), MeasureSpec.EXACTLY));
    }
}
