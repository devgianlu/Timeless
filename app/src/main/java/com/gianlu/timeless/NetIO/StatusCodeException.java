package com.gianlu.timeless.NetIO;

import android.support.annotation.NonNull;

import java.io.IOException;

import okhttp3.Response;

public class StatusCodeException extends IOException {
    private StatusCodeException(int code, String message) {
        super(code + ": " + message);
    }

    StatusCodeException(@NonNull Response response) {
        this(response.code(), response.message());
    }
}
