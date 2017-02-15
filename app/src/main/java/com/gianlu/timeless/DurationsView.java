package com.gianlu.timeless;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

import com.gianlu.timeless.Objects.Duration;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class DurationsView extends View {
    private Map<Long, Long> values;
    private Rect textBounds = new Rect();
    private Paint durationPaint;
    private Paint gridPaint;
    private Paint textPaint;
    private float padding;
    private List<String> projects;

    public DurationsView(Context context) {
        super(context);
        init(context);
    }

    public DurationsView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public DurationsView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    public DurationsView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context);
    }

    private void init(Context context) {
        durationPaint = new Paint();
        durationPaint.setColor(ContextCompat.getColor(context, R.color.colorAccent));
        durationPaint.setAlpha(100);

        gridPaint = new Paint();
        gridPaint.setColor(Color.GRAY);

        textPaint = new Paint();
        textPaint.setColor(Color.BLACK);
        textPaint.setAntiAlias(true);
        textPaint.setTextSize(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 10, context.getResources().getDisplayMetrics()));
    }

    @SuppressLint("UseSparseArrays")
    public void setDurations(List<Duration> durations) {
        values = new HashMap<>();
        projects = new ArrayList<>();
        Calendar cal = Calendar.getInstance();

        for (Duration duration : durations) {
            cal.setTimeInMillis(duration.time * 1000);
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);

            values.put(TimeUnit.MILLISECONDS.toSeconds((duration.time * 1000) - cal.getTimeInMillis()), duration.duration);

            if (!projects.contains(duration.project))
                projects.add(duration.project);
        }

        invalidate();
    }

    private void calcPadding() {
        textPaint.getTextBounds("24", 0, 2, textBounds);
        padding = textBounds.width() / 2;
    }

    private void adjustTextSize(Canvas canvas) {
        calcPadding();
        float secPerPixel = ((float) canvas.getWidth() - (padding * 2)) / 86400f;

        for (int i = 0; i <= 24; i++) {
            String hour = String.valueOf(i);
            textPaint.getTextBounds(hour, 0, hour.length(), textBounds);

            if (textBounds.width() >= 3600 * secPerPixel) {
                textPaint.setTextSize(textPaint.getTextSize() - 1);
                adjustTextSize(canvas);
                break;
            }
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        calcPadding();
        float secPerPixel = ((float) canvas.getWidth() - (padding * 2)) / 86400f;
        adjustTextSize(canvas);

        for (int i = 0; i <= 24; i++) {
            String hour = String.valueOf(i);
            float pos = (i * 3600 * secPerPixel) + padding;

            textPaint.getTextBounds(hour, 0, hour.length(), textBounds);
            canvas.drawLine(pos, 0, pos, canvas.getHeight() - textBounds.height() - 5, gridPaint);
            if (i % 2 == 0)
                canvas.drawText(hour, pos - (textBounds.width() / 2), canvas.getHeight(), textPaint);
        }

        if (values != null)
            for (Map.Entry<Long, Long> entry : values.entrySet())
                canvas.drawRect((entry.getKey() * secPerPixel) + padding, 0, ((entry.getKey() + entry.getValue()) * secPerPixel) + padding, canvas.getHeight() - textBounds.height() - 5, durationPaint);
    }
}