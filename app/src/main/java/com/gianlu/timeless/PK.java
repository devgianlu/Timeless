package com.gianlu.timeless;

import android.support.annotation.NonNull;

import com.gianlu.commonutils.Preferences.Prefs;

public enum PK implements Prefs.PrefKey {
    FIRST_RUN("firstRun"),
    /**
     * Refresh token
     */
    TOKEN("token"),
    CACHE_ENABLED("cacheEnabled");

    private final String key;

    PK(String key) {
        this.key = key;
    }

    @NonNull
    @Override
    public String getKey() {
        return key;
    }
}
