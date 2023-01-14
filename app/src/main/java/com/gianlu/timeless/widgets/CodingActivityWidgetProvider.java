package com.gianlu.timeless.widgets;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.gianlu.commonutils.preferences.json.JsonStoring;
import com.gianlu.timeless.LoadingActivity;
import com.gianlu.timeless.PK;
import com.gianlu.timeless.R;
import com.gianlu.timeless.Utils;
import com.gianlu.timeless.api.WakaTime;
import com.gianlu.timeless.api.WakaTimeException;
import com.gianlu.timeless.api.models.Summaries;

import org.json.JSONException;
import org.json.JSONObject;

public class CodingActivityWidgetProvider extends AppWidgetProvider {
    private static final String TAG = CodingActivityWidgetProvider.class.getSimpleName();

    @NonNull
    private static PendingIntent startAppPendingIntent(@NonNull Context context) {
        int flags = 0;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) flags = PendingIntent.FLAG_IMMUTABLE;
        return PendingIntent.getActivity(context, 0, new Intent(context, LoadingActivity.class), flags);
    }

    @NonNull
    private static RemoteViews createRemoteViews(@NonNull Context context, @NonNull WakaTime.Range range, long time) {
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_coding_activity);
        views.setViewVisibility(R.id.codingActivityWidget_time, View.VISIBLE);
        views.setViewVisibility(R.id.codingActivityWidget_range, View.VISIBLE);
        views.setViewVisibility(R.id.codingActivityWidget_error, View.GONE);

        views.setTextViewText(R.id.codingActivityWidget_range, range.getFormal(context));
        views.setTextViewText(R.id.codingActivityWidget_time, Utils.timeFormatterHours(time, false));
        views.setOnClickPendingIntent(R.id.codingActivityWidget, startAppPendingIntent(context));
        return views;
    }

    @NonNull
    private static RemoteViews createRemoteViewsLoading(@NonNull Context context) {
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_coding_activity);
        views.setViewVisibility(R.id.codingActivityWidget_time, View.GONE);
        views.setViewVisibility(R.id.codingActivityWidget_range, View.GONE);
        views.setViewVisibility(R.id.codingActivityWidget_error, View.GONE);
        views.setOnClickPendingIntent(R.id.codingActivityWidget, startAppPendingIntent(context));
        return views;
    }

    @NonNull
    private static RemoteViews createRemoteViewsError(@NonNull Context context, @NonNull String errorStr) {
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_coding_activity);
        views.setViewVisibility(R.id.codingActivityWidget_time, View.GONE);
        views.setViewVisibility(R.id.codingActivityWidget_range, View.GONE);
        views.setViewVisibility(R.id.codingActivityWidget_error, View.VISIBLE);

        views.setTextViewText(R.id.codingActivityWidget_error, errorStr);
        views.setOnClickPendingIntent(R.id.codingActivityWidget, startAppPendingIntent(context));
        return views;
    }

    public static void performWidgetUpdate(@NonNull WakaTime wakaTime, @NonNull Context context, int appWidgetId, @Nullable Runnable completionCallback) {
        AppWidgetManager manager = AppWidgetManager.getInstance(context);
        manager.updateAppWidget(appWidgetId, createRemoteViewsLoading(context));

        Log.v(TAG, "Refreshing widget: " + appWidgetId);
        WidgetOptions options = loadWidgetOptions(appWidgetId);
        wakaTime.skipNextRequestCache();
        wakaTime.getRangeSummary(options.range.getStartAndEnd(), null, new WakaTime.OnSummary() {
            @Override
            public void onSummary(@NonNull Summaries summaries) {
                performWidgetUpdate(context, appWidgetId, options, summaries);
                if (completionCallback != null) completionCallback.run();
            }

            @Override
            public void onWakaTimeError(@NonNull WakaTimeException ex) {
                manager.updateAppWidget(appWidgetId, createRemoteViewsError(context, ex.getMessage()));
                if (completionCallback != null) completionCallback.run();
            }

            @Override
            public void onException(@NonNull Exception ex) {
                Log.e(TAG, "Failed retrieving summary for widget update.", ex);

                manager.updateAppWidget(appWidgetId, createRemoteViewsError(context, context.getString(R.string.failedLoading)));
                if (completionCallback != null) completionCallback.run();
            }
        });
    }

    private static void performWidgetUpdate(@NonNull Context context, int appWidgetId, @NonNull WidgetOptions options, @NonNull Summaries summaries) {
        long total = summaries.globalSummary.total_seconds;
        AppWidgetManager.getInstance(context).updateAppWidget(appWidgetId, createRemoteViews(context, options.range, total));
        Log.d(TAG, "Widget updated: " + appWidgetId);
    }

    /**
     * Updates the widget after making sure that the range is correct.
     *
     * @param context     The context
     * @param appWidgetId The target widget ID
     * @param range       The range of the summaries
     * @param summaries   The summaries
     */
    public static void performWidgetUpdate(@NonNull Context context, int appWidgetId, @NonNull WakaTime.Range range, @NonNull Summaries summaries) {
        WidgetOptions options = loadWidgetOptions(appWidgetId);
        if (options.range != range)
            return;

        long total = summaries.globalSummary.total_seconds;
        AppWidgetManager.getInstance(context).updateAppWidget(appWidgetId, createRemoteViews(context, range, total));
    }

    @NonNull
    public static WidgetOptions loadWidgetOptions(int appWidgetId) {
        try {
            JSONObject obj = JsonStoring.intoPrefs().getJsonObject(PK.WIDGETS_CONFIG);
            if (obj == null) return new WidgetOptions();

            JSONObject options = obj.optJSONObject(String.valueOf(appWidgetId));
            if (options == null) return new WidgetOptions();
            else return new WidgetOptions(options);
        } catch (JSONException ex) {
            Log.e(TAG, "Failed getting widget options: " + appWidgetId, ex);
            return new WidgetOptions();
        }
    }

    public static void saveWidgetOptions(int appWidgetId, @NonNull WidgetOptions options) {
        try {
            JSONObject obj = JsonStoring.intoPrefs().getJsonObject(PK.WIDGETS_CONFIG);
            if (obj == null) obj = new JSONObject();

            obj.put(String.valueOf(appWidgetId), options.toJson());
            JsonStoring.intoPrefs().putJsonObject(PK.WIDGETS_CONFIG, obj);
        } catch (JSONException ex) {
            Log.e(TAG, "Failed saving widget options: " + appWidgetId, ex);
        }
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, @NonNull int[] appWidgetIds) {
        new WakaTime.Builder(context).alreadyAuthorized(new WakaTime.InitializationListener() {
            @Override
            public void onWakatimeInitialized(@NonNull WakaTime instance) {
                for (int appWidgetId : appWidgetIds)
                    performWidgetUpdate(instance, context, appWidgetId, null);
            }

            @Override
            public void onException(@NonNull Exception ex) {
                Log.e(TAG, "Failed initializing WakaTime, skipping update.", ex);
            }
        });
    }

    public static class WidgetOptions {
        public WakaTime.Range range;

        WidgetOptions() {
            range = WakaTime.Range.TODAY;
        }

        WidgetOptions(@NonNull JSONObject obj) throws JSONException {
            range = WakaTime.Range.valueOf(obj.getString("range"));
        }

        @NonNull
        private JSONObject toJson() throws JSONException {
            return new JSONObject().put("range", range.name());
        }
    }
}
