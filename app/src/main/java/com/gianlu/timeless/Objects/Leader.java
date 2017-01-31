package com.gianlu.timeless.Objects;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class Leader {

    public Leader(JSONObject obj) throws JSONException {
        // TODO: Parse leader
    }

    public static List<Leader> fromJSON(JSONArray array) throws JSONException {
        List<Leader> leaders = new ArrayList<>();
        for (int i = 0; i < array.length(); i++)
            leaders.add(new Leader(array.getJSONObject(i)));

        return leaders;
    }
}
