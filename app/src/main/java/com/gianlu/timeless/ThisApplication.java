package com.gianlu.timeless;

import android.content.Intent;

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

public class ThisApplication extends AnalyticsApplication implements WakaTime.OnShouldGetToken {

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

        WakaTime.setShouldGetTokenListener(this);

        // Backward compatibility
        if (!Prefs.has(this, PKeys.TOKEN)) {
            try {
                BufferedReader in = new BufferedReader(new InputStreamReader(openFileInput("token")));
                String token = in.readLine();
                if (token != null && !token.isEmpty()) Prefs.putString(this, PKeys.TOKEN, token);
                deleteFile("token");
            } catch (IOException ex) {
                Logging.log(ex);
            }
        }
    }

    private void startGrant() {
        startActivity(new Intent(this, GrantActivity.class)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
    }

    @Override
    protected boolean uncaughtNotDebug(Thread thread, Throwable throwable) {
        if (throwable instanceof WakaTime.ShouldGetAccessToken) {
            startGrant();
            return false;
        }

        return true;
    }

    @Override
    protected boolean isDebug() {
        return BuildConfig.DEBUG;
    }

    @Override
    public void thrownException(WakaTime.ShouldGetAccessToken ex) {
        Logging.log(ex);
        startGrant();
    }
}
