package com.gianlu.timeless.NetIO;

import org.json.JSONException;
import org.json.JSONObject;

public class WakaTimeException extends Exception {
    WakaTimeException(JSONObject obj) throws JSONException {
        super(obj.getString("error"));
    }
}
