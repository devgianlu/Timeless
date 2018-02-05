package com.gianlu.timeless.Models;


import com.gianlu.commonutils.CommonUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class Summaries {
    public final List<Summary> summaries;
    public final GlobalSummary globalSummary;
    public final List<String> availableBranches;
    public final List<String> selectedBranches;

    public Summaries(JSONObject obj) throws JSONException {
        summaries = CommonUtils.toTList(obj.getJSONArray("data"), Summary.class);
        globalSummary = new GlobalSummary(summaries);

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
