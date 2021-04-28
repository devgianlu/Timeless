package com.gianlu.timeless;

import android.preference.PreferenceManager;

import androidx.annotation.NonNull;

import com.gianlu.commonutils.analytics.AnalyticsApplication;
import com.gianlu.commonutils.network.ConnectivityChecker;
import com.gianlu.timeless.api.WakaTime;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class ThisApplication extends AnalyticsApplication {
    public static final String USER_AGENT = "Timeless by devgianlu";

    @Override
    public void onCreate() {
        super.onCreate();

        ConnectivityChecker.setUserAgent(USER_AGENT);
        ConnectivityChecker.setProvider(new ConnectivityChecker.URLProvider() {
            @Override
            public URL getUrl(boolean useDotCom) throws MalformedURLException {
                return new URL("https://wakatime.com");
            }

            @Override
            public boolean validateResponse(@NonNull HttpURLConnection connection) throws IOException {
                return connection.getResponseCode() == HttpURLConnection.HTTP_OK;
            }
        });

        PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener((sharedPreferences, key) -> {
            if (key.equals(PK.CACHE_ENABLED.key())) {
                try {
                    WakaTime.get().cacheEnabledChanged();
                } catch (WakaTime.MissingCredentialsException ex) {
                    ex.resolve(ThisApplication.this);
                }
            }
        });
    }

    @Override
    protected boolean uncaughtNotDebug(Thread thread, Throwable throwable) {
        if (throwable instanceof WakaTime.MissingCredentialsException) {
            ((WakaTime.MissingCredentialsException) throwable).resolve(this);
            return false;
        }

        return true;
    }

    @Override
    protected boolean isDebug() {
        return BuildConfig.DEBUG;
    }
}
