package com.gianlu.timeless.Models;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class Summary {
    private static final SimpleDateFormat parser = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    public final LoggedEntities projects;
    public final LoggedEntities languages;
    public final LoggedEntities editors;
    public final LoggedEntities operating_systems;
    public final LoggedEntities entities;
    public final LoggedEntities branches;
    public long total_seconds;
    public long date;
    public int sumNumber;

    protected Summary() {
        total_seconds = 0;
        sumNumber = 0;
        date = -1;
        projects = LoggedEntities.empty();
        languages = LoggedEntities.empty();
        editors = LoggedEntities.empty();
        operating_systems = LoggedEntities.empty();
        entities = LoggedEntities.empty();
        branches = LoggedEntities.empty();
    }

    public Summary(JSONObject obj) throws JSONException, ParseException {
        total_seconds = obj.getJSONObject("grand_total").getLong("total_seconds");
        date = parser.parse(obj.getJSONObject("range").getString("date")).getTime();

        if (obj.has("projects"))
            projects = new LoggedEntities(obj.getJSONArray("projects"));
        else
            projects = LoggedEntities.empty();

        if (obj.has("branches"))
            branches = new LoggedEntities(obj.getJSONArray("branches"));
        else
            branches = LoggedEntities.empty();

        if (obj.has("entities"))
            entities = new LoggedEntities(obj.getJSONArray("entities"));
        else
            entities = LoggedEntities.empty();

        languages = new LoggedEntities(obj.getJSONArray("languages"));
        editors = new LoggedEntities(obj.getJSONArray("editors"));
        operating_systems = new LoggedEntities(obj.getJSONArray("operating_systems"));

        Collections.sort(entities, new LoggedEntity.TotalSecondsComparator());
    }

    public static float doTotalSecondsAverage(List<Summary> summaries) {
        long sum = 0;

        for (Summary summary : summaries)
            sum += summary.total_seconds;

        return (float) sum / (float) summaries.size();
    }
}
