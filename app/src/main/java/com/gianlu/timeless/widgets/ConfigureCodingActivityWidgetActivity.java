package com.gianlu.timeless.widgets;

import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.gianlu.commonutils.dialogs.ActivityWithDialog;
import com.gianlu.timeless.R;
import com.gianlu.timeless.api.WakaTime;
import com.gianlu.timeless.widgets.CodingActivityWidgetProvider.WidgetOptions;
import com.google.android.material.button.MaterialButtonToggleGroup;

public class ConfigureCodingActivityWidgetActivity extends ActivityWithDialog {
    private static final String TAG = ConfigureCodingActivityWidgetActivity.class.getSimpleName();
    private MaterialButtonToggleGroup rangePick;
    private int appWidgetId;
    private WidgetOptions options;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setResult(RESULT_CANCELED);
        setContentView(R.layout.activity_configure_coding_activity_widget);

        appWidgetId = getIntent().getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish();
            return;
        }

        options = CodingActivityWidgetProvider.loadWidgetOptions(appWidgetId);

        rangePick = findViewById(R.id.configureCodingActivityWidget_range);
        switch (options.range) {
            default:
            case TODAY:
                rangePick.check(R.id.configureCodingActivityWidget_range_today);
                break;
            case LAST_7_DAYS:
                rangePick.check(R.id.configureCodingActivityWidget_range_lastWeek);
                break;
            case LAST_30_DAYS:
                rangePick.check(R.id.configureCodingActivityWidget_range_lastMonth);
                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.confiure_conding_activity_widget, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.configureCodingActivityWidget_done:
                done();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void done() {
        if (options == null)
            return;

        WakaTime.Range range;
        switch (rangePick.getCheckedButtonId()) {
            default:
            case R.id.configureCodingActivityWidget_range_today:
                range = WakaTime.Range.TODAY;
                break;
            case R.id.configureCodingActivityWidget_range_lastWeek:
                range = WakaTime.Range.LAST_7_DAYS;
                break;
            case R.id.configureCodingActivityWidget_range_lastMonth:
                range = WakaTime.Range.LAST_30_DAYS;
                break;
        }

        options.range = range;
        CodingActivityWidgetProvider.saveWidgetOptions(appWidgetId, options);

        showProgress(R.string.loadingData);
        new WakaTime.Builder(this).alreadyAuthorized(new WakaTime.InitializationListener() {
            @Override
            public void onWakatimeInitialized(@NonNull WakaTime instance) {
                CodingActivityWidgetProvider.performWidgetUpdate(instance, ConfigureCodingActivityWidgetActivity.this, appWidgetId, () -> {
                    dismissDialog();

                    setResult(RESULT_OK, new Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId));
                    finish();
                });
            }

            @Override
            public void onException(@NonNull Exception ex) {
                Log.e(TAG, "Failed initializing WakaTime.", ex);
                dismissDialog();
                finish();
            }
        });
    }
}
