package com.gianlu.timeless;

import com.gianlu.commonutils.CommonPK;
import com.gianlu.commonutils.Preferences.Prefs;

public final class PK extends CommonPK {
    public static Prefs.Key FIRST_RUN = new Prefs.Key("firstRun");
    public static Prefs.Key TOKEN = new Prefs.Key("token");
    public static Prefs.KeyWithDefault<Boolean> CACHE_ENABLED = new Prefs.KeyWithDefault<>("cacheEnabled", true);
}
