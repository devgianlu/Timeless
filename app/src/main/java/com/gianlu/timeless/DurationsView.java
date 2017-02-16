package com.gianlu.timeless;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.widget.LinearLayout;

import com.gianlu.timeless.Objects.Duration;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class DurationsView extends LinearLayout {
    private List<String> projects;

    public DurationsView(Context context) {
        super(context);
        setOrientation(VERTICAL);
    }

    public DurationsView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setOrientation(VERTICAL);
    }

    public DurationsView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setOrientation(VERTICAL);
    }

    public void setDurations(List<Duration> durations) {
        this.projects = new ArrayList<>();
        for (Duration duration : durations)
            if (!projects.contains(duration.project))
                projects.add(duration.project);

        for (String project : projects)
            addView(new ChartView(getContext(), project, Duration.filter(durations, project), projects.size() <= 1));

        if (projects.isEmpty())
            addView(new ChartView(getContext(), "", Collections.<Duration>emptyList(), true));
    }

    private int countVisibleChildren() {
        int count = 0;
        for (int i = 0; i < getChildCount(); i++)
            if (getChildAt(i).getVisibility() == VISIBLE)
                count++;

        return count;
    }

    private class ChartView extends View {
        private final boolean lonely;
        private final String project;
        private final Map<Long, Long> values;
        private final Rect textBounds = new Rect();
        private final Rect titleTextBounds = new Rect();
        private final Paint titleTextPaint;
        private final Paint durationPaint;
        private final Paint gridPaint;
        private final Paint textPaint;
        private final Paint noDataPaint;
        private final Rect noDataBounds = new Rect();
        private final int padding;
        private final int defaultHeight;
        private float internalPadding;

        @SuppressLint("UseSparseArrays")
        public ChartView(Context context, String project, List<Duration> durations, boolean lonely) {
            super(context);
            durationPaint = new Paint();
            durationPaint.setColor(ContextCompat.getColor(context, R.color.colorAccent));

            gridPaint = new Paint();
            gridPaint.setColor(Color.GRAY);

            textPaint = new Paint();
            textPaint.setColor(Color.BLACK);
            textPaint.setAntiAlias(true);
            textPaint.setTextSize(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 10, context.getResources().getDisplayMetrics()));

            noDataPaint = new Paint();
            noDataPaint.setColor(Color.rgb(247, 189, 51));
            noDataPaint.setAntiAlias(true);
            noDataPaint.setTextSize(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 10, context.getResources().getDisplayMetrics()));

            titleTextPaint = new Paint();
            titleTextPaint.setColor(Color.BLACK);
            titleTextPaint.setAlpha(64);
            titleTextPaint.setAntiAlias(true);
            titleTextPaint.setTypeface(Typeface.createFromAsset(context.getAssets(), "fonts/Roboto-Medium.ttf"));
            titleTextPaint.setTextSize(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 40, context.getResources().getDisplayMetrics()));

            this.project = project;
            this.lonely = lonely;
            this.values = new HashMap<>();
            defaultHeight = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 100, context.getResources().getDisplayMetrics());
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

            if (lonely)
                padding = 0;
            else
                padding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, context.getResources().getDisplayMetrics());
        }

        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            super.onMeasure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(defaultHeight, MeasureSpec.EXACTLY));
        }

        private void calcInternalPadding() {
            textPaint.getTextBounds("24", 0, 2, textBounds);
            internalPadding = textBounds.width() / 2;
        }

        private void adjustTextSize(Canvas canvas) {
            calcInternalPadding();
            float secPerPixel = ((float) canvas.getWidth() - (internalPadding * 2)) / 86400f;

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

        private void adjustTitleTextSize(Canvas canvas) {
            calcInternalPadding();
            titleTextPaint.getTextBounds(project, 0, project.length(), titleTextBounds);
            if (titleTextBounds.width() >= canvas.getWidth() - internalPadding * 2) {
                titleTextPaint.setTextSize(titleTextPaint.getTextSize() - 1);
                adjustTitleTextSize(canvas);
            }
        }

        @Override
        protected void onDraw(Canvas canvas) {
            calcInternalPadding();
            float secPerPixel = ((float) canvas.getWidth() - (internalPadding * 2)) / 86400f;
            adjustTextSize(canvas);

            if (!lonely) {
                adjustTitleTextSize(canvas);
                titleTextPaint.getTextBounds(project, 0, project.length(), titleTextBounds);
                canvas.drawText(project, (canvas.getWidth() - titleTextBounds.width()) / 2, (canvas.getHeight() + titleTextBounds.height()) / 2 - textBounds.height() - 5, titleTextPaint);
            }

            for (int i = 0; i <= 24; i++) {
                String hour = String.valueOf(i);
                float pos = (i * 3600 * secPerPixel) + internalPadding;

                textPaint.getTextBounds(hour, 0, hour.length(), textBounds);
                canvas.drawLine(pos, padding, pos, canvas.getHeight() - textBounds.height() - (lonely ? 10 : 5) - padding, gridPaint);
                if (i % 2 == 0)
                    canvas.drawText(hour, pos - (textBounds.width() / 2), canvas.getHeight() - padding, textPaint);
            }

            boolean drawn = false;
            if (values != null && !values.isEmpty()) {
                for (Map.Entry<Long, Long> entry : values.entrySet()) {
                    if (entry.getValue() * secPerPixel >= 1) {
                        canvas.drawRect((entry.getKey() * secPerPixel) + internalPadding, padding, ((entry.getKey() + entry.getValue()) * secPerPixel) + internalPadding, canvas.getHeight() - textBounds.height() - (lonely ? 10 : 5) - padding, durationPaint);
                        if (!drawn)
                            drawn = true;
                    }
                }
            }

            if (!drawn) {
                canvas.drawColor(Color.WHITE);

                if (lonely || DurationsView.this.countVisibleChildren() == 1) {
                    String noData = getContext().getString(R.string.noData);
                    noDataPaint.getTextBounds(noData, 0, noData.length(), noDataBounds);
                    canvas.drawText(noData, (canvas.getWidth() - noDataBounds.width()) / 2, (canvas.getHeight() + noDataBounds.height()) / 2, noDataPaint);
                } else {
                    setVisibility(GONE);
                }
            }
        }
    }
}