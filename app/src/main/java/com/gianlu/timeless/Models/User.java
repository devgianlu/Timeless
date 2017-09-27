package com.gianlu.timeless.Models;

import android.content.Context;

import com.gianlu.commonutils.Drawer.BaseDrawerProfile;
import com.gianlu.timeless.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

public class User implements Serializable, BaseDrawerProfile {
    public final String email;
    public final String username;
    public final String id;
    private final String full_name;

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

    @Override
    public String getInitials(Context context) {
        if (username == null && email == null && full_name == null)
            return "??";

        if (full_name != null) {
            StringBuilder letters = new StringBuilder();
            boolean getNext = false;
            for (int i = 0; i < full_name.length(); i++) {
                if (i == 0 || getNext) {
                    letters.append(full_name.charAt(i));
                    getNext = false;
                } else if (full_name.charAt(i) == ' ') {
                    getNext = true;
                }
            }
            return letters.toString();
        } else if (username != null) {
            return username.substring(0, 2);
        } else {
            return email.substring(0, 2);
        }
    }

    @Override
    public String getProfileName(Context context) {
        return getDisplayName();
    }

    @Override
    public String getSecondaryText(Context context) {
        return email;
    }
}
