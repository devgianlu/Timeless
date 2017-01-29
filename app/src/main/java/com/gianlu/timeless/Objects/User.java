package com.gianlu.timeless.Objects;

import com.gianlu.timeless.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

public class User implements Serializable {
    public final String email;
    public final String username;
    final String id;
    final String timezone;
    final String full_name;
    final long last_heartbeat;
    final String last_plugin;
    final String last_project;
    final String plan;
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
        username = Utils.parseStupidNullJSON(obj, "username");
        full_name = Utils.parseStupidNullJSON(obj, "full_name");

        last_heartbeat = Utils.parseWithCallback(Utils.ISOParser, obj.getString("last_heartbeat"), -1);
        created_at = Utils.parseWithCallback(Utils.ISOParser, obj.getString("created_at"), -1);
        modified_at = Utils.parseWithCallback(Utils.ISOParser, obj.getString("modified_at"), -1);

        email_public = obj.getBoolean("email_public");
        photo_public = obj.getBoolean("photo_public");
    }

    public String getInitials() {
        if (username == null && email == null && full_name == null)
            return "??";

        if (full_name != null) {
            String letters = "";
            boolean getNext = false;
            for (int i = 0; i < full_name.length(); i++) {
                if (i == 0 || getNext) {
                    letters += full_name.charAt(i);
                    getNext = false;
                } else if (full_name.charAt(i) == ' ') {
                    getNext = true;
                }
            }
            return letters;
        } else if (username != null) {
            return username.substring(0, 2);
        } else {
            return email.substring(0, 2);
        }
    }
}
