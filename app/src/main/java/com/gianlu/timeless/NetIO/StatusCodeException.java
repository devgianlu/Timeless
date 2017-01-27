package com.gianlu.timeless.NetIO;

import org.json.JSONException;
import org.json.JSONObject;

public class StatusCodeException extends Exception {
    public StatusCodeException(int code, String message) {
        super(code + ": " + message);
    }

    public StatusCodeException(String body) throws JSONException {
        super(new JSONObject(body).getString("error"));
    }
}
