package com.gianlu.timeless;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

import com.gianlu.timeless.Objects.User;

public class LetterIcon extends View {
    private final Rect lettersBounds = new Rect();
    private final Rect textBounds = new Rect();
    private final Paint shapePaint;
    private final Paint letterPaint;
    private String letters;

    public LetterIcon(Context context, AttributeSet attrs) {
        super(context, attrs);

        letterPaint = new Paint();
        letterPaint.setColor(ContextCompat.getColor(context, R.color.colorAccent));
        letterPaint.setAntiAlias(true);
        letterPaint.setTypeface(Typeface.createFromAsset(context.getAssets(), "fonts/Roboto-Light.ttf"));
        letterPaint.setTextSize(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 26, context.getResources().getDisplayMetrics()));

        shapePaint = new Paint();
        shapePaint.setAntiAlias(true);
        shapePaint.setColor(ContextCompat.getColor(context, R.color.colorPrimaryDark));
        shapePaint.setShadowLayer(4, 0, 4, ContextCompat.getColor(context, R.color.colorPrimary_shadow));
        setLayerType(LAYER_TYPE_SOFTWARE, shapePaint);

        if (isInEditMode())
            letters = "AA";
    }

    public void setUser(@NonNull User user) {
        letters = user.getInitials();
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (shapePaint == null || letterPaint == null) return;

        int viewWidthHalf = this.getMeasuredWidth() / 2;
        int viewHeightHalf = this.getMeasuredHeight() / 2;

        int radius;
        if (viewWidthHalf > viewHeightHalf)
            radius = viewHeightHalf - 4;
        else
            radius = viewWidthHalf - 4;

        canvas.drawCircle(viewWidthHalf, viewHeightHalf, radius, shapePaint);

        letterPaint.getTextBounds(letters, 0, letters.length(), lettersBounds);
        canvas.drawText(letters, viewWidthHalf - lettersBounds.exactCenterX(), viewHeightHalf - lettersBounds.exactCenterY() - textBounds.height() - 2, letterPaint);
    }
}
