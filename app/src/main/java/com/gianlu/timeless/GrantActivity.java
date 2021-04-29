package com.gianlu.timeless;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;

import com.gianlu.commonutils.dialogs.ActivityWithDialog;
import com.gianlu.commonutils.dialogs.DialogUtils;
import com.gianlu.commonutils.preferences.Prefs;
import com.gianlu.commonutils.ui.Toaster;
import com.gianlu.timeless.api.WakaTime;

public class GrantActivity extends ActivityWithDialog implements WakaTime.InitializationListener {
    private static final String TAG = GrantActivity.class.getSimpleName();
    private WakaTime.Builder builder;

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        if (builder != null && intent.getDataString() != null) {
            showDialog(DialogUtils.progressDialog(this, R.string.checkingWakatimePermissions));
            builder.endFlow(intent.getDataString(), this);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grant);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null)
            actionBar.hide();

        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                | View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);

        builder = new WakaTime.Builder(this);

        Button grant = findViewById(R.id.grantActivity_grant);
        grant.setOnClickListener(v -> builder.startFlow());

        builder.apiUrl("https://wakapi.dev/api/compat/wakatime/v1/");
        builder.apiKey("API_KEY_HERE", this);
    }

    @Override
    public void onWakatimeInitialized(@NonNull WakaTime instance) {
        dismissDialog();

        try {
            startActivity(new Intent(this, LoadingActivity.class)
                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK));
            Prefs.putBoolean(PK.FIRST_RUN, false);
        } catch (ActivityNotFoundException ex) {
            Toaster.with(this).message(R.string.failedCheckingWakatimePermissions).show();
        }
    }

    @Override
    public void onException(@NonNull Exception ex) {
        dismissDialog();
        Log.e(TAG, "Failed checking permissions.", ex);
        Toaster.with(this).message(R.string.failedCheckingWakatimePermissions).show();
    }
}
