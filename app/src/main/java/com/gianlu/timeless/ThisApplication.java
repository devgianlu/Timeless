package com.gianlu.timeless;

import android.app.Application;
import android.content.Context;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;
import com.google.firebase.analytics.FirebaseAnalytics;

import java.util.Map;

public class ThisApplication extends Application {
    public static final String CATEGORY_USER_INPUT = "User input";
    public static final String ACTION_DATE_RANGE = "Changed date range";
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
        if (!PreferenceManager.getDefaultSharedPreferences(context).getBoolean("trackingDisable", false) && !BuildConfig.DEBUG)
            if (tracker != null)
                tracker.send(map);
    }

    @Override
    public void onCreate() {
        super.onCreate();

        FirebaseAnalytics.getInstance(this).setAnalyticsCollectionEnabled(!BuildConfig.DEBUG);
        tracker = getTracker(this);
    }
}
