package com.gianlu.timeless.Models;

import java.util.Collections;
import java.util.List;

public class GlobalSummary extends Summary {

    public GlobalSummary(List<Summary> summaries) {
        super();

        for (Summary summary : summaries) {
            total_seconds += summary.total_seconds;
            LoggedEntity.sum(editors, summary.editors);
            LoggedEntity.sum(languages, summary.languages);
            LoggedEntity.sum(projects, summary.projects);
            LoggedEntity.sum(operating_systems, summary.operating_systems);
            LoggedEntity.sum(entities, summary.entities);
            LoggedEntity.sum(branches, summary.branches);
            sumNumber++;
        }

        Collections.sort(entities, new LoggedEntity.TotalSecondsComparator());
    }
}
