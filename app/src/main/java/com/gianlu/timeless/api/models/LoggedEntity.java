package com.gianlu.timeless.api.models;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public class LoggedEntity {
    public final String name;
    public long total_seconds;

    public LoggedEntity(JSONObject obj) throws JSONException {
        name = obj.getString("name");
        total_seconds = obj.getLong("total_seconds");
    }

    public LoggedEntity(LoggedEntity copy) {
        this.name = copy.name;
        this.total_seconds = copy.total_seconds;
    }

    public static void sum(List<LoggedEntity> parents, List<LoggedEntity> children) {
        for (LoggedEntity child : children) {
            if (parents.contains(child))
                parents.get(parents.indexOf(child)).total_seconds += child.total_seconds;
            else
                parents.add(new LoggedEntity(child));
        }
    }

    @Nullable
    public static LoggedEntity find(@NonNull List<LoggedEntity> entities, @NonNull String name) {
        for (LoggedEntity entity : entities)
            if (Objects.equals(entity.name, name))
                return entity;

        return null;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof LoggedEntity) {
            LoggedEntity entity = (LoggedEntity) obj;
            return Objects.equals(entity.name, name);
        }

        return false;
    }

    public static class TotalSecondsComparator implements Comparator<LoggedEntity> {
        @Override
        public int compare(LoggedEntity o1, LoggedEntity o2) {
            return Long.compare(o2.total_seconds, o1.total_seconds);
        }
    }
}
