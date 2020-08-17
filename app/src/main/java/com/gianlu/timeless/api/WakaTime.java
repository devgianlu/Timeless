package com.gianlu.timeless.api;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.util.LruCache;
import android.util.Pair;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.UiThread;
import androidx.annotation.WorkerThread;

import com.gianlu.commonutils.CommonUtils;
import com.gianlu.commonutils.lifecycle.LifecycleAwareHandler;
import com.gianlu.commonutils.lifecycle.LifecycleAwareRunnable;
import com.gianlu.commonutils.preferences.Prefs;
import com.gianlu.timeless.GrantActivity;
import com.gianlu.timeless.PK;
import com.gianlu.timeless.R;
import com.gianlu.timeless.ThisApplication;
import com.gianlu.timeless.api.models.Commits;
import com.gianlu.timeless.api.models.Durations;
import com.gianlu.timeless.api.models.Leaderboards;
import com.gianlu.timeless.api.models.Leaders;
import com.gianlu.timeless.api.models.LeadersWithMe;
import com.gianlu.timeless.api.models.LifetimeStats;
import com.gianlu.timeless.api.models.Project;
import com.gianlu.timeless.api.models.Projects;
import com.gianlu.timeless.api.models.Summaries;
import com.gianlu.timeless.api.models.User;
import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.core.builder.api.DefaultApi20;
import com.github.scribejava.core.exceptions.OAuthException;
import com.github.scribejava.core.extractors.OAuth2AccessTokenJsonExtractor;
import com.github.scribejava.core.model.OAuth2AccessToken;
import com.github.scribejava.core.model.OAuth2AccessTokenErrorResponse;
import com.github.scribejava.core.model.OAuth2Authorization;
import com.github.scribejava.core.oauth.OAuth20Service;
import com.github.scribejava.core.oauth2.clientauthentication.ClientAuthentication;
import com.github.scribejava.core.oauth2.clientauthentication.RequestBodyAuthenticationScheme;
import com.github.scribejava.httpclient.okhttp.OkHttpHttpClient;

import org.jetbrains.annotations.Contract;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class WakaTime {
    private static final HttpUrl BASE_URL;
    private static final long MAX_CACHE_AGE = TimeUnit.MINUTES.toMillis(10);
    private static final int MAX_CACHE_SIZE = 20;
    private static final String TAG = WakaTime.class.getSimpleName();
    private static WakaTime instance;

    static {
        BASE_URL = HttpUrl.get("https://wakatime.com/api/v1/").newBuilder()
                .addQueryParameter("timezone", TimeZone.getDefault().getID())
                .build();
    }

    private final OkHttpClient client;
    private final LifecycleAwareHandler handler;
    private final LruCache<HttpUrl, CachedResponse> memoryCache = new LruCache<>(MAX_CACHE_SIZE);
    private final Requester requester;
    private final ExecutorService executorService;
    private final OAuth20Service service;
    private volatile boolean skipCache = false;
    private volatile boolean skipNextCache = false;
    private OAuth2AccessToken token;
    private long tokenCreateAt;

    private WakaTime(@NonNull Builder builder) throws ShouldGetAccessToken {
        if (builder.token == null) throw new ShouldGetAccessToken("Requested token is null!");

        this.client = builder.client;
        this.handler = new LifecycleAwareHandler(builder.handler);
        this.executorService = builder.executorService;
        this.token = builder.token;
        this.tokenCreateAt = builder.tokenCreateAt;
        this.service = builder.service;
        this.requester = new Requester();
    }

    @NonNull
    public static WakaTime get() throws ShouldGetAccessToken {
        if (instance == null) throw new ShouldGetAccessToken("Instance hasn't been initialized!");
        return instance;
    }

    @NonNull
    @Contract(" -> new")
    @SuppressLint("SimpleDateFormat")
    private static SimpleDateFormat getAPIFormatter() {
        return new SimpleDateFormat("yyyy-MM-dd");
    }

    //region Store token
    private static void storeToken(@NonNull OAuth2AccessToken token, long createdAt) {
        Prefs.putString(PK.TOKEN_RAW, token.getRawResponse());
        Prefs.putLong(PK.TOKEN_CREATED_AT, createdAt);
    }

    @Nullable
    private static OAuth2AccessToken loadToken() {
        String raw = Prefs.getString(PK.TOKEN_RAW, null);
        if (raw == null) return null;

        try {
            return OAuth2AccessTokenJsonExtractor.instance().extract(new com.github.scribejava.core.model.Response(200, null, null, raw));
        } catch (IOException ex) {
            Log.e(TAG, "Failed loading token.", ex);
            return null;
        }
    }
    //endregion

    //region Cache
    public void cacheEnabledChanged() {
        if (cacheEnabled()) memoryCache.resize(MAX_CACHE_SIZE);
        else memoryCache.trimToSize(-1);
    }

    private boolean cacheEnabled() {
        return Prefs.getBoolean(PK.CACHE_ENABLED, true);
    }

    public void skipNextRequestCache() {
        skipNextCache = true;
    }

    @Nullable
    private CachedResponse getFromCache(HttpUrl url) {
        synchronized (memoryCache) {
            return memoryCache.get(url);
        }
    }
    //endregion

    @NonNull
    @WorkerThread
    private synchronized JSONObject doRequestSync(HttpUrl url) throws IOException, ExecutionException, InterruptedException, JSONException, WakaTimeException {
        CachedResponse cachedResponse;
        if (cacheEnabled() && !skipCache && !skipNextCache) cachedResponse = getFromCache(url);
        else cachedResponse = null;

        if (cachedResponse == null || System.currentTimeMillis() - cachedResponse.timestamp > MAX_CACHE_AGE) {
            if (tokenCreateAt + token.getExpiresIn() * 1000 <= System.currentTimeMillis()) {
                token = service.refreshAccessToken(token.getRefreshToken());
                tokenCreateAt = System.currentTimeMillis();
                storeToken(token, tokenCreateAt);

                Log.d(TAG, "Refreshed token.");
            }

            Request.Builder request = new Request.Builder().get().url(url);
            request.addHeader("Authorization", String.format("Bearer %s", token.getAccessToken()));

            try (Response resp = client.newCall(request.build()).execute()) {
                ResponseBody body = resp.body();
                if (body == null) throw new IOException("Body is empty!");

                JSONObject obj = new JSONObject(body.string());
                if (resp.code() == 200) {
                    synchronized (memoryCache) {
                        memoryCache.put(url, new CachedResponse(obj));
                    }

                    skipNextCache = false;

                    return obj;
                } else if (resp.code() == 400 || resp.code() == 402) {
                    throw new WakaTimeException(obj);
                } else {
                    throw new StatusCodeException(resp);
                }
            }
        } else {
            return cachedResponse.response;
        }
    }

    //region Requests
    public void batch(@Nullable Activity activity, @NonNull BatchStuff listener, boolean skipCache) {
        executorService.execute(new BatchRequest(activity == null ? listener : activity, listener, skipCache));
    }

    public void getCurrentUser(@Nullable Activity activity, @NonNull OnResult<User> listener) {
        executorService.execute(new LifecycleAwareRunnable(handler, activity == null ? listener : activity) {
            @Override
            public void run() {
                try {
                    User user = requester.user();
                    post(() -> listener.onResult(user));
                } catch (JSONException | IOException | InterruptedException | ExecutionException | WakaTimeException ex) {
                    post(() -> listener.onException(ex));
                }
            }
        });
    }

    public void getLeaders(@Nullable String language, int page, @Nullable Activity activity, @NonNull OnResult<LeadersWithMe> listener) {
        executorService.execute(new LifecycleAwareRunnable(handler, activity == null ? listener : activity) {
            @Override
            public void run() {
                try {
                    LeadersWithMe leaders = requester.leaders(language, page);
                    post(() -> listener.onResult(leaders));
                } catch (JSONException | IOException | InterruptedException | ExecutionException | WakaTimeException ex) {
                    post(() -> listener.onException(ex));
                }
            }
        });
    }

    public void getLeaders(@NonNull String id, @Nullable String language, int page, @Nullable Activity activity, @NonNull OnResult<Leaders> listener) {
        executorService.execute(new LifecycleAwareRunnable(handler, activity == null ? listener : activity) {
            @Override
            public void run() {
                try {
                    Leaders leaders = requester.leaders(id, language, page);
                    post(() -> listener.onResult(leaders));
                } catch (JSONException | IOException | InterruptedException | ExecutionException | WakaTimeException ex) {
                    post(() -> listener.onException(ex));
                }
            }
        });
    }

    public void getProjects(@Nullable Activity activity, @NonNull OnResult<Projects> listener) {
        executorService.execute(new LifecycleAwareRunnable(handler, activity == null ? listener : activity) {
            @Override
            public void run() {
                try {
                    Projects projects = requester.projects();
                    post(() -> listener.onResult(projects));
                } catch (IOException | JSONException | InterruptedException | ExecutionException | WakaTimeException ex) {
                    post(() -> listener.onException(ex));
                }
            }
        });
    }

    public void getCommits(@NonNull Project project, int page, @Nullable Activity activity, @NonNull OnResult<Commits> listener) {
        executorService.execute(new LifecycleAwareRunnable(handler, activity == null ? listener : activity) {
            @Override
            public void run() {
                try {
                    Commits commits = requester.commits(project, page);
                    post(() -> listener.onResult(commits));
                } catch (IOException | JSONException | ParseException | InterruptedException | ExecutionException | WakaTimeException ex) {
                    post(() -> listener.onException(ex));
                }
            }
        });
    }

    public void getRangeSummary(@NonNull Pair<Date, Date> startAndEnd, @Nullable Activity activity, @NonNull OnSummary listener) {
        executorService.execute(new LifecycleAwareRunnable(handler, activity == null ? listener : activity) {
            @Override
            public void run() {
                try {
                    Summaries summaries = requester.summaries(startAndEnd.first, startAndEnd.second, null, null);
                    post(() -> listener.onSummary(summaries));
                } catch (IOException | JSONException | ParseException | InterruptedException | ExecutionException ex) {
                    post(() -> listener.onException(ex));
                } catch (WakaTimeException ex) {
                    post(() -> listener.onWakaTimeError(ex));
                }
            }
        });
    }

    public void getPrivateLeaderboards(int page, @Nullable Activity activity, @NonNull OnResult<Leaderboards> listener) {
        executorService.execute(new LifecycleAwareRunnable(handler, activity == null ? listener : activity) {
            @Override
            public void run() {
                try {
                    Leaderboards leaderboards = requester.privateLeaderboards(page);
                    post(() -> listener.onResult(leaderboards));
                } catch (IOException | JSONException | InterruptedException | ExecutionException | WakaTimeException ex) {
                    post(() -> listener.onException(ex));
                }
            }
        });
    }

    public void getLifetimeStats(@Nullable String project, @Nullable Activity activity, @NonNull OnResult<LifetimeStats> listener) {
        executorService.execute(new LifecycleAwareRunnable(handler, activity == null ? listener : activity) {
            @Override
            public void run() {
                try {
                    LifetimeStats lifetimeStats = requester.lifetimeTotal(project);
                    post(() -> listener.onResult(lifetimeStats));
                } catch (IOException | JSONException | InterruptedException | ExecutionException | WakaTimeException ex) {
                    post(() -> listener.onException(ex));
                }
            }
        });
    }
    //endregion

    public enum Range {
        TODAY,
        LAST_7_DAYS,
        LAST_30_DAYS;

        @NonNull
        public String getFormal(@NonNull Context context) {
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

        @NonNull
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
                    cal.add(Calendar.DATE, -29);
                    break;
            }

            return new Pair<>(cal.getTime(), end);
        }

        @NonNull
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

    public interface OnResult<R> {
        @UiThread
        void onResult(@NonNull R result);

        @UiThread
        void onException(@NonNull Exception ex);
    }

    public interface OnSummary {
        void onSummary(@NonNull Summaries summaries);

        void onWakaTimeError(@NonNull WakaTimeException ex);

        void onException(@NonNull Exception ex);
    }

    public interface InitializationListener {

        @UiThread
        void onWakatimeInitialized(@NonNull WakaTime instance);

        @UiThread
        void onException(@NonNull Exception ex);
    }

    public interface BatchStuff {
        @WorkerThread
        void request(@NonNull Requester requester, @NonNull LifecycleAwareHandler ui) throws Exception;

        @UiThread
        void somethingWentWrong(@NonNull Exception ex);
    }

    private static class WakatimeApi extends DefaultApi20 {
        @Override
        public ClientAuthentication getClientAuthentication() {
            return RequestBodyAuthenticationScheme.instance();
        }

        @Override
        public String getAccessTokenEndpoint() {
            return "https://wakatime.com/oauth/token";
        }

        @Override
        protected String getAuthorizationBaseUrl() {
            return "https://wakatime.com/oauth/authorize";
        }
    }

    public static class Builder {
        private final OAuth20Service service;
        private final OkHttpClient client;
        private final Context context;
        private final ExecutorService executorService = Executors.newFixedThreadPool(3);
        private final Handler handler;
        private long tokenCreateAt;
        private OAuth2AccessToken token;

        public Builder(Context context) {
            this.context = context;
            this.handler = new Handler(Looper.getMainLooper());
            this.client = new OkHttpClient.Builder()
                    .addInterceptor(new UserAgentInterceptor())
                    .build();

            ServiceBuilder builder = new ServiceBuilder("TLCbAeUZV03mu854dptQPE0s");
            builder.withScope("email,read_stats,read_logged_time,read_private_leaderboards");
            builder.apiSecret("sec_yFZ1S6VZgZcjkUGPjN8VThQMbZGxjpzZUzjpA2uNJ6VY6LFKhunHfDV0RyUEqhXTWdYiEwJJAVr2ZLgs")
                    .callback("timeless://grantActivity/")
                    .userAgent(ThisApplication.USER_AGENT)
                    .httpClient(new OkHttpHttpClient(client));
            this.service = builder.build(new WakatimeApi());
        }

        public void startFlow() {
            try {
                context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(service.getAuthorizationUrl())));
            } catch (ActivityNotFoundException ignored) {
            }
        }

        public void endFlow(@NonNull String data, @NonNull InitializationListener listener) {
            executorService.execute(() -> {
                try {
                    OAuth2Authorization auth = service.extractAuthorization(data);
                    if (auth.getCode() == null)
                        throw new ShouldGetAccessToken("Failed getting authorization code!");

                    token = service.getAccessToken(auth.getCode());
                    tokenCreateAt = System.currentTimeMillis();
                    storeToken(token, tokenCreateAt);

                    WakaTime w = build();
                    handler.post(() -> listener.onWakatimeInitialized(w));
                } catch (ShouldGetAccessToken ex) {
                    ex.resolve(context);
                } catch (IOException | InterruptedException | ExecutionException | OAuthException ex) {
                    handler.post(() -> listener.onException(ex));
                }
            });
        }

        public void alreadyAuthorized(@NonNull InitializationListener listener) {
            if (WakaTime.instance != null) {
                listener.onWakatimeInitialized(WakaTime.instance);
                return;
            }

            long storedCreatedAt;
            OAuth2AccessToken storedToken;
            if (Prefs.has(PK.TOKEN)) {
                storedCreatedAt = 0; // Definitely expired
                storedToken = new OAuth2AccessToken("", null, 0, Prefs.getString(PK.TOKEN, null), null, null);
                Log.d(TAG, "Used deprecated token.");
            } else {
                storedCreatedAt = Prefs.getLong(PK.TOKEN_CREATED_AT, 0);
                storedToken = loadToken();
                if (storedToken == null) {
                    listener.onException(new ShouldGetAccessToken("Missing stored token."));
                    return;
                }
            }

            executorService.execute(() -> {
                try {
                    if (storedCreatedAt + storedToken.getExpiresIn() * 1000 <= System.currentTimeMillis()) {
                        token = service.refreshAccessToken(storedToken.getRefreshToken());
                        tokenCreateAt = System.currentTimeMillis();
                        storeToken(token, tokenCreateAt);

                        Prefs.remove(PK.TOKEN);
                    } else {
                        token = storedToken;
                        tokenCreateAt = storedCreatedAt;
                    }

                    if (!token.getScope().contains("read_private_leaderboards"))
                        throw new ShouldGetAccessToken("Missing `read_private_leaderboards` scope");

                    WakaTime w = build();
                    handler.post(() -> listener.onWakatimeInitialized(w));
                } catch (ShouldGetAccessToken ex) {
                    ex.resolve(context);
                } catch (IOException | ExecutionException | InterruptedException | OAuth2AccessTokenErrorResponse ex) {
                    handler.post(() -> listener.onException(ex));
                }
            });
        }

        @NonNull
        private WakaTime build() throws ShouldGetAccessToken {
            WakaTime w = new WakaTime(this);
            WakaTime.instance = w;
            return w;
        }
    }

    public static class ShouldGetAccessToken extends Exception {
        ShouldGetAccessToken(String message) {
            super(message);
        }

        public void resolve(@Nullable Context context) {
            if (context == null) throw new IllegalStateException(this);
            context.startActivity(new Intent(context, GrantActivity.class)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
        }
    }

    private static class CachedResponse {
        private final JSONObject response;
        private final long timestamp;

        CachedResponse(JSONObject response) {
            this.response = response;
            this.timestamp = System.currentTimeMillis();
        }
    }

    public class BatchRequest implements Runnable {
        private final Object ctx;
        private final BatchStuff stuff;
        private final boolean skipCache;

        private BatchRequest(@Nullable Object ctx, @NonNull BatchStuff stuff, boolean skipCache) {
            this.ctx = ctx;
            this.stuff = stuff;
            this.skipCache = skipCache;
        }

        @Override
        public void run() {
            try {
                WakaTime.this.skipCache = skipCache;
                stuff.request(requester, handler);
                WakaTime.this.skipCache = false;
            } catch (Exception ex) {
                Log.e(TAG, "Request failed.", ex);
                handler.post(ctx, () -> stuff.somethingWentWrong(ex));
            }
        }
    }

    @WorkerThread
    public class Requester {

        @NonNull
        public Durations durations(Date day, @Nullable Project project, @Nullable List<String> branches) throws IOException, JSONException, ExecutionException, InterruptedException, WakaTimeException {
            HttpUrl.Builder builder = BASE_URL.newBuilder().addPathSegments("users/current/durations")
                    .addQueryParameter("date", getAPIFormatter().format(day));

            if (branches != null)
                builder.addQueryParameter("branches", CommonUtils.join(branches, ","));

            return new Durations(doRequestSync(builder.build()), project);
        }

        @NonNull
        public Summaries summaries(@NonNull Pair<Date, Date> startAndEnd, @Nullable Project project, @Nullable List<String> branches) throws IOException, JSONException, WakaTimeException, ParseException, ExecutionException, InterruptedException {
            return summaries(startAndEnd.first, startAndEnd.second, project, branches);
        }

        @NonNull
        public Summaries summaries(Date start, Date end, @Nullable Project project, @Nullable List<String> branches) throws IOException, JSONException, WakaTimeException, ParseException, ExecutionException, InterruptedException {
            SimpleDateFormat formatter = getAPIFormatter();
            HttpUrl.Builder builder = BASE_URL.newBuilder().addPathSegments("users/current/summaries")
                    .addQueryParameter("start", formatter.format(start))
                    .addQueryParameter("end", formatter.format(end));

            if (project != null)
                builder.addQueryParameter("project", project.name);

            if (branches != null)
                builder.addQueryParameter("branches", CommonUtils.join(branches, ","));

            return new Summaries(doRequestSync(builder.build()));
        }

        @NonNull
        public Commits commits(@NonNull Project project, int page) throws IOException, JSONException, ParseException, ExecutionException, InterruptedException, WakaTimeException {
            return new Commits(doRequestSync(BASE_URL.newBuilder()
                    .addPathSegments("users/current/projects/" + project.id + "/commits")
                    .addQueryParameter("page", String.valueOf(page)).build()));
        }

        @NonNull
        public LeadersWithMe leaders(@Nullable String language, int page) throws IOException, JSONException, ExecutionException, InterruptedException, WakaTimeException {
            HttpUrl.Builder builder = BASE_URL.newBuilder()
                    .addPathSegment("leaders")
                    .addQueryParameter("page", String.valueOf(page));

            if (language != null)
                builder.addQueryParameter("language", language);

            return new LeadersWithMe(doRequestSync(builder.build()));
        }

        @NonNull
        public Leaders leaders(@NonNull String id, @Nullable String language, int page) throws InterruptedException, ExecutionException, IOException, JSONException, WakaTimeException {
            HttpUrl.Builder builder = BASE_URL.newBuilder()
                    .addPathSegments("users/current/leaderboards/" + id)
                    .addQueryParameter("page", String.valueOf(page));

            if (language != null)
                builder.addQueryParameter("language", language);

            return new Leaders(doRequestSync(builder.build()));
        }

        @NonNull
        public Projects projects() throws IOException, JSONException, ExecutionException, InterruptedException, WakaTimeException {
            return new Projects(doRequestSync(BASE_URL.newBuilder()
                    .addPathSegments("users/current/projects").build()));
        }

        @NonNull
        public User user() throws IOException, JSONException, ExecutionException, InterruptedException, WakaTimeException {
            return new User(doRequestSync(BASE_URL.newBuilder()
                    .addPathSegments("users/current").build()).getJSONObject("data"));
        }

        @NonNull
        public Leaderboards privateLeaderboards(int page) throws InterruptedException, ExecutionException, IOException, JSONException, WakaTimeException {
            return new Leaderboards(doRequestSync(BASE_URL.newBuilder()
                    .addQueryParameter("page", String.valueOf(page))
                    .addPathSegments("users/current/leaderboards").build()));
        }

        @NonNull
        public LifetimeStats lifetimeTotal(@Nullable String project) throws InterruptedException, ExecutionException, IOException, JSONException, WakaTimeException {
            HttpUrl.Builder builder = BASE_URL.newBuilder()
                    .addPathSegments("users/current/all_time_since_today");

            if (project != null)
                builder.addQueryParameter("project", project);

            return new LifetimeStats(doRequestSync(builder.build()));
        }
    }
}
