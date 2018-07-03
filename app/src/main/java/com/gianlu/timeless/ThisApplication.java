package com.gianlu.timeless;

import com.gianlu.commonutils.Analytics.AnalyticsApplication;
import com.gianlu.commonutils.ConnectivityChecker;
import com.gianlu.commonutils.Logging;
import com.gianlu.commonutils.Preferences.Prefs;
import com.gianlu.timeless.NetIO.WakaTime;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
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
            public boolean validateResponse(HttpURLConnection connection) throws IOException {
                return connection.getResponseCode() == HttpURLConnection.HTTP_OK;
            }
        });

        // Backward compatibility
        if (!Prefs.has(this, PK.TOKEN)) {
            try {
                BufferedReader in = new BufferedReader(new InputStreamReader(openFileInput("token")));
                String token = in.readLine();
                if (token != null && !token.isEmpty()) Prefs.putString(this, PK.TOKEN, token);
                deleteFile("token");
            } catch (IOException ex) {
                Logging.log(ex);
            }
        }
    }

    @Override
    protected boolean uncaughtNotDebug(Thread thread, Throwable throwable) {
        if (throwable instanceof WakaTime.ShouldGetAccessToken) {
            ((WakaTime.ShouldGetAccessToken) throwable).resolve(this);
            return false;
        }

        return true;
    }

    @Override
    protected boolean isDebug() {
        return BuildConfig.DEBUG;
    }
}
