package com.gianlu.timeless.NetIO;

import org.json.JSONException;
import org.json.JSONObject;

public class LoggedEntity {
    public final String name;
    public final long total_seconds;
    public final float percent;
    public final String digital;
    public final String text;
    public final long hours;
    public final long minutes;

    public LoggedEntity(JSONObject obj) throws JSONException {
        name = obj.getString("name");
        total_seconds = obj.getLong("total_seconds");
        percent = (float) obj.getDouble("percent");
        digital = obj.getString("digital");
        text = obj.getString("text");
        hours = obj.getLong("hours");
        minutes = obj.getLong("minutes");
    }
}
