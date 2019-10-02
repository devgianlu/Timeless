package com.gianlu.timeless.api;

import org.json.JSONException;
import org.json.JSONObject;

public class WakaTimeException extends Exception {
    WakaTimeException(JSONObject obj) throws JSONException {
        super(obj.getString("error"));
    }
}
