package com.gianlu.timeless.Objects;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Duration {
    public final long time;
    public final long duration;
    public final String project;

    public Duration(JSONObject obj) throws JSONException {
        time = obj.getLong("time");
        duration = obj.getLong("duration");
        project = obj.getString("project");
    }

    public static List<Duration> filter(List<Duration> items, String project) {
        List<Duration> durations = new ArrayList<>();
        for (Duration item : items)
            if (Objects.equals(item.project, project))
                durations.add(item);

        return durations;
    }
}
