package com.gianlu.timeless.Models;

import android.support.annotation.Keep;

import com.gianlu.commonutils.CommonUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class Summary {
    private static final SimpleDateFormat parser = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    public final List<LoggedEntity> projects;
    public final List<LoggedEntity> languages;
    public final List<LoggedEntity> editors;
    public final List<LoggedEntity> operating_systems;
    public final List<LoggedEntity> entities;
    public final List<LoggedEntity> branches;
    public long total_seconds;
    public long date;
    public int sumNumber;

    protected Summary() {
        total_seconds = 0;
        sumNumber = 0;
        date = -1;
        projects = new ArrayList<>();
        languages = new ArrayList<>();
        editors = new ArrayList<>();
        operating_systems = new ArrayList<>();
        entities = new ArrayList<>();
        branches = new ArrayList<>();
    }

    @Keep
    public Summary(JSONObject obj) throws JSONException, ParseException {
        total_seconds = obj.getJSONObject("grand_total").getLong("total_seconds");

        date = parser.parse(obj.getJSONObject("range").getString("date")).getTime();

        if (obj.has("projects"))
            projects = CommonUtils.toTList(obj.getJSONArray("projects"), LoggedEntity.class);
        else projects = new ArrayList<>();

        if (obj.has("branches"))
            branches = CommonUtils.toTList(obj.getJSONArray("branches"), LoggedEntity.class);
        else branches = new ArrayList<>();

        languages = CommonUtils.toTList(obj.getJSONArray("languages"), LoggedEntity.class);
        editors = CommonUtils.toTList(obj.getJSONArray("editors"), LoggedEntity.class);
        operating_systems = CommonUtils.toTList(obj.getJSONArray("operating_systems"), LoggedEntity.class);
        entities = CommonUtils.toTList(obj.getJSONArray("entities"), LoggedEntity.class);

        Collections.sort(entities, new LoggedEntity.TotalSecondsComparator());
    }

    public static float doTotalSecondsAverage(List<Summary> summaries) {
        long sum = 0;

        for (Summary summary : summaries)
            sum += summary.total_seconds;

        return (float) sum / (float) summaries.size();
    }
}
