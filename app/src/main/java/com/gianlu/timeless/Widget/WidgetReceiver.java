package com.gianlu.timeless.Widget;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.util.TypedValue;
import android.widget.RemoteViews;

import com.gianlu.commonutils.CommonUtils;
import com.gianlu.timeless.Charting.BarChartPrepare;
import com.gianlu.timeless.Charting.SquareBarChart;
import com.gianlu.timeless.Models.Summaries;
import com.gianlu.timeless.NetIO.WakaTime;
import com.gianlu.timeless.NetIO.WakaTimeException;
import com.gianlu.timeless.R;

import androidx.annotation.NonNull;

public class WidgetReceiver extends AppWidgetProvider {

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        new WakaTime.Builder(context)
                .alreadyAuthorized(new WakaTime.InitializationListener() {
                    @Override
                    public void onWakatimeInitialized(@NonNull WakaTime w) {
                        for (int id : appWidgetIds) {
                            w.getRangeSummary(WakaTime.Range.LAST_7_DAYS.getStartAndEnd(), new WakaTime.OnSummary() {
                                @Override
                                public void onSummary(@NonNull Summaries summaries) {
                                    Bundle options = appWidgetManager.getAppWidgetOptions(id);
                                    int widthPx = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                                            options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH),
                                            context.getResources().getDisplayMetrics());
                                    int heightPx = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                                            options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT),
                                            context.getResources().getDisplayMetrics());

                                    BarChartPrepare holder = new BarChartPrepare(new SquareBarChart(context));
                                    holder.setup(context, summaries);

                                    RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_layout);
                                    views.setImageViewBitmap(R.id.widget_image, realBitmap(context, holder, widthPx, heightPx));

                                    appWidgetManager.updateAppWidget(id, views);
                                }

                                @Override
                                public void onWakaTimeError(@NonNull WakaTimeException ex) {
                                    ex.printStackTrace();
                                }

                                @Override
                                public void onException(@NonNull Exception ex) {
                                    ex.printStackTrace();
                                }
                            });
                        }
                    }

                    @Override
                    public void onException(@NonNull Exception ex) {
                        ex.printStackTrace();
                    }
                });
    }

    private Bitmap realBitmap(Context context, BarChartPrepare prepare, int width, int height) {
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);

        int dp8 = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, context.getResources().getDisplayMetrics());
        Bitmap chart = prepare.createBitmap(width - dp8 * 2, height - dp8 * 2);

        Paint bgPaint = new Paint();
        bgPaint.setColor(CommonUtils.resolveAttrAsColor(context, android.R.attr.colorBackground));
        bgPaint.setAntiAlias(true);

        Paint titlePaint = new Paint();
        titlePaint.setColor(Color.RED);
        titlePaint.setTextSize(dp8 * 2);
        titlePaint.setAntiAlias(true);

        canvas.drawRoundRect(0, 0, width, height, dp8, dp8, bgPaint);
        canvas.drawBitmap(chart, dp8, dp8, null);

        return bitmap;
    }

    @Override
    public void onAppWidgetOptionsChanged(Context context, AppWidgetManager appWidgetManager, int appWidgetId, Bundle newOptions) {
        onUpdate(context, appWidgetManager, new int[]{appWidgetId});
    }
}
