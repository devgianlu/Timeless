package com.gianlu.timeless.api.models;

import android.util.Pair;

import androidx.annotation.NonNull;

import java.util.Collections;
import java.util.Date;
import java.util.List;

public class GlobalSummary extends Summary {
    public final long total_seconds;
    public final long start;
    public final long end;
    public int days;

    GlobalSummary(@NonNull List<SingleSummary> summaries) {
        super();

        long total = 0;
        for (SingleSummary summary : summaries) {
            total += summary.total_seconds;
            LoggedEntity.sum(editors, summary.editors);
            LoggedEntity.sum(languages, summary.languages);
            LoggedEntity.sum(projects, summary.projects);
            LoggedEntity.sum(operating_systems, summary.operating_systems);
            LoggedEntity.sum(entities, summary.entities);
            LoggedEntity.sum(branches, summary.branches);
            LoggedEntity.sum(machines, summary.machines);
            days++;
        }

        Collections.sort(entities, new LoggedEntity.TotalSecondsComparator());

        this.total_seconds = total;
        this.start = summaries.get(0).date;
        this.end = summaries.get(summaries.size() - 1).date;
    }

    @NonNull
    public Pair<Date, Date> interval() {
        return new Pair<>(new Date(start), new Date(end));
    }
}
