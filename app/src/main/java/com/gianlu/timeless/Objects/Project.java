package com.gianlu.timeless.Objects;


import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

public class Project implements Serializable {
    public final String id;
    public final String name;
    public final boolean hasRepository;

    public Project(JSONObject obj) throws JSONException {
        id = obj.getString("id");
        name = obj.getString("name");
        hasRepository = obj.optJSONObject("repository") != null;
    }
}
