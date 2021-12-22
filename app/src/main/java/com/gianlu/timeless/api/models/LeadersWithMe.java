package com.gianlu.timeless.api.models;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

public class LeadersWithMe extends Leaders {
    private static final String TAG = LeadersWithMe.class.getSimpleName();
    public final Leader me;

    public LeadersWithMe(JSONObject obj) throws JSONException {
        super(obj);

        if (obj.isNull("current_user")) {
            me = null;
        } else {
            Leader me = null;
            try {
                me = new Leader(obj.getJSONObject("current_user"));
            } catch (JSONException ex) {
                Log.w(TAG, "Failed parsing current_user leader!", ex);
            }

            this.me = me;
        }
    }
}
