package com.gianlu.timeless.Models;

import java.util.List;

public abstract class Summary {
    public final LoggedEntities projects;
    public final LoggedEntities languages;
    public final LoggedEntities editors;
    public final LoggedEntities operating_systems;
    public final LoggedEntities entities;
    public final LoggedEntities branches;
    public final LoggedEntities machines;

    Summary(LoggedEntities projects, LoggedEntities languages, LoggedEntities editors, LoggedEntities operating_systems, LoggedEntities entities, LoggedEntities branches, LoggedEntities machines) {
        this.projects = projects;
        this.languages = languages;
        this.editors = editors;
        this.operating_systems = operating_systems;
        this.entities = entities;
        this.branches = branches;
        this.machines = machines;
    }

    Summary() {
        projects = LoggedEntities.empty();
        languages = LoggedEntities.empty();
        editors = LoggedEntities.empty();
        operating_systems = LoggedEntities.empty();
        entities = LoggedEntities.empty();
        branches = LoggedEntities.empty();
        machines = LoggedEntities.empty();
    }

    public static float doTotalSecondsAverage(List<SingleSummary> summaries) {
        long sum = 0;
        for (SingleSummary summary : summaries) sum += summary.total_seconds;
        return (float) sum / (float) summaries.size();
    }
}
