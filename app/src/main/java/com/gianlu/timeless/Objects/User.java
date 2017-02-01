package com.gianlu.timeless.Objects;

import com.gianlu.timeless.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

public class User implements Serializable {
    public final String email;
    public final String username;
    public final String id;
    final String full_name;

    public User(JSONObject obj) throws JSONException {
        id = obj.getString("id");
        email = Utils.parseStupidNullJSON(obj, "email");
        username = Utils.parseStupidNullJSON(obj, "username");
        full_name = Utils.parseStupidNullJSON(obj, "full_name");
    }

    public String getDisplayName() {
        if (full_name != null && !full_name.isEmpty()) {
            return full_name;
        } else if (username != null && !username.isEmpty()) {
            return username;
        } else if (email != null && !email.isEmpty()) {
            return email;
        } else {
            return "Anonymous User";
        }
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
