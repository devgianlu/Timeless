package com.gianlu.timeless;


import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.support.annotation.NonNull;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.view.MenuItem;

import com.gianlu.commonutils.AppCompatPreferenceActivity;
import com.gianlu.commonutils.AppCompatPreferenceFragment;
import com.gianlu.commonutils.BaseAboutFragment;
import com.gianlu.commonutils.CommonUtils;
import com.gianlu.commonutils.LogsActivity;

import java.util.List;

public class PreferencesActivity extends AppCompatPreferenceActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(R.string.preferences);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null)
            actionBar.setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            if (!super.onMenuItemSelected(featureId, item))
                NavUtils.navigateUpFromSameTask(this);
            return true;
        }
        return super.onMenuItemSelected(featureId, item);
    }

    @Override
    public void onHeaderClick(Header header, int position) {
        if (header.iconRes == R.drawable.ic_announcement_black_24dp) {
            startActivity(new Intent(this, LogsActivity.class));
            return;
        }

        super.onHeaderClick(header, position);
    }

    @Override
    public boolean onIsMultiPane() {
        return isXLargeTablet(this);
    }

    @Override
    public void onBuildHeaders(List<Header> target) {
        loadHeadersFromResource(R.xml.pref_headers, target);
    }

    protected boolean isValidFragment(String fragmentName) {
        return PreferenceFragment.class.getName().equals(fragmentName)
                || ThirdPartFragment.class.getName().equals(fragmentName)
                || AboutFragment.class.getName().equals(fragmentName);
    }

    public static class ThirdPartFragment extends AppCompatPreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.thrid_part_pref);
            getActivity().setTitle(R.string.third_part);
            setHasOptionsMenu(true);

            final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setPositiveButton(android.R.string.ok, null);

            findPreference("mpAndroidChart").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                public boolean onPreferenceClick(Preference preference) {
                    CommonUtils.showDialog(getActivity(), builder
                            .setTitle("MPAndroidChart")
                            .setMessage(R.string.mpAndroidChart_details));
                    return true;
                }
            });

            findPreference("materialDateRangePicker").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                public boolean onPreferenceClick(Preference preference) {
                    CommonUtils.showDialog(getActivity(), builder
                            .setTitle("MaterialDateRangePicker")
                            .setMessage(R.string.materialDateRangePicker_details));
                    return true;
                }
            });

            findPreference("scribejava").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                public boolean onPreferenceClick(Preference preference) {
                    CommonUtils.showDialog(getActivity(), builder
                            .setTitle("ScribeJava")
                            .setMessage(R.string.scribejava_details));
                    return true;
                }
            });

            findPreference("apacheLicense").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                public boolean onPreferenceClick(Preference preference) {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.apache.org/licenses/LICENSE-2.0")));
                    return true;
                }
            });

            findPreference("mitLicense").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                public boolean onPreferenceClick(Preference preference) {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://opensource.org/licenses/MIT")));
                    return true;
                }
            });
        }

        @Override
        protected Class getParent() {
            return PreferencesActivity.class;
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

        @Override
        protected Class getParent() {
            return PreferencesActivity.class;
        }
    }
}
