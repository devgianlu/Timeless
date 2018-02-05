package com.gianlu.timeless;

import android.content.Intent;

import com.gianlu.commonutils.Analytics.AnalyticsApplication;
import com.gianlu.commonutils.ConnectivityChecker;
import com.gianlu.timeless.NetIO.WakaTime;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class ThisApplication extends AnalyticsApplication {

    @Override
    public void onCreate() {
        super.onCreate();

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
    protected boolean uncaughtNotDebug(Thread thread, Throwable throwable) {
        if (throwable instanceof WakaTime.ShouldGetAccessToken) {
            startActivity(new Intent(this, GrantActivity.class)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
            return false;
        }

        return true;
    }

    @Override
    protected boolean isDebug() {
        return BuildConfig.DEBUG;
    }
}
