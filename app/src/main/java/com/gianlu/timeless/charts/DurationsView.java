package com.gianlu.timeless.charts;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.ColorInt;

import com.gianlu.commonutils.CommonUtils;
import com.gianlu.commonutils.typography.FontsManager;
import com.gianlu.timeless.R;
import com.gianlu.timeless.api.models.Duration;
import com.gianlu.timeless.api.models.Durations;
import com.gianlu.timeless.colors.ColorsMapper;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class DurationsView extends LinearLayout {
    private final Paint gridPaint;
    private final Paint titleTextPaint;
    private final Paint textPaint;
    private List<String> projects;

    public DurationsView(Context context) {
        this(context, null, 0);
    }

    public DurationsView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DurationsView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setOrientation(VERTICAL);

        int primaryText = CommonUtils.resolveAttrAsColor(context, android.R.attr.textColorPrimary);

        titleTextPaint = new Paint();
        titleTextPaint.setColor(primaryText);
        titleTextPaint.setAlpha(64);
        titleTextPaint.setAntiAlias(true);
        FontsManager.set(context, titleTextPaint, R.font.roboto_medium);
        titleTextPaint.setTextSize(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 40, context.getResources().getDisplayMetrics()));

        textPaint = new Paint();
        textPaint.setColor(primaryText);
        textPaint.setAntiAlias(true);
        textPaint.setTextSize(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 10, context.getResources().getDisplayMetrics()));

        gridPaint = new Paint();
        gridPaint.setColor(CommonUtils.resolveAttrAsColor(context, android.R.attr.textColorSecondary));
    }

    public void setDurations(Durations durations, ColorsMapper colors) {
        this.projects = new ArrayList<>();
        for (Duration duration : durations)
            if (!projects.contains(duration.project))
                projects.add(duration.project);

        removeAllViews();
        if (projects.isEmpty()) {
            nothingToShow();
        } else {
            for (int i = 0; i < projects.size(); i++) {
                String p = projects.get(i);
                addView(new ChartView(getContext(), p, durations.filter(p), colors.getColor(getContext(), p), projects.size() <= 1, durations.isToday()));
            }
        }
    }

    private void nothingToShow() {
        TextView noData = new TextView(getContext());
        noData.setText(R.string.noData);
        noData.setTextColor(Color.rgb(247, 189, 51));
        noData.setTextAlignment(TEXT_ALIGNMENT_CENTER);
        addView(noData);
    }

    @Override
    public void removeView(View view) {
        super.removeView(view);

        if (getChildCount() == 0) {
            removeAllViews();
            nothingToShow();
        }
    }

    private class ChartView extends View {
        private final boolean lonely;
        private final String project;
        private final long[] data;
        private final Rect textBounds = new Rect();
        private final Rect titleTextBounds = new Rect();
        private final Paint durationPaint;
        private final Paint hiddenSpacePaint;
        private final int mPadding;
        private final int mHeight;
        private final float hiddenSince;
        private float mInternalPadding;

        public ChartView(Context context, String project, List<Duration> durations, @ColorInt int color, boolean lonely, boolean isToday) {
            super(context);
            mHeight = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 100, context.getResources().getDisplayMetrics());

            durationPaint = new Paint();
            durationPaint.setColor(color);

            hiddenSpacePaint = new Paint();
            hiddenSpacePaint.setColor(color);
            hiddenSpacePaint.setAlpha(32);

            this.project = project;
            this.lonely = lonely;

            this.data = new long[durations.size() * 2];
            Calendar cal = Calendar.getInstance();
            for (int i = 0; i < durations.size(); i++) {
                Duration duration = durations.get(i);

                cal.setTimeInMillis(duration.time * 1000);
                cal.set(Calendar.HOUR_OF_DAY, 0);
                cal.set(Calendar.MINUTE, 0);
                cal.set(Calendar.SECOND, 0);
                cal.set(Calendar.MILLISECOND, 0);

                data[i * 2] = TimeUnit.MILLISECONDS.toSeconds((duration.time * 1000) - cal.getTimeInMillis());
                data[i * 2 + 1] = duration.duration;

                if (!projects.contains(duration.project))
                    projects.add(duration.project);
            }

            if (lonely)
                mPadding = 0;
            else
                mPadding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, context.getResources().getDisplayMetrics());

            hiddenSince = isToday ? (System.currentTimeMillis() - cal.getTimeInMillis()) / 1000f : -1;
        }

        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            super.onMeasure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(mHeight, MeasureSpec.EXACTLY));
        }

        private void calcInternalPadding() {
            textPaint.getTextBounds("24", 0, 2, textBounds);
            mInternalPadding = textBounds.width() / 2f;
        }

        private void adjustTextSize(Canvas canvas) {
            calcInternalPadding();
            float secPerPixel = ((float) canvas.getWidth() - (mInternalPadding * 2)) / 86400f;
            float destWidth = 3600 * secPerPixel;

            for (int i = 0; i <= 24; i++) {
                String hour = String.valueOf(i);
                textPaint.getTextBounds(hour, 0, hour.length(), textBounds);

                if (textBounds.width() >= destWidth) {
                    textPaint.setTextSize(textPaint.getTextSize() - 1);
                    adjustTextSize(canvas);
                    break;
                }
            }
        }

        private void adjustTitleTextSize(Canvas canvas) {
            calcInternalPadding();
            float destWidth = canvas.getWidth() - mInternalPadding * 2;
            titleTextPaint.getTextBounds(project, 0, project.length(), titleTextBounds);
            if (titleTextBounds.width() >= destWidth) {
                titleTextPaint.setTextSize(titleTextPaint.getTextSize() - 1);
                adjustTitleTextSize(canvas);
            }
        }

        @Override
        protected void onDraw(Canvas canvas) {
            calcInternalPadding();
            float secPerPixel = ((float) getWidth() - (mInternalPadding * 2)) / 86400f;
            adjustTextSize(canvas);

            float bottomPadding = getHeight() - textBounds.height() - (lonely ? 10 : 5) - mPadding;

            for (int i = 0; i <= 24; i++) {
                String hour = String.valueOf(i);
                float pos = (i * 3600 * secPerPixel) + mInternalPadding;

                textPaint.getTextBounds(hour, 0, hour.length(), textBounds);
                canvas.drawLine(pos, mPadding, pos, bottomPadding, gridPaint);
                if (i % 2 == 0)
                    canvas.drawText(hour, pos - (textBounds.width() / 2f), getHeight() - mPadding, textPaint);
            }

            if (hiddenSince != -1)
                canvas.drawRect(mInternalPadding, mPadding, secPerPixel * hiddenSince + mInternalPadding, bottomPadding, hiddenSpacePaint);
            else
                canvas.drawRect(mInternalPadding, mPadding, 24 * 3600 * secPerPixel + mInternalPadding, bottomPadding, hiddenSpacePaint);

            adjustTitleTextSize(canvas);
            titleTextPaint.getTextBounds(project, 0, project.length(), titleTextBounds);
            canvas.drawText(project, (getWidth() - titleTextBounds.width()) / 2f, ((getHeight() + titleTextBounds.height()) / 2f) - textBounds.height() - 5, titleTextPaint);

            boolean drawn = false;
            if (data.length != 0) {
                for (int i = 0; i < data.length / 2; i++) {
                    long key = data[i * 2];
                    long val = data[i * 2 + 1];

                    if (val * secPerPixel > 1) {
                        canvas.drawRect((key * secPerPixel) + mInternalPadding, mPadding, ((key + val) * secPerPixel) + mInternalPadding, bottomPadding, durationPaint);
                        if (!drawn) drawn = true;
                    }
                }
            }

            if (!drawn) {
                post(() -> DurationsView.this.removeView(ChartView.this));
            }
        }
    }
}