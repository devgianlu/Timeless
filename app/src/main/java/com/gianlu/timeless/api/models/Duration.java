package com.gianlu.timeless.api.models;

import org.json.JSONException;
import org.json.JSONObject;

public class Duration {
    public final long time;
    public final long duration;
    public final String project;

    Duration(JSONObject obj) throws JSONException {
        time = obj.getLong("time");
        duration = obj.getLong("duration");
        project = obj.getString("project");
    }
}
