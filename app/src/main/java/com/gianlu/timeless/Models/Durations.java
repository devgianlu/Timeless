package com.gianlu.timeless.Models;


import android.support.annotation.Nullable;

import com.gianlu.commonutils.CommonUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

public class Durations {
    public final List<Duration> durations;
    public final List<String> branches;

    public Durations(JSONObject obj, @Nullable Project project) throws JSONException {
        branches = CommonUtils.toStringsList(obj.getJSONArray("branches"), true);
        durations = CommonUtils.toTList(obj.getJSONArray("data"), Duration.class);

        if (project != null) {
            Iterator<Duration> iterator = durations.listIterator();
            while (iterator.hasNext()) {
                if (!Objects.equals(iterator.next().project, project.name))
                    iterator.remove();
            }
        }
    }

    public static List<Duration> filter(List<Duration> durations, String projectName) {
        List<Duration> filtered = new ArrayList<>();
        for (Duration duration : durations)
            if (Objects.equals(duration.project, projectName))
                filtered.add(duration);

        return filtered;
    }
}
