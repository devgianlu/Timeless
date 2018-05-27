package com.gianlu.timeless.Models;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.util.ArrayList;

public class Commits extends ArrayList<Commit> {
    public final Project project;
    public final int total_pages;
    public final int total;
    public final int page;
    public final int next_page;

    public Commits(JSONObject obj) throws JSONException, ParseException {
        JSONArray array = obj.getJSONArray("commits");
        for (int i = 0; i < array.length(); i++) add(new Commit(array.getJSONObject(i)));

        project = new Project(obj.getJSONObject("project"));

        total = obj.getInt("total");
        total_pages = obj.getInt("total_pages");
        page = obj.getInt("page");
        next_page = obj.optInt("next_page", -1);
    }
}
