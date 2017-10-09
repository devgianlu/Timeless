package com.gianlu.timeless.NetIO;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.util.LruCache;
import android.util.Pair;

import com.gianlu.commonutils.CommonUtils;
import com.gianlu.timeless.Models.Commits;
import com.gianlu.timeless.Models.Duration;
import com.gianlu.timeless.Models.GlobalSummary;
import com.gianlu.timeless.Models.Leader;
import com.gianlu.timeless.Models.Project;
import com.gianlu.timeless.Models.Summary;
import com.gianlu.timeless.Models.User;
import com.gianlu.timeless.R;
import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.core.builder.api.BaseApi;
import com.github.scribejava.core.builder.api.DefaultApi20;
import com.github.scribejava.core.exceptions.OAuthException;
import com.github.scribejava.core.model.OAuth2AccessToken;
import com.github.scribejava.core.model.OAuth2AccessTokenErrorResponse;
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
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
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
    private static WakaTime instance;
    private final OAuth20Service service;
    private final String lastState;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private final Handler handler;
    private final LruCache<String, CachedResponse> memoryCache = new LruCache<>(MAX_CACHE_SIZE);
    private OAuth2AccessToken token;
    private boolean cacheEnabled = true;

    private WakaTime() {
        handler = new Handler(Looper.getMainLooper());
        lastState = new BigInteger(130, new SecureRandom()).toString(32);
        service = new ServiceBuilder(APP_ID)
                .apiSecret(APP_SECRET)
                .callback(CALLBACK)
                .scope("email,read_stats,read_logged_time,read_teams")
                .state(lastState)
                .build(new WakaTimeApi());
    }

    public static WakaTime getInstance() {
        if (instance == null) instance = new WakaTime();
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

    public void setCacheEnabled(boolean enabled) {
        this.cacheEnabled = enabled;
        if (enabled) memoryCache.resize(MAX_CACHE_SIZE);
        else memoryCache.trimToSize(-1);
    }

    /**
     * Also refreshes the {@link WakaTime#cacheEnabled} value
     */
    public void refreshToken(final Context context, final IRefreshToken listener) {
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    String refreshToken = loadRefreshToken(context);
                    if (refreshToken == null || refreshToken.isEmpty())
                        throw new InvalidTokenException();

                    token = service.refreshAccessToken(refreshToken);
                    storeRefreshToken(context, token);
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            listener.onRefreshed(instance);
                        }
                    });

                    cacheEnabled = PreferenceManager.getDefaultSharedPreferences(context).getBoolean("cacheEnabled", true);
                } catch (IOException | InvalidTokenException | InterruptedException | ExecutionException | OAuth2AccessTokenErrorResponse ex) {
                    if (ex instanceof OAuth2AccessTokenErrorResponse) {
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                listener.onInvalidToken(ex);
                            }
                        });
                    } else {
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                listener.onException(ex);
                            }
                        });
                    }
                }
            }
        });
    }

    public String getAuthorizationUrl() {
        return service.getAuthorizationUrl();
    }

    public void getCurrentUser(final IUser listener) {
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    final Response response = doRequestSync(Verb.GET, BASE_URL + "users/current");

                    if (response.getCode() == 200) {
                        final User user = new User(new JSONObject(response.getBody()).getJSONObject("data"));
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                listener.onUser(user);
                            }
                        });
                    } else {
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                listener.onException(new StatusCodeException(response.getCode(), response.getMessage()));
                            }
                        });
                    }
                } catch (final WakaTimeException ex) {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            listener.onInvalidToken(ex);
                        }
                    });
                } catch (InterruptedException | ExecutionException | IOException | JSONException ex) {
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

    public void getDurations(final Date day, @Nullable final List<String> branches, final IDurations listener) {
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                try {
                    final Response response = doRequestSync(Verb.GET, BASE_URL + "users/current/durations?date="
                            + formatter.format(day)
                            + (branches != null ? ("&branches=" + CommonUtils.join(branches, ",")) : ""));

                    if (response.getCode() == 200) {
                        JSONObject obj = new JSONObject(response.getBody());
                        final List<String> responseBranches = CommonUtils.toStringsList(obj.getJSONArray("branches"), true);
                        final List<Duration> durations = CommonUtils.toTList(obj.getJSONArray("data"), Duration.class);

                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                listener.onDurations(durations, responseBranches);
                            }
                        });
                    } else {
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                listener.onException(new StatusCodeException(response.getCode(), response.getMessage()));
                            }
                        });
                    }
                } catch (final WakaTimeException ex) {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            listener.onInvalidToken(ex);
                        }
                    });
                } catch (InterruptedException | ExecutionException | IOException | JSONException ex) {
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

    public void getDurations(final Date day, final Project project, @Nullable List<String> branches, final IDurations listener) {
        getDurations(day, branches, new IDurations() {
            @Override
            public void onDurations(List<Duration> durations, List<String> branches) {
                listener.onDurations(Duration.filter(durations, project.name), branches);
            }

            @Override
            public void onException(Exception ex) {
                listener.onException(ex);
            }

            @Override
            public void onInvalidToken(WakaTimeException ex) {
                listener.onInvalidToken(ex);
            }
        });
    }

    public void getProjects(final IProjects listener) {
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    final Response response = doRequestSync(Verb.GET, BASE_URL + "users/current/projects");

                    if (response.getCode() == 200) {
                        final List<Project> projects = CommonUtils.toTList(new JSONObject(response.getBody()).getJSONArray("data"), Project.class);

                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                listener.onProjects(projects);
                            }
                        });
                    } else {
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                listener.onException(new StatusCodeException(response.getCode(), response.getMessage()));
                            }
                        });
                    }
                } catch (final WakaTimeException ex) {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            listener.onInvalidToken(ex);
                        }
                    });
                } catch (InterruptedException | ExecutionException | IOException | JSONException ex) {
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

    public void getCommits(final Project project, final ICommits listener) {
        getCommits(project, 1, listener);
    }

    public void getCommits(final Project project, final int page, final ICommits listener) {
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    final Response response = doRequestSync(Verb.GET, BASE_URL + "users/current/projects/" + project.id + "/commits?page=" + page);

                    if (response.getCode() == 200) {
                        final Commits commits = new Commits(new JSONObject(response.getBody()));
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                listener.onCommits(commits);
                            }
                        });
                    } else {
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                listener.onException(new StatusCodeException(response.getCode(), response.getMessage()));
                            }
                        });
                    }
                } catch (final WakaTimeException ex) {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            listener.onInvalidToken(ex);
                        }
                    });
                } catch (InterruptedException | ExecutionException | IOException | JSONException ex) {
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

    public void getLeaders(final ILeaders listener) {
        getLeaders(null, 1, listener);
    }

    public void getLeaders(String language, final ILeaders listener) {
        getLeaders(language, 1, listener);
    }

    public void getLeaders(@Nullable final String language, final int page, final ILeaders listener) {
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    final Response response = doRequestSync(Verb.GET, BASE_URL + "leaders" +
                            "?page=" + page +
                            (language != null ? ("&language=" + language) : ""));

                    if (response.getCode() == 200) {
                        JSONObject obj = new JSONObject(response.getBody());
                        final List<Leader> leaders = CommonUtils.toTList(obj.getJSONArray("data"), Leader.class);
                        final Leader me = new Leader(obj.getJSONObject("current_user")); // TODO: Looks like that's not updated with the language
                        final int pages = obj.getInt("total_pages");

                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                listener.onLeaders(leaders, me, pages);
                            }
                        });
                    } else {
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                listener.onException(new StatusCodeException(response.getCode(), response.getMessage()));
                            }
                        });
                    }
                } catch (final WakaTimeException ex) {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            listener.onInvalidToken(ex);
                        }
                    });
                } catch (InterruptedException | ExecutionException | IOException | JSONException ex) {
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

    public void getRangeSummary(Pair<Date, Date> startAndEnd, final ISummary listener) {
        getRangeSummary(startAndEnd.first, startAndEnd.second, null, null, listener);
    }

    public void getRangeSummary(final Date start, final Date end, @Nullable final Project project, @Nullable final List<String> branches, final ISummary listener) {
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                try {
                    final Response response = doRequestSync(Verb.GET, BASE_URL + "users/current/summaries?start="
                            + formatter.format(start)
                            + "&end="
                            + formatter.format(end)
                            + (project != null ? ("&project=" + project.name) : "")
                            + (branches != null ? ("&branches=" + CommonUtils.join(branches, ",")) : ""));

                    if (response.getCode() == 200) {
                        JSONObject obj = new JSONObject(response.getBody());
                        final List<Summary> summaries = CommonUtils.toTList(obj.getJSONArray("data"), Summary.class);
                        final GlobalSummary globalSummary = new GlobalSummary(summaries);
                        final List<String> availableBranches;
                        if (obj.has("available_branches"))
                            availableBranches = CommonUtils.toStringsList(obj.getJSONArray("available_branches"), true);
                        else availableBranches = null;

                        final List<String> selectedBranches;
                        if (obj.has("branches"))
                            selectedBranches = CommonUtils.toStringsList(obj.getJSONArray("branches"), true);
                        else selectedBranches = null;

                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                listener.onSummary(summaries, globalSummary, availableBranches, selectedBranches);
                            }
                        });
                    } else if (response.getCode() == 400) {
                        final WakaTimeException ex = new WakaTimeException(response.getBody());
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                listener.onException(ex); // That's not an error
                            }
                        });
                    } else {
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                listener.onException(new StatusCodeException(response.getCode(), response.getMessage()));
                            }
                        });
                    }
                } catch (final WakaTimeException ex) {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            listener.onInvalidToken(ex);
                        }
                    });
                } catch (InterruptedException | ExecutionException | IOException | JSONException ex) {
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

    private Response doRequestSync(Verb verb, String url) throws InterruptedException, ExecutionException, IOException, OAuthException, JSONException, WakaTimeException {
        CachedResponse cachedResponse;
        if (verb == Verb.GET && cacheEnabled) cachedResponse = getFromCache(url);
        else cachedResponse = null;

        if (cachedResponse == null || System.currentTimeMillis() - cachedResponse.timestamp > MAX_CACHE_AGE) {
            if (token == null) throw new WakaTimeException("OAuth2AccessToken is null");
            final OAuthRequest request = new OAuthRequest(verb, url);
            service.signRequest(token, request);
            Response resp = service.execute(request);
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

    public void newAccessToken(final Context context, final String uri, final INewAccessToken listener) {
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                OAuth2Authorization auth = service.extractAuthorization(uri);
                if (Objects.equals(auth.getState(), lastState)) {
                    try {
                        token = service.getAccessToken(auth.getCode());
                        storeRefreshToken(context, token);
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                listener.onTokenAccepted();
                            }
                        });
                    } catch (InterruptedException | ExecutionException | IOException ex) {
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                listener.onException(ex);
                            }
                        });
                    }
                } else {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            listener.onTokenRejected(new InvalidTokenException());
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

    public interface IDurations {
        void onDurations(List<Duration> durations, List<String> branches);

        void onException(Exception ex);

        void onInvalidToken(WakaTimeException ex);
    }

    public interface ILeaders {
        void onLeaders(List<Leader> leaders, Leader me, int maxPages);

        void onException(Exception ex);

        void onInvalidToken(WakaTimeException ex);
    }

    public interface ISummary {
        void onSummary(List<Summary> summaries, GlobalSummary globalSummary, @Nullable List<String> branches, @Nullable List<String> selectedBranches);

        void onInvalidToken(WakaTimeException ex);

        void onException(Exception ex);
    }

    public interface IProjects {
        void onProjects(List<Project> projects);

        void onException(Exception ex);

        void onInvalidToken(WakaTimeException ex);
    }

    public interface ICommits {
        void onCommits(Commits commits);

        void onException(Exception ex);

        void onInvalidToken(WakaTimeException ex);
    }

    public interface IRefreshToken {
        void onRefreshed(WakaTime wakaTime);

        void onInvalidToken(Exception ex);

        void onException(Exception ex);
    }

    public interface IUser {
        void onUser(User user);

        void onException(Exception ex);

        void onInvalidToken(WakaTimeException ex);
    }

    public interface INewAccessToken {
        void onTokenAccepted();

        void onTokenRejected(InvalidTokenException ex);

        void onException(Exception ex);
    }

    private static class CachedResponse {
        private final Response response;
        private final long timestamp;

        CachedResponse(Response response) {
            this.response = response;
            this.timestamp = System.currentTimeMillis();
        }
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
