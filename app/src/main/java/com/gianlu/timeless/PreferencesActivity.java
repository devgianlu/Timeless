package com.gianlu.timeless;


import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.gianlu.commonutils.LogsActivity;
import com.gianlu.commonutils.Preferences.AppCompatPreferenceActivity;
import com.gianlu.commonutils.Preferences.AppCompatPreferenceFragment;
import com.gianlu.commonutils.Preferences.BaseAboutFragment;
import com.gianlu.commonutils.Preferences.BaseThirdPartProjectsFragment;
import com.gianlu.timeless.NetIO.WakaTime;

import java.util.List;

public class PreferencesActivity extends AppCompatPreferenceActivity {

    @Override
    public void onHeaderClick(Header header, int position) {
        if (header.iconRes == R.drawable.ic_announcement_black_24dp) {
            startActivity(new Intent(this, LogsActivity.class));
            return;
        }

        super.onHeaderClick(header, position);
    }

    @Override
    public void onBuildHeaders(List<Header> target) {
        loadHeadersFromResource(R.xml.pref_headers, target);
    }

    public static class NetworkFragment extends AppCompatPreferenceFragment {

        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.network_pref);
            getActivity().setTitle(R.string.network);
            setHasOptionsMenu(true);

            findPreference("cacheEnabled").setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    try {
                        WakaTime.get().cacheEnabledChanged();
                        return true;
                    } catch (WakaTime.ShouldGetAccessToken ex) {
                        ex.resolve(getActivity());
                        return false;
                    }
                }
            });
        }

        @Override
        protected Class getParent() {
            return PreferencesActivity.class;
        }
    }

    public static class ThirdPartFragment extends BaseThirdPartProjectsFragment {

        @Override
        protected Class getParent() {
            return PreferencesActivity.class;
        }

        @NonNull
        @Override
        protected ThirdPartProject[] getProjects() {
            return new ThirdPartProject[]{
                    new ThirdPartProject(R.string.mpAndroidChart, R.string.mpAndroidChart_details, ThirdPartProject.License.APACHE),
                    new ThirdPartProject(R.string.materialDateRangePicker, R.string.materialDateRangePicker_details, ThirdPartProject.License.APACHE),
                    new ThirdPartProject(R.string.scribejava, R.string.scribejava_details, ThirdPartProject.License.MIT)
            };
        }
    }

    public static class AboutFragment extends BaseAboutFragment {
        @Override
        protected int getAppNameRes() {
            return R.string.app_name;
        }

        @NonNull
        @Override
        protected String getPackageName() {
            return "com.gianlu.timeless";
        }

        @Nullable
        @Override
        protected Uri getOpenSourceUrl() {
            return null;
        }

        @Override
        protected Class getParent() {
            return PreferencesActivity.class;
        }
    }
}
