package com.gianlu.timeless.NetIO;


import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

public class Project implements Serializable {
    public final String id;
    public final String name;

    public Project(JSONObject obj) throws JSONException {
        id = obj.getString("id");
        name = obj.getString("name");
    }
}
