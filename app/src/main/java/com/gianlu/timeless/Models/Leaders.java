package com.gianlu.timeless.Models;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class Leaders extends ArrayList<Leader> {
    public final Leader me;
    public final int maxPages;

    public Leaders(JSONObject obj) throws JSONException {
        JSONArray array = obj.getJSONArray("data");
        for (int i = 0; i < array.length(); i++) add(new Leader(array.getJSONObject(i)));

        me = new Leader(obj.getJSONObject("current_user"));
        maxPages = obj.getInt("total_pages");
    }
}
