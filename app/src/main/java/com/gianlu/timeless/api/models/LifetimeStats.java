package com.gianlu.timeless.api.models;

import androidx.annotation.NonNull;

import com.gianlu.commonutils.CommonUtils;

import org.json.JSONException;
import org.json.JSONObject;

public class LifetimeStats {
    public final boolean upToDate;
    public final String project;
    public final Long totalSeconds;

    public LifetimeStats(@NonNull JSONObject obj) throws JSONException {
        obj = obj.getJSONObject("data");

        upToDate = obj.getBoolean("is_up_to_date");
        project = CommonUtils.optString(obj, "project");
        totalSeconds = CommonUtils.optLong(obj, "total_seconds");
    }
}
