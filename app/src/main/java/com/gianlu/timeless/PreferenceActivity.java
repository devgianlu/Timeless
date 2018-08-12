package com.gianlu.timeless;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.gianlu.commonutils.Preferences.BasePreferenceActivity;
import com.gianlu.commonutils.Preferences.BasePreferenceFragment;
import com.gianlu.commonutils.Preferences.MaterialAboutPreferenceItem;
import com.yarolegovich.mp.MaterialCheckboxPreference;

import java.util.Collections;
import java.util.List;

public class PreferenceActivity extends BasePreferenceActivity {
    @NonNull
    @Override
    protected List<MaterialAboutPreferenceItem> getPreferencesItems() {
        return Collections.singletonList(new MaterialAboutPreferenceItem(R.string.network, R.drawable.baseline_wifi_24, NetworkFragment.class));
    }

    @Override
    protected int getAppIconRes() {
        return R.mipmap.ic_launcher;
    }

    @Override
    protected boolean hasTutorial() {
        return false;
    }

    @Nullable
    @Override
    protected String getOpenSourceUrl() {
        return null;
    }

    public static class NetworkFragment extends BasePreferenceFragment {

        @Override
        protected void buildPreferences(@NonNull Context context) {
            MaterialCheckboxPreference cache = new MaterialCheckboxPreference.Builder(context)
                    .key(PK.CACHE_ENABLED.getKey())
                    .defaultValue(true)
                    .build();
            cache.setTitle(R.string.enableCache);
            cache.setSummary(R.string.enableCacheSummary);
            addPreference(cache);
        }

        @Override
        public int getTitleRes() {
            return R.string.network;
        }
    }
}
