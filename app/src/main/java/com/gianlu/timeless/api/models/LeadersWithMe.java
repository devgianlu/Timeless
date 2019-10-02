package com.gianlu.timeless.api.models;

import org.json.JSONException;
import org.json.JSONObject;

public class LeadersWithMe extends Leaders {
    public final Leader me;

    public LeadersWithMe(JSONObject obj) throws JSONException {
        super(obj);

        if (obj.isNull("current_user")) me = null;
        else me = new Leader(obj.getJSONObject("current_user"));
    }
}
