package com.gianlu.timeless.Models;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Leader {
    public final int rank;
    public final User user;
    public final long total_seconds;
    public final long daily_average;
    public final Map<String, Long> languages;

    public Leader(JSONObject obj) throws JSONException {
        rank = obj.optInt("rank", -1);
        user = new User(obj.getJSONObject("user"));

        JSONObject running_total = obj.getJSONObject("running_total");
        daily_average = running_total.getLong("daily_average");
        total_seconds = running_total.getLong("total_seconds");

        JSONArray languagesArray = running_total.getJSONArray("languages");
        languages = new HashMap<>();
        for (int i = 0; i < languagesArray.length(); i++) {
            JSONObject lang = languagesArray.getJSONObject(i);
            languages.put(lang.getString("name"), lang.getLong("total_seconds"));
        }
    }

    public static List<Leader> fromJSON(JSONArray array) throws JSONException {
        List<Leader> leaders = new ArrayList<>();
        for (int i = 0; i < array.length(); i++)
            leaders.add(new Leader(array.getJSONObject(i)));

        return leaders;
    }
}
