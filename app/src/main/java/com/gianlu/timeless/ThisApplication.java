package com.gianlu.timeless;

import com.gianlu.commonutils.Analytics.AnalyticsApplication;
import com.gianlu.commonutils.ConnectivityChecker;

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
    protected boolean isDebug() {
        return BuildConfig.DEBUG;
    }
}
