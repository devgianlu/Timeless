package com.gianlu.timeless.NetIO;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.LruCache;
import android.util.Pair;

import com.crashlytics.android.Crashlytics;
import com.gianlu.commonutils.CommonUtils;
import com.gianlu.commonutils.Logging;
import com.gianlu.commonutils.Preferences.Prefs;
import com.gianlu.timeless.BuildConfig;
import com.gianlu.timeless.Models.Commits;
import com.gianlu.timeless.Models.Durations;
import com.gianlu.timeless.Models.Leaders;
import com.gianlu.timeless.Models.Project;
import com.gianlu.timeless.Models.Summaries;
import com.gianlu.timeless.Models.User;
import com.gianlu.timeless.PKeys;
import com.gianlu.timeless.R;
import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.core.builder.api.BaseApi;
import com.github.scribejava.core.builder.api.DefaultApi20;
import com.github.scribejava.core.exceptions.OAuthException;
import com.github.scribejava.core.model.OAuth2AccessToken;
import com.github.scribejava.core.model.OAuth2Authorization;
import com.github.scribejava.core.model.OAuthAsyncRequestCallback;
import com.github.scribejava.core.model.OAuthConfig;
import com.github.scribejava.core.model.OAuthRequest;
import com.github.scribejava.core.model.Response;
import com.github.scribejava.core.model.Verb;
import com.github.scribejava.core.oauth.OAuth20Service;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class WakaTime {
    private static final String BASE_URL = "https://wakatime.com/api/v1/";
    private static final String APP_ID = "TLCbAeUZV03mu854dptQPE0s";
    private static final String APP_SECRET = "sec_yFZ1S6VZgZcjkUGPjN8VThQMbZGxjpzZUzjpA2uNJ6VY6LFKhunHfDV0RyUEqhXTWdYiEwJJAVr2ZLgs";
    private static final String CALLBACK = "timeless://grantActivity/";
    private static final long MAX_CACHE_AGE = TimeUnit.MINUTES.toMillis(10);
    private static final int MAX_CACHE_SIZE = 20;
    private static final OAuth20Service SERVICE;
    private static final Object getTokenLock = new Object();
    private static WakaTime instance;
    private static OnShouldGetToken shouldGetTokenListener = null;

    static {
        ServiceBuilder builder = new ServiceBuilder(APP_ID)
                .apiSecret(APP_SECRET)
                .callback(CALLBACK)
                .scope("email,read_stats,read_logged_time,read_teams");

        if (BuildConfig.DEBUG) builder.debug();

        SERVICE = builder.build(new WakaTimeApi());
    }

    private final ExecutorService executorService = Executors.newFixedThreadPool(3);
    private final Handler handler;
    private final LruCache<String, CachedResponse> memoryCache = new LruCache<>(MAX_CACHE_SIZE);
    private final OAuth2AccessToken token;
    private final SharedPreferences prefs;
    private final Requester requester;

    private WakaTime(Context context, @Nullable OAuth2AccessToken token) {
        this(PreferenceManager.getDefaultSharedPreferences(context), token);
    }

    private WakaTime(SharedPreferences prefs, @Nullable OAuth2AccessToken token) {
        this.token = token;
        this.prefs = prefs;
        handler = new Handler(Looper.getMainLooper());
        requester = new Requester();
    }

    public static void accessToken(final Context context, String uri, final OnAccessToken listener) {
        final Handler handler = new Handler(context.getMainLooper());
        final OAuth2Authorization auth = SERVICE.extractAuthorization(uri);

        new Thread() {
            @Override
            public void run() {
                synchronized (getTokenLock) {
                    SERVICE.getAccessToken(auth.getCode(), new OAuthAsyncRequestCallback<OAuth2AccessToken>() {
                        @Override
                        public void onCompleted(OAuth2AccessToken response) {
                            storeRefreshToken(context, response);
                            instance = new WakaTime(context, response);
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    listener.onTokenAccepted();
                                }
                            });

                            synchronized (getTokenLock) {
                                getTokenLock.notifyAll();
                            }
                        }

                        @Override
                        public void onThrowable(final Throwable ex) {
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    listener.onException(ex);
                                }
                            });

                            synchronized (getTokenLock) {
                                getTokenLock.notifyAll();
                            }
                        }
                    });

                    try {
                        getTokenLock.wait();
                    } catch (InterruptedException ex) {
                        Logging.log(ex);
                    }
                }
            }
        }.start();
    }

    public static void refreshToken(final Context context, final OnAccessToken listener) {
        final Handler handler = new Handler(context.getMainLooper());

        final String refreshToken = loadRefreshToken(context);
        if (refreshToken == null || refreshToken.isEmpty()) {
            listener.onException(new ShouldGetAccessToken());
            return;
        }

        new Thread() {
            @Override
            public void run() {
                synchronized (getTokenLock) {
                    SERVICE.refreshAccessToken(refreshToken, new OAuthAsyncRequestCallback<OAuth2AccessToken>() {
                        @Override
                        public void onCompleted(OAuth2AccessToken response) {
                            storeRefreshToken(context, response);
                            instance = new WakaTime(context, response);
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    listener.onTokenAccepted();
                                }
                            });

                            synchronized (getTokenLock) {
                                getTokenLock.notifyAll();
                            }
                        }

                        @Override
                        public void onThrowable(final Throwable ex) {
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    listener.onException(ex);
                                }
                            });

                            synchronized (getTokenLock) {
                                getTokenLock.notifyAll();
                            }
                        }
                    });

                    try {
                        getTokenLock.wait();
                    } catch (InterruptedException ex) {
                        Logging.log(ex);
                    }
                }
            }
        }.start();
    }

    private static void refreshTokenSync(SharedPreferences prefs) throws InterruptedException, ExecutionException, IOException {
        String refreshToken = loadRefreshToken(prefs);
        if (refreshToken == null || refreshToken.isEmpty()) {
            throw ShouldGetAccessToken.throwNow();
        }

        synchronized (getTokenLock) {
            getTokenLock.wait();
            OAuth2AccessToken token = SERVICE.refreshAccessToken(refreshToken);
            storeRefreshToken(prefs, token);
            instance = new WakaTime(instance.prefs, token);
            getTokenLock.notifyAll();
        }
    }

    @NonNull
    public static WakaTime get() {
        if (instance == null) throw ShouldGetAccessToken.throwNow();
        return instance;
    }

    @Nullable
    private static String loadRefreshToken(SharedPreferences prefs) {
        return Prefs.getString(prefs, PKeys.TOKEN, null);
    }

    @Nullable
    private static String loadRefreshToken(Context context) {
        return loadRefreshToken(PreferenceManager.getDefaultSharedPreferences(context));
    }

    private static void storeRefreshToken(Context context, OAuth2AccessToken token) {
        storeRefreshToken(PreferenceManager.getDefaultSharedPreferences(context), token);
    }

    private static void storeRefreshToken(SharedPreferences prefs, OAuth2AccessToken token) {
        Prefs.putString(prefs, PKeys.TOKEN, token.getRefreshToken());
    }

    @SuppressLint("SimpleDateFormat")
    private static SimpleDateFormat getAPIFormatter() {
        return new SimpleDateFormat("yyyy-MM-dd");
    }

    public static String authorizationUrl() {
        return SERVICE.getAuthorizationUrl();
    }

    public static void setShouldGetTokenListener(OnShouldGetToken listener) {
        shouldGetTokenListener = listener;
    }

    public void cacheEnabledChanged() {
        if (cacheEnabled()) memoryCache.resize(MAX_CACHE_SIZE);
        else memoryCache.trimToSize(-1);
    }

    private boolean cacheEnabled() {
        return Prefs.getBoolean(prefs, PKeys.CACHE_ENABLED, true);
    }

    @NonNull
    private Response doRequestSync(Verb verb, String url) throws InterruptedException, ExecutionException, IOException {
        CachedResponse cachedResponse;
        if (verb == Verb.GET && cacheEnabled()) cachedResponse = getFromCache(url);
        else cachedResponse = null;

        if (cachedResponse == null || System.currentTimeMillis() - cachedResponse.timestamp > MAX_CACHE_AGE) {
            OAuthRequest request = new OAuthRequest(verb, url);

            if (token == null) {
                refreshTokenSync(prefs);
                return instance.doRequestSync(verb, url);
            }

            synchronized (getTokenLock) {
                SERVICE.signRequest(token, request);
            }

            if (BuildConfig.DEBUG) Logging.log(request.toString(), false);

            Response resp;
            try {
                resp = SERVICE.execute(request);
            } catch (OAuthException ex) {
                throw new IOException("Just a wrapper", ex);
            }

            synchronized (memoryCache) {
                memoryCache.put(url, new CachedResponse(resp));
            }

            return resp;
        } else {
            return cachedResponse.response;
        }
    }

    @Nullable
    private CachedResponse getFromCache(String url) {
        synchronized (memoryCache) {
            return memoryCache.get(url);
        }
    }

    public void batch(BatchStuff listener) {
        executorService.execute(new BatchRequest(listener));
    }

    public void getCurrentUser(final OnUser listener) {
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    final User user = requester.user();
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            listener.onUser(user);
                        }
                    });
                } catch (InterruptedException | ExecutionException | JSONException | IOException | StatusCodeException ex) {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            listener.onException(ex);
                        }
                    });
                }
            }
        });
    }

    public void getLeaders(@Nullable final String language, final int page, final OnLeaders listener) {
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    final Leaders leaders = requester.leaders(language, page);
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            listener.onLeaders(leaders);
                        }
                    });
                } catch (InterruptedException | ExecutionException | JSONException | IOException | StatusCodeException ex) {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            listener.onException(ex);
                        }
                    });
                }
            }
        });
    }

    public void getProjects(final OnProjects listener) {
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    final List<Project> projects = requester.projects();
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            listener.onProjects(projects);
                        }
                    });
                } catch (InterruptedException | ExecutionException | IOException | JSONException | StatusCodeException ex) {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            listener.onException(ex);
                        }
                    });
                }
            }
        });
    }

    public void getCommits(final Project project, final int page, final OnCommits listener) {
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    final Commits commits = requester.commits(project, page);
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            listener.onCommits(commits);
                        }
                    });
                } catch (InterruptedException | ExecutionException | IOException | JSONException | StatusCodeException ex) {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            listener.onException(ex);
                        }
                    });
                }
            }
        });
    }

    public void getRangeSummary(final Pair<Date, Date> startAndEnd, final OnSummary listener) {
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    final Summaries summaries = requester.summaries(startAndEnd.first, startAndEnd.second, null, null);
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            listener.onSummary(summaries);
                        }
                    });
                } catch (InterruptedException | ExecutionException | IOException | JSONException | StatusCodeException ex) {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            listener.onException(ex);
                        }
                    });
                } catch (final WakaTimeException ex) {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            listener.onWakaTimeError(ex);
                        }
                    });
                }
            }
        });
    }

    public enum Range {
        TODAY,
        LAST_7_DAYS,
        LAST_30_DAYS;

        public String getFormal(Context context) {
            switch (this) {
                case TODAY:
                    return context.getString(R.string.today);
                default:
                case LAST_7_DAYS:
                    return context.getString(R.string.last_7_days);
                case LAST_30_DAYS:
                    return context.getString(R.string.last_30_days);
            }
        }

        public Pair<Date, Date> getStartAndEnd() {
            Calendar cal = Calendar.getInstance();
            Date end = cal.getTime();

            switch (this) {
                case TODAY:
                    break;
                default:
                case LAST_7_DAYS:
                    cal.add(Calendar.DATE, -6);
                    break;
                case LAST_30_DAYS:
                    cal.add(Calendar.DATE, -30);
                    break;
            }

            return new Pair<>(cal.getTime(), end);
        }

        public Pair<Date, Date> getWeekBefore() {
            if (this == TODAY) {
                Calendar cal = Calendar.getInstance();
                cal.add(Calendar.DATE, -1);
                Date end = cal.getTime();
                cal.add(Calendar.DATE, -7);
                Date start = cal.getTime();
                return new Pair<>(start, end);
            } else {
                throw new IllegalArgumentException("Range must be TODAY");
            }
        }
    }

    public interface OnShouldGetToken {
        void thrownException(ShouldGetAccessToken ex);
    }

    public interface OnLeaders {
        void onLeaders(Leaders leaders);

        void onException(Exception ex);
    }

    public interface OnSummary {
        void onSummary(Summaries summaries);

        void onWakaTimeError(WakaTimeException ex);

        void onException(Exception ex);
    }

    public interface OnProjects {
        void onProjects(List<Project> projects);

        void onException(Exception ex);
    }

    public interface OnCommits {
        void onCommits(Commits commits);

        void onException(Exception ex);
    }

    public interface OnUser {
        void onUser(User user);

        void onException(Exception ex);
    }

    public interface OnAccessToken {
        void onTokenAccepted();

        void onException(Throwable ex);
    }

    public interface BatchStuff {
        void request(Requester requester, Handler ui) throws Exception;

        void somethingWentWrong(Exception ex); // Always on UI thread
    }

    public static class ShouldGetAccessToken extends RuntimeException {

        private ShouldGetAccessToken() {
        }

        static Error throwNow() {
            ShouldGetAccessToken ex = new ShouldGetAccessToken();
            if (shouldGetTokenListener != null) {
                shouldGetTokenListener.thrownException(ex);
            } else if (Thread.getDefaultUncaughtExceptionHandler() != null) {
                Thread.getDefaultUncaughtExceptionHandler().uncaughtException(Thread.currentThread(), ex);
            }

            return new Error(ex);
        }
    }

    private static class CachedResponse {
        private final Response response;
        private final long timestamp;

        CachedResponse(Response response) {
            this.response = response;
            this.timestamp = System.currentTimeMillis();
        }
    }

    private static class WakaTimeApi implements BaseApi<OAuth20Service> {

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

    public class BatchRequest implements Runnable {
        private final BatchStuff stuff;

        private BatchRequest(@NonNull BatchStuff stuff) {
            this.stuff = stuff;
        }

        @Override
        public void run() {
            try {
                stuff.request(requester, handler);
            } catch (final Exception ex) {
                Logging.log(ex);

                if (ex instanceof RuntimeException) Crashlytics.logException(ex);

                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        stuff.somethingWentWrong(ex);
                    }
                });
            }
        }
    }

    public class Requester {

        public Durations durations(Date day, @Nullable Project project, @Nullable List<String> branches) throws IOException, JSONException, ExecutionException, InterruptedException, StatusCodeException {
            Response response = doRequestSync(Verb.GET, BASE_URL + "users/current/durations?date=" + getAPIFormatter().format(day)
                    + (branches != null ? ("&branches=" + CommonUtils.join(branches, ",")) : ""));

            if (response.getCode() == 200)
                return new Durations(new JSONObject(response.getBody()), project);
            else throw new StatusCodeException(response);
        }

        public Summaries summaries(Pair<Date, Date> startAndEnd, @Nullable Project project, @Nullable List<String> branches) throws InterruptedException, ExecutionException, IOException, JSONException, StatusCodeException, WakaTimeException {
            return summaries(startAndEnd.first, startAndEnd.second, project, branches);
        }

        public Summaries summaries(Date start, Date end, @Nullable Project project, @Nullable List<String> branches) throws IOException, JSONException, ExecutionException, InterruptedException, StatusCodeException, WakaTimeException {
            SimpleDateFormat formatter = getAPIFormatter();
            Response response = doRequestSync(Verb.GET, BASE_URL + "users/current/summaries?start=" + formatter.format(start)
                    + "&end=" + formatter.format(end)
                    + (project != null ? ("&project=" + project.name) : "")
                    + (branches != null ? ("&branches=" + CommonUtils.join(branches, ",")) : ""));

            if (response.getCode() == 200) {
                return new Summaries(new JSONObject(response.getBody()));
            } else if (response.getCode() == 400) {
                throw new WakaTimeException(response.getBody());
            } else {
                throw new StatusCodeException(response);
            }
        }

        public Commits commits(Project project, int page) throws IOException, StatusCodeException, ExecutionException, InterruptedException, JSONException {
            Response response = doRequestSync(Verb.GET, BASE_URL + "users/current/projects/" + project.id + "/commits?page=" + page);

            if (response.getCode() == 200) return new Commits(new JSONObject(response.getBody()));
            else throw new StatusCodeException(response);
        }

        public Leaders leaders(@Nullable String language, int page) throws InterruptedException, ExecutionException, IOException, JSONException, StatusCodeException {
            Response response = doRequestSync(Verb.GET, BASE_URL + "leaders" +
                    "?page=" + page +
                    (language != null ? ("&language=" + language) : ""));

            if (response.getCode() == 200) return new Leaders(new JSONObject(response.getBody()));
            else throw new StatusCodeException(response);
        }

        public List<Project> projects() throws InterruptedException, ExecutionException, IOException, JSONException, StatusCodeException {
            Response response = doRequestSync(Verb.GET, BASE_URL + "users/current/projects");

            if (response.getCode() == 200)
                return CommonUtils.toTList(new JSONObject(response.getBody()).getJSONArray("data"), Project.class);
            else
                throw new StatusCodeException(response);
        }

        public User user() throws InterruptedException, ExecutionException, IOException, JSONException, StatusCodeException {
            Response response = doRequestSync(Verb.GET, BASE_URL + "users/current");
            if (response.getCode() == 200)
                return new User(new JSONObject(response.getBody()).getJSONObject("data"));
            else
                throw new StatusCodeException(response);
        }
    }
}
