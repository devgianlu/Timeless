package com.gianlu.timeless.Models;

import org.json.JSONException;
import org.json.JSONObject;

public class LeadersWithMe extends Leaders {
    public final Leader me;

    public LeadersWithMe(JSONObject obj) throws JSONException {
        super(obj);

        me = new Leader(obj.getJSONObject("current_user"));
    }
}
