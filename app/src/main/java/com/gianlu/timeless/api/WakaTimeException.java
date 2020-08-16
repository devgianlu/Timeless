package com.gianlu.timeless.api;

import androidx.annotation.NonNull;

import org.json.JSONException;
import org.json.JSONObject;

public class WakaTimeException extends Exception {
    WakaTimeException(@NonNull JSONObject obj) throws JSONException {
        super(obj.getString("error"));
    }
}
