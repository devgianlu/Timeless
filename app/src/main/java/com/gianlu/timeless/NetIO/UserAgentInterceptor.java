package com.gianlu.timeless.NetIO;

import com.gianlu.timeless.ThisApplication;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Response;

public class UserAgentInterceptor implements Interceptor {

    @Override
    public Response intercept(Chain chain) throws IOException {
        return chain.proceed(chain.request().newBuilder()
                .header("User-Agent", ThisApplication.USER_AGENT)
                .build());
    }
}