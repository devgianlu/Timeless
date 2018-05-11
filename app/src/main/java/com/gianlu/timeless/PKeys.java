package com.gianlu.timeless;

import android.support.annotation.NonNull;

import com.gianlu.commonutils.Preferences.Prefs;

public enum PKeys implements Prefs.PrefKey {
    FIRST_RUN("firstRun"),
    TOKEN("token"),
    CACHE_ENABLED("cacheEnabled");

    private final String key;

    PKeys(String key) {
        this.key = key;
    }

    @NonNull
    @Override
    public String getKey() {
        return key;
    }
}
