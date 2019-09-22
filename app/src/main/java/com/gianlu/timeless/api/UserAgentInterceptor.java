package com.gianlu.timeless.api;

import androidx.annotation.NonNull;

import com.gianlu.timeless.ThisApplication;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Response;

public final class UserAgentInterceptor implements Interceptor {

    @NonNull
    @Override
    public Response intercept(Chain chain) throws IOException {
        return chain.proceed(chain.request().newBuilder()
                .header("User-Agent", ThisApplication.USER_AGENT)
                .build());
    }
}