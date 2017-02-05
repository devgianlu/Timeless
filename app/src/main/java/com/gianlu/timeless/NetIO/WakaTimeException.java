package com.gianlu.timeless.NetIO;

import org.json.JSONException;
import org.json.JSONObject;

public class WakaTimeException extends Exception {
    public WakaTimeException(String body) throws JSONException {
        super(new JSONObject(body).getString("error"));
    }
}
