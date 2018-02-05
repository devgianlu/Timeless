package com.gianlu.timeless.Models;

import android.support.annotation.Keep;

import org.json.JSONException;
import org.json.JSONObject;

public class Duration {
    public final long time;
    public final long duration;
    public final String project;

    @Keep
    public Duration(JSONObject obj) throws JSONException {
        time = obj.getLong("time");
        duration = obj.getLong("duration");
        project = obj.getString("project");
    }
}
