package com.gianlu.timeless.Models;


import com.gianlu.commonutils.CommonUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

public class Summaries extends ArrayList<SingleSummary> {
    public final GlobalSummary globalSummary;
    public final List<String> availableBranches;
    public final List<String> selectedBranches;

    public Summaries(JSONObject obj) throws JSONException, ParseException {
        JSONArray array = obj.getJSONArray("data");
        for (int i = 0; i < array.length(); i++) add(SingleSummary.parse(array.getJSONObject(i)));

        globalSummary = new GlobalSummary(this);

        if (obj.has("available_branches"))
            availableBranches = CommonUtils.toStringsList(obj.getJSONArray("available_branches"), true);
        else
            availableBranches = null;

        if (obj.has("branches"))
            selectedBranches = CommonUtils.toStringsList(obj.getJSONArray("branches"), true);
        else
            selectedBranches = null;
    }
}
