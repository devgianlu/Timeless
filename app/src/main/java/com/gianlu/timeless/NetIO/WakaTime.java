package com.gianlu.timeless.NetIO;

import android.content.Context;
import android.support.annotation.Nullable;

import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.core.builder.api.BaseApi;
import com.github.scribejava.core.builder.api.DefaultApi20;
import com.github.scribejava.core.model.OAuth2AccessToken;
import com.github.scribejava.core.model.OAuth2Authorization;
import com.github.scribejava.core.model.OAuthConfig;
import com.github.scribejava.core.model.OAuthRequest;
import com.github.scribejava.core.model.Response;
import com.github.scribejava.core.model.Verb;
import com.github.scribejava.core.oauth.OAuth20Service;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

public class WakaTime {
    private static final String APP_ID = "TLCbAeUZV03mu854dptQPE0s";
    private static final String APP_SECRET = "sec_yFZ1S6VZgZcjkUGPjN8VThQMbZGxjpzZUzjpA2uNJ6VY6LFKhunHfDV0RyUEqhXTWdYiEwJJAVr2ZLgs";
    private static final String CALLBACK = "timeless://grantActivity/";
    private static WakaTime instance;
    private final OAuth20Service service;
    private OAuth2AccessToken token;
    private String lastState;

    private WakaTime() {
        lastState = new BigInteger(130, new SecureRandom()).toString(32);
        service = new ServiceBuilder()
                .apiKey(APP_ID)
                .apiSecret(APP_SECRET)
                .callback(CALLBACK)
                .scope("email,read_stats")
                .state(lastState)
                .build(new WakaTimeApi());
    }

    public static WakaTime getInstance() {
        if (instance == null)
            instance = new WakaTime();
        return instance;
    }

    @Nullable
    private static String loadAccessToken(Context context) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(context.openFileInput("token")))) {
            return reader.readLine();
        } catch (IOException ex) {
            return null;
        }
    }

    private static void saveAccessToken(Context context, String token) {
        try (OutputStream out = context.openFileOutput("token", Context.MODE_PRIVATE)) {
            out.write(token.getBytes());
            out.flush();
            out.close();
        } catch (IOException ignored) {
        }
    }

    private void refreshTokenSync(Context context) throws InvalidTokenException, InterruptedException, ExecutionException, IOException {
        if (token == null) {
            String oldToken = loadAccessToken(context);
            if (oldToken == null)
                throw new InvalidTokenException();

            token = service.refreshAccessToken(oldToken);
            saveAccessToken(context, token.getRefreshToken());
        }
    }

    public String getAuthorizationUrl() {
        return service.getAuthorizationUrl();
    }

    public void getCurrentUser(final Context context, final IUser handler) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    refreshTokenSync(context);
                    Response response = doRequestSync(Verb.GET, "https://wakatime.com/api/v1/users/current");

                    if (response.getCode() == 200) {
                        handler.onUser(new User(new JSONObject(response.getBody()).getJSONObject("data")));
                    } else {
                        handler.onException(new StatusCodeException(response.getCode(), response.getMessage()));
                    }
                } catch (InvalidTokenException | InterruptedException | ExecutionException | IOException | JSONException ex) {
                    handler.onException(ex);
                }
            }
        }).start();
    }

    private Response doRequestSync(Verb verb, String url) throws InterruptedException, ExecutionException, IOException {
        final OAuthRequest request = new OAuthRequest(verb, url);
        service.signRequest(token, request);
        return service.execute(request);
    }

    public void newAccessToken(final Context context, final String uri, final INewAccessToken handler) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                OAuth2Authorization auth = service.extractAuthorization(uri);
                if (Objects.equals(auth.getState(), lastState)) {
                    try {
                        token = service.getAccessToken(auth.getCode());
                        saveAccessToken(context, token.getRefreshToken());
                        handler.onTokenAccepted();
                    } catch (InterruptedException | ExecutionException | IOException ex) {
                        handler.onException(ex);
                    }
                } else {
                    handler.onTokenRejected(new InvalidTokenException());
                }
            }
        }).start();
    }
    public interface IUser {
        void onUser(User user);

        void onException(Exception ex);
    }

    public interface INewAccessToken {
        void onTokenAccepted();

        void onTokenRejected(InvalidTokenException ex);

        void onException(Exception ex);
    }

    private class WakaTimeApi implements BaseApi<OAuth20Service> {

        @Override
        public OAuth20Service createService(OAuthConfig oAuthConfig) {
            return new OAuth20Service(new Api20(), oAuthConfig);
        }

        private class Api20 extends DefaultApi20 {
            @Override
            public String getAccessTokenEndpoint() {
                return "https://wakatime.com/oauth/token";
            }

            @Override
            protected String getAuthorizationBaseUrl() {
                return "https://wakatime.com/oauth/authorize";
            }
        }
    }
}
