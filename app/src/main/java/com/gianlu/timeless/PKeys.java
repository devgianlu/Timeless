package com.gianlu.timeless;

import com.gianlu.commonutils.Preferences.Prefs;

public enum PKeys implements Prefs.PrefKey {
    FIRST_RUN("firstRun"),
    TOKEN("token"),
    CACHE_ENABLED("cacheEnabled");

    private final String key;

    PKeys(String key) {
        this.key = key;
    }

    @Override
    public String getKey() {
        return key;
    }
}
