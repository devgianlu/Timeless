package com.gianlu.timeless.NetIO;

import org.json.JSONException;
import org.json.JSONObject;

public class User {
    final String id;
    final String email;

    public User(JSONObject obj) throws JSONException {
        id = obj.getString("id");
        email = obj.getString("email");
    }
}
