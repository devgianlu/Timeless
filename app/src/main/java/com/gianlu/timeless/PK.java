package com.gianlu.timeless;

import com.gianlu.commonutils.preferences.CommonPK;
import com.gianlu.commonutils.preferences.Prefs;

public final class PK extends CommonPK {
    public final static Prefs.Key FIRST_RUN = new Prefs.Key("firstRun");
    public final static Prefs.Key TOKEN = new Prefs.Key("token");
    public final static Prefs.KeyWithDefault<Boolean> CACHE_ENABLED = new Prefs.KeyWithDefault<>("cacheEnabled", true);
}
