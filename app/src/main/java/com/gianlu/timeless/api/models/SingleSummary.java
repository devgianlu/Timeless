package com.gianlu.timeless.api.models;

import androidx.annotation.NonNull;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Locale;

public class SingleSummary extends Summary {
    public final long total_seconds;
    public final long date;

    private SingleSummary(LoggedEntities projects, LoggedEntities languages, LoggedEntities editors, LoggedEntities operating_systems, LoggedEntities entities, LoggedEntities branches, LoggedEntities machines, long total_seconds, long date) {
        super(projects, languages, editors, operating_systems, entities, branches, machines);
        this.total_seconds = total_seconds;
        this.date = date;
    }

    @NonNull
    public static SingleSummary parse(@NonNull JSONObject obj) throws JSONException, ParseException {
        SimpleDateFormat parser = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

        long total_seconds = obj.getJSONObject("grand_total").getLong("total_seconds");
        long date = parser.parse(obj.getJSONObject("range").getString("date")).getTime();

        LoggedEntities projects;
        if (obj.has("projects"))
            projects = new LoggedEntities(obj.getJSONArray("projects"));
        else
            projects = LoggedEntities.empty();

        LoggedEntities branches;
        if (obj.has("branches"))
            branches = new LoggedEntities(obj.getJSONArray("branches"));
        else
            branches = LoggedEntities.empty();

        LoggedEntities entities;
        if (obj.has("entities"))
            entities = new LoggedEntities(obj.getJSONArray("entities"));
        else
            entities = LoggedEntities.empty();

        LoggedEntities languages = new LoggedEntities(obj.getJSONArray("languages"));
        LoggedEntities editors = new LoggedEntities(obj.getJSONArray("editors"));
        LoggedEntities operating_systems = new LoggedEntities(obj.getJSONArray("operating_systems"));
        LoggedEntities machines = new LoggedEntities(obj.getJSONArray("machines"));

        Collections.sort(entities, new LoggedEntity.TotalSecondsComparator());
        return new SingleSummary(projects, languages, editors, operating_systems, entities, branches, machines, total_seconds, date);
    }
}
