package com.gianlu.timeless;

import com.gianlu.commonutils.preferences.CommonPK;
import com.gianlu.commonutils.preferences.Prefs;

public final class PK extends CommonPK {
    public final static Prefs.Key FIRST_RUN = new Prefs.Key("firstRun");
    public final static Prefs.Key TOKEN_RAW = new Prefs.Key("rawToken");
    public final static Prefs.Key TOKEN_CREATED_AT = new Prefs.Key("tokenCreatedAt");
    public final static Prefs.KeyWithDefault<Boolean> CACHE_ENABLED = new Prefs.KeyWithDefault<>("cacheEnabled", true);
    public final static Prefs.Key WIDGETS_CONFIG = new Prefs.Key("widgetsConfig");
    public final static Prefs.Key API_URL = new Prefs.Key("apiUrl");
    public final static Prefs.Key API_KEY = new Prefs.Key("apiKey");
}
