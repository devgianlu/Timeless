package com.gianlu.timeless;

import android.app.Application;
import android.content.Context;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.gianlu.commonutils.CommonUtils;
import com.gianlu.commonutils.ConnectivityChecker;
import com.gianlu.commonutils.UncaughtExceptionActivity;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.google.firebase.analytics.FirebaseAnalytics;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

public class ThisApplication extends Application implements Thread.UncaughtExceptionHandler {
    public static final String CATEGORY_USER_INPUT = "User input";
    public static final String ACTION_DATE_RANGE = "Changed date range";
    public static final String ACTION_SAVED_CHART = "Chart saved as image";
    public static final String ACTION_DONATE_OPEN = "Opened donation dialog";
    public static final String ACTION_FILTER_LEADERS = "Filtered leaders";
    public static final String ACTION_SHOW_ME_LEADER = "Show me in leaderboards";
    public static Tracker tracker;

    @NonNull
    private static Tracker getTracker(Application application) {
        if (tracker == null) {
            GoogleAnalytics analytics = GoogleAnalytics.getInstance(application.getApplicationContext());
            analytics.enableAutoActivityReports(application);
            tracker = analytics.newTracker(R.xml.tracking);
            tracker.enableAdvertisingIdCollection(true);
            tracker.enableExceptionReporting(true);
        }

        return tracker;
    }

    public static void sendAnalytics(Context context, @Nullable Map<String, String> map) {
        if (tracker != null && !PreferenceManager.getDefaultSharedPreferences(context).getBoolean("trackingDisable", false) && !BuildConfig.DEBUG)
            tracker.send(map);
    }

    @Override
    public void onCreate() {
        super.onCreate();

        CommonUtils.setDebug(BuildConfig.DEBUG);

        FirebaseAnalytics.getInstance(this).setAnalyticsCollectionEnabled(!BuildConfig.DEBUG);
        if (!BuildConfig.DEBUG) tracker = getTracker(this);

        Thread.setDefaultUncaughtExceptionHandler(this);

        ConnectivityChecker.setUserAgent("Timeless, a Wakatime client");
        ConnectivityChecker.setProvider(new ConnectivityChecker.URLProvider() {
            @Override
            public URL getUrl(boolean useDotCom) throws MalformedURLException {
                return new URL("https://wakatime.com");
            }

            @Override
            public boolean validateResponse(HttpURLConnection connection) throws IOException {
                return connection.getResponseCode() == HttpURLConnection.HTTP_OK;
            }
        });
    }

    @Override
    public void uncaughtException(Thread thread, Throwable throwable) {
        if (BuildConfig.DEBUG) {
            throwable.printStackTrace();
        } else {
            StringWriter writer = new StringWriter();
            throwable.printStackTrace(new PrintWriter(writer));

            ThisApplication.sendAnalytics(getApplicationContext(), new HitBuilders.ExceptionBuilder()
                    .setDescription(writer.toString())
                    .setFatal(true)
                    .build());

            UncaughtExceptionActivity.startActivity(getApplicationContext(), getApplicationContext().getString(R.string.app_name), throwable);
        }
    }
}
