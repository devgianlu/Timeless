package com.gianlu.timeless.NetIO;

import com.github.scribejava.core.model.Response;

public class StatusCodeException extends Exception {
    private StatusCodeException(int code, String message) {
        super(code + ": " + message);
    }

    public StatusCodeException(Response response) {
        this(response.getCode(), response.getMessage());
    }
}
