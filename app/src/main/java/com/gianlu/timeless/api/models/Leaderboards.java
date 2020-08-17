package com.gianlu.timeless.api.models;

import androidx.annotation.NonNull;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class Leaderboards extends ArrayList<Leaderboards.Item> {
    public final int maxPages;

    public Leaderboards(@NonNull JSONObject obj) throws JSONException {
        maxPages = obj.getInt("total_pages");

        JSONArray data = obj.getJSONArray("data");
        for (int i = 0; i < data.length(); i++) add(new Item(data.getJSONObject(i)));
    }

    public static class Item {
        public final String id;
        public final String name;
        public final int members;

        private Item(@NonNull JSONObject obj) throws JSONException {
            id = obj.getString("id");
            name = obj.getString("name");
            members = obj.getInt("members_count");
        }
    }
}
