package com.gianlu.timeless.Models;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public class LoggedEntity {
    public final String name;
    public long total_seconds;

    @SuppressWarnings("unused")
    public LoggedEntity(JSONObject obj) throws JSONException {
        name = obj.getString("name");
        total_seconds = obj.getLong("total_seconds");
    }

    public LoggedEntity(LoggedEntity copy) {
        this.name = copy.name;
        this.total_seconds = copy.total_seconds;
    }

    public static void sum(List<LoggedEntity> parents, List<LoggedEntity> children) {
        for (LoggedEntity child : children)
            if (parents.contains(child))
                parents.get(parents.indexOf(child)).total_seconds += child.total_seconds;
            else
                parents.add(new LoggedEntity(child));
    }

    public static long sumSeconds(List<LoggedEntity> entities) {
        long sum = 0;
        for (LoggedEntity entity : entities)
            sum += entity.total_seconds;

        return sum;
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
            if (o1.total_seconds == o2.total_seconds) return 0;
            else if (o1.total_seconds > o2.total_seconds) return -1;
            else return 1;
        }
    }
}
