package com.gianlu.timeless.NetIO;

import org.json.JSONException;
import org.json.JSONObject;

public class WakaTimeException extends Exception {
    WakaTimeException(String body) throws JSONException {
        super(new JSONObject(body).getString("error"));
    }
}
