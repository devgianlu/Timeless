package com.gianlu.timeless.Models;


import android.support.annotation.Keep;
import android.support.annotation.Nullable;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

public class Project implements Serializable {
    public final String id;
    public final String name;
    public final boolean hasRepository;

    @Keep
    public Project(JSONObject obj) throws JSONException {
        id = obj.getString("id");
        name = obj.getString("name");
        hasRepository = obj.optJSONObject("repository") != null;
    }

    @Nullable
    public static Project find(String id, List<Project> projects) {
        for (Project project : projects)
            if (Objects.equals(project.id, id))
                return project;

        return null;
    }
}
