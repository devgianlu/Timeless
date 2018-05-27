package com.gianlu.timeless.Models;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Objects;

public class Projects extends ArrayList<Project> {

    public Projects(JSONObject obj) throws JSONException {
        JSONArray array = obj.getJSONArray("data");
        for (int i = 0; i < array.length(); i++) add(new Project(array.getJSONObject(i)));
    }

    public int indexOf(String id) {
        for (int i = 0; i < size(); i++)
            if (Objects.equals(get(i).id, id))
                return i;

        return -1;
    }
}
