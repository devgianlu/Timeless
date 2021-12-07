package com.gianlu.timeless.api.models;

import androidx.annotation.NonNull;

import com.gianlu.commonutils.CommonUtils;

import org.json.JSONException;
import org.json.JSONObject;

public class LifetimeStats {
    public final boolean upToDate;
    public final String project;
    public final Long totalSeconds;

    public LifetimeStats(@NonNull JSONObject obj, String projectName) throws JSONException {
        obj = obj.getJSONObject("data");

        upToDate = obj.getBoolean("is_up_to_date");
        String jsonProjectName = CommonUtils.optString(obj, "project");
        project = jsonProjectName != null ? jsonProjectName : projectName;
        totalSeconds = CommonUtils.optLong(obj, "total_seconds");
    }
}
