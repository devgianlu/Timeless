package com.gianlu.timeless.api.models;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Objects;

public class Projects extends ArrayList<Project> {

    public Projects(JSONObject obj) throws JSONException {
        JSONArray array = obj.getJSONArray("data");
        for (int i = 0; i < array.length(); i++) add(new Project(array.getJSONObject(i)));
    }

    public void filterNoRepository() {
        Iterator<Project> iterator = iterator();
        while (iterator.hasNext()) {
            if (!iterator.next().hasRepository)
                iterator.remove();
        }
    }

    public int indexOfId(String id) {
        for (int i = 0; i < size(); i++)
            if (Objects.equals(get(i).id, id))
                return i;

        return -1;
    }

    public int indexOfName(String name) {
        for (int i = 0; i < size(); i++)
            if (Objects.equals(get(i).name, name))
                return i;

        return -1;
    }
}
