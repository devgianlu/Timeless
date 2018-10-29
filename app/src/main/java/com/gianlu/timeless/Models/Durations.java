package com.gianlu.timeless.Models;


import com.gianlu.commonutils.CommonUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class Durations extends ArrayList<Duration> {
    public final List<String> branches;
    private final boolean isToday;

    public Durations(JSONObject obj, @Nullable Project project) throws JSONException {
        JSONArray array = obj.getJSONArray("data");
        for (int i = 0; i < array.length(); i++) {
            Duration duration = new Duration(array.getJSONObject(i));
            if (project == null || Objects.equals(duration.project, project.name)) add(duration);
        }

        branches = CommonUtils.toStringsList(obj.getJSONArray("branches"), true);

        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        long todayStart = cal.getTimeInMillis() / 1000;
        cal.setTimeInMillis(System.currentTimeMillis());
        cal.set(Calendar.HOUR, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        cal.set(Calendar.MILLISECOND, 999);
        long todayEnd = cal.getTimeInMillis() / 1000;

        boolean tmp = true;
        for (Duration duration : this) {
            if (duration.time <= todayStart || duration.time >= todayEnd) {
                tmp = false;
                break;
            }
        }

        isToday = tmp;
    }

    public boolean isToday() {
        return isToday;
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
