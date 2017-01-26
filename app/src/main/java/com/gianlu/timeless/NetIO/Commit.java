package com.gianlu.timeless.NetIO;

import org.json.JSONException;
import org.json.JSONObject;

// TODO
public class Commit {
    public final String message;

    public Commit(JSONObject obj) throws JSONException {
        message = obj.getString("message");
    }
}
