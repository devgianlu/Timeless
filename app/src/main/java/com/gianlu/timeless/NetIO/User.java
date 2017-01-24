package com.gianlu.timeless.NetIO;

import com.gianlu.timeless.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class User {
    final String id;
    final String email;
    final String timezone;
    final long last_heartbeat;
    final String last_plugin;
    final String last_project;
    final String plan;
    final String username;
    final boolean email_public;
    final boolean photo_public;
    final long created_at;
    final long modified_at;

    public User(JSONObject obj) throws JSONException {
        id = obj.getString("id");
        email = obj.getString("email");
        timezone = obj.getString("timezone");
        last_plugin = obj.getString("last_plugin");
        last_project = obj.getString("last_project");
        plan = obj.getString("plan");
        username = obj.getString("username");

        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault());
        last_heartbeat = Utils.parseWithCallback(formatter, obj.getString("last_heartbeat"), -1);
        created_at = Utils.parseWithCallback(formatter, obj.getString("created_at"), -1);
        modified_at = Utils.parseWithCallback(formatter, obj.getString("modified_at"), -1);

        email_public = obj.getBoolean("email_public");
        photo_public = obj.getBoolean("photo_public");
    }
}
