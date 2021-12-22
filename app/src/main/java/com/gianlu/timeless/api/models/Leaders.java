package com.gianlu.timeless.api.models;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class Leaders extends ArrayList<Leader> {
    private static final String TAG = Leaders.class.getSimpleName();
    public final int maxPages;

    public Leaders(JSONObject obj) throws JSONException {
        JSONArray array = obj.getJSONArray("data");
        for (int i = 0; i < array.length(); i++) {
            try {
                add(new Leader(array.getJSONObject(i)));
            } catch (JSONException ex) {
                Log.w(TAG, "Cannot parse leader at " + i, ex);
            }
        }

        maxPages = obj.getInt("total_pages");
    }
}
