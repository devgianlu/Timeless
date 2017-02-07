package com.gianlu.timeless.Google;

import com.gianlu.timeless.BuildConfig;
import com.gianlu.timeless.ThisApplication;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import java.io.PrintWriter;
import java.io.StringWriter;

public class UncaughtExceptionHandler implements Thread.UncaughtExceptionHandler {
    @Override
    public void uncaughtException(final Thread thread, final Throwable throwable) {
        if (BuildConfig.DEBUG) {
            throwable.printStackTrace();
        } else {
            StringWriter writer = new StringWriter();
            throwable.printStackTrace(new PrintWriter(writer));

            Tracker tracker = ThisApplication.tracker;
            if (tracker != null)
                tracker.send(new HitBuilders.ExceptionBuilder()
                        .setDescription(writer.toString())
                        .setFatal(true)
                        .build());
        }
    }
}