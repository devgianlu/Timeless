package com.gianlu.timeless.Models;


import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.gianlu.commonutils.CommonUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Durations extends ArrayList<Duration> {
    public final List<String> branches;

    public Durations(JSONObject obj, @Nullable Project project) throws JSONException {
        JSONArray array = obj.getJSONArray("data");
        for (int i = 0; i < array.length(); i++) {
            Duration duration = new Duration(array.getJSONObject(i));
            if (project == null || Objects.equals(duration.project, project.name)) add(duration);
        }

        branches = CommonUtils.toStringsList(obj.getJSONArray("branches"), true);
    }

    @NonNull
    public List<Duration> filter(String projectName) {
        List<Duration> filtered = new ArrayList<>();
        for (Duration duration : this)
            if (Objects.equals(duration.project, projectName))
                filtered.add(duration);

        return filtered;
    }
}
