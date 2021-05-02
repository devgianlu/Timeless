package com.gianlu.timeless.api;

import androidx.annotation.NonNull;

import java.io.IOException;

import okhttp3.Response;

public class StatusCodeException extends IOException {
    public final int code;

    private StatusCodeException(int code, String message) {
        super(code + ": " + message);
        this.code = code;
    }

    StatusCodeException(@NonNull Response response) {
        this(response.code(), response.message());
    }
}
