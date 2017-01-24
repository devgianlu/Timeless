package com.gianlu.timeless.NetIO;

public class StatusCodeException extends Exception {
    public StatusCodeException(int code, String message) {
        super(code + ": " + message);
    }
}
