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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
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
                .scope("email,read_stats,read_logged_time,read_teams")
                .state(lastState)
                .build(new WakaTimeApi());
    }

    public static WakaTime getInstance() {
        if (instance == null)
            instance = new WakaTime();
        return instance;
    }

    @Nullable
    private static String loadRefreshToken(Context context) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(context.openFileInput("token")));
        return reader.readLine();
    }

    private static void storeRefreshToken(Context context, OAuth2AccessToken token) throws IOException {
        OutputStream out = context.openFileOutput("token", Context.MODE_PRIVATE);
        out.write(token.getRefreshToken().getBytes());
        out.flush();
    }

    public void refreshToken(final Context context, final IRefreshToken handler) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String refreshToken = loadRefreshToken(context);
                    if (refreshToken == null)
                        throw new InvalidTokenException();

                    token = service.refreshAccessToken(refreshToken);
                    storeRefreshToken(context, token);
                    handler.onRefreshed();
                } catch (IOException | InvalidTokenException | InterruptedException | ExecutionException ex) {
                    handler.onException(ex);
                }
            }
        }).start();
    }

    public String getAuthorizationUrl() {
        return service.getAuthorizationUrl();
    }

    public void getCurrentUser(final IUser handler) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Response response = doRequestSync(Verb.GET, "https://wakatime.com/api/v1/users/current");

                    if (response.getCode() == 200) {
                        handler.onUser(new User(new JSONObject(response.getBody()).getJSONObject("data")));
                    } else {
                        handler.onException(new StatusCodeException(response.getCode(), response.getMessage()));
                    }
                } catch (InterruptedException | ExecutionException | IOException | JSONException ex) {
                    handler.onException(ex);
                }
            }
        }).start();
    }

    public void getProjects(final IProjects handler) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Response response = doRequestSync(Verb.GET, "https://wakatime.com/api/v1/users/current/projects");

                    if (response.getCode() == 200) {
                        JSONArray projectsArray = new JSONObject(response.getBody()).getJSONArray("data");
                        List<Project> projects = new ArrayList<>();
                        for (int i = 0; i < projectsArray.length(); i++)
                            projects.add(new Project(projectsArray.getJSONObject(i)));

                        handler.onProjects(projects);
                    } else {
                        handler.onException(new StatusCodeException(response.getCode(), response.getMessage()));
                    }
                } catch (InterruptedException | ExecutionException | IOException | JSONException ex) {
                    handler.onException(ex);
                }
            }
        }).start();
    }

    public void getCommits(final Project project, final ICommits handler) {
        getCommits(project, 1, handler);
    }

    public void getCommits(final Project project, final int page, final ICommits handler) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Response response = doRequestSync(Verb.GET, "https://wakatime.com/api/v1/users/current/projects/" + project.id + "/commits?page=" + page);

                    if (response.getCode() == 200) {
                        handler.onCommits(new Commits(new JSONObject(response.getBody())));
                    } else {
                        handler.onException(new StatusCodeException(response.getCode(), response.getMessage()));
                    }
                } catch (InterruptedException | ExecutionException | IOException | JSONException ex) {
                    handler.onException(ex);
                }
            }
        }).start();
    }

    public void getStats(final Stats.Range range, final IStats handler) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Response response = doRequestSync(Verb.GET, "https://wakatime.com/api/v1/users/current/stats/" + range.toValidFormat());

                    if (response.getCode() == 200) {
                        handler.onStats(new Stats(new JSONObject(response.getBody()).getJSONObject("data")));
                    } else {
                        handler.onException(new StatusCodeException(response.getCode(), response.getMessage()));
                    }
                } catch (InterruptedException | ExecutionException | IOException | JSONException | ParseException ex) {
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
                        storeRefreshToken(context, token);
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

    public interface IProjects {
        void onProjects(List<Project> projects);

        void onException(Exception ex);
    }

    public interface ICommits {
        void onCommits(Commits commits);

        void onException(Exception ex);
    }

    public interface IRefreshToken {
        void onRefreshed();

        void onException(Exception ex);
    }

    public interface IStats {
        void onStats(Stats stats);

        void onException(Exception ex);
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
