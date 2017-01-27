package com.gianlu.timeless.Objects;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class Commits {
    public final List<Commit> commits;
    public final Project project;
    public final int total_pages;
    public final int total;
    public int page;
    public int next_page;

    public Commits(JSONObject obj) throws JSONException {
        JSONArray commitsArray = obj.getJSONArray("commits");
        commits = new ArrayList<>();
        for (int i = 0; i < commitsArray.length(); i++)
            commits.add(new Commit(commitsArray.getJSONObject(i)));

        project = new Project(obj.getJSONObject("project"));

        total = obj.getInt("total");
        total_pages = obj.getInt("total_pages");
        page = obj.getInt("page");
        next_page = obj.optInt("next_page", -1);
    }

    public void merge(Commits newCommits) {
        commits.addAll(newCommits.commits);

        page = newCommits.page;
        next_page = newCommits.next_page;
    }
}
