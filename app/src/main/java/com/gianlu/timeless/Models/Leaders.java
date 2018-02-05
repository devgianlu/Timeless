package com.gianlu.timeless.Models;

import com.gianlu.commonutils.CommonUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class Leaders {
    public final List<Leader> leaders;
    public final Leader me;
    public final int maxPages;

    public Leaders(JSONObject obj) throws JSONException {
        leaders = CommonUtils.toTList(obj.getJSONArray("data"), Leader.class);
        me = new Leader(obj.getJSONObject("current_user"));
        maxPages = obj.getInt("total_pages");
    }
}
