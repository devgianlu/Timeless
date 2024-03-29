package com.gianlu.timeless.api.models;

import androidx.annotation.NonNull;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;

public class LoggedEntities extends ArrayList<LoggedEntity> {

    public LoggedEntities(JSONArray array) throws JSONException {
        super(array.length());
        for (int i = 0; i < array.length(); i++) add(new LoggedEntity(array.getJSONObject(i)));
    }

    private LoggedEntities() {
    }

    @NonNull
    public static LoggedEntities empty() {
        return new LoggedEntities();
    }
}
