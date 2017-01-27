package com.gianlu.timeless.Objects;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.Objects;

public class LoggedEntity {
    public final String name;
    public long total_seconds;

    public LoggedEntity(JSONObject obj) throws JSONException {
        name = obj.getString("name");
        total_seconds = obj.getLong("total_seconds");
    }

    public static void sum(List<LoggedEntity> parents, List<LoggedEntity> children) {
        for (LoggedEntity child : children)
            if (parents.contains(child))
                parents.get(parents.indexOf(child)).total_seconds += child.total_seconds;
            else
                parents.add(child);
    }

    public static float[] secondsToFloatArray(List<LoggedEntity> entities) {
        float[] array = new float[entities.size()];

        for (int i = 0; i < entities.size(); i++)
            array[i] = entities.get(i).total_seconds;

        return array;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof LoggedEntity) {
            LoggedEntity entity = (LoggedEntity) obj;
            return Objects.equals(entity.name, name);
        }

        return false;
    }
}
