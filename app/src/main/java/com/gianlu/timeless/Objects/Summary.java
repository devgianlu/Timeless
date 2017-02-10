package com.gianlu.timeless.Objects;

import org.json.JSONArray;
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
    public long total_seconds;
    public long date;
    public int sumNumber;

    private Summary() {
        total_seconds = 0;
        sumNumber = 0;
        date = -1;
        projects = new ArrayList<>();
        languages = new ArrayList<>();
        editors = new ArrayList<>();
        operating_systems = new ArrayList<>();
        entities = new ArrayList<>();
    }

    public Summary(JSONObject obj) throws JSONException, ParseException {
        total_seconds = obj.getJSONObject("grand_total").getLong("total_seconds");

        date = parser.parse(obj.getJSONObject("range").getString("date")).getTime();

        projects = new ArrayList<>();
        if (obj.has("projects")) {
            JSONArray projectsArray = obj.getJSONArray("projects");
            for (int i = 0; i < projectsArray.length(); i++)
                projects.add(new LoggedEntity(projectsArray.getJSONObject(i)));
        }

        JSONArray languagesArray = obj.getJSONArray("languages");
        languages = new ArrayList<>();
        for (int i = 0; i < languagesArray.length(); i++)
            languages.add(new LoggedEntity(languagesArray.getJSONObject(i)));

        JSONArray editorsArray = obj.getJSONArray("editors");
        editors = new ArrayList<>();
        for (int i = 0; i < editorsArray.length(); i++)
            editors.add(new LoggedEntity(editorsArray.getJSONObject(i)));

        JSONArray operatingSystemsArray = obj.getJSONArray("operating_systems");
        operating_systems = new ArrayList<>();
        for (int i = 0; i < operatingSystemsArray.length(); i++)
            operating_systems.add(new LoggedEntity(operatingSystemsArray.getJSONObject(i)));

        JSONArray entitiesArray = obj.getJSONArray("entities");
        entities = new ArrayList<>();
        for (int i = 0; i < entitiesArray.length(); i++)
            entities.add(new LoggedEntity(entitiesArray.getJSONObject(i)));

        Collections.sort(entities, new LoggedEntity.TotalSecondsComparator());
    }

    public static List<Summary> fromJSON(String json) throws JSONException, ParseException {
        JSONArray array = new JSONObject(json).getJSONArray("data");

        List<Summary> summaries = new ArrayList<>();
        for (int i = 0; i < array.length(); i++)
            summaries.add(new Summary(array.getJSONObject(i)));

        return summaries;
    }

    public static Summary createRangeSummary(List<Summary> summaries) {
        Summary rangeSummary = new Summary();

        for (Summary summary : summaries) {
            rangeSummary.total_seconds += summary.total_seconds;
            LoggedEntity.sum(rangeSummary.editors, summary.editors);
            LoggedEntity.sum(rangeSummary.languages, summary.languages);
            LoggedEntity.sum(rangeSummary.projects, summary.projects);
            LoggedEntity.sum(rangeSummary.operating_systems, summary.operating_systems);
            LoggedEntity.sum(rangeSummary.entities, summary.entities);
            rangeSummary.sumNumber++;
        }

        Collections.sort(rangeSummary.entities, new LoggedEntity.TotalSecondsComparator());
        return rangeSummary;
    }
}
