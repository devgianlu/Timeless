package com.gianlu.timeless;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import com.gianlu.commonutils.Dialogs.ActivityWithDialog;
import com.gianlu.commonutils.Dialogs.DialogUtils;
import com.gianlu.commonutils.Preferences.Prefs;
import com.gianlu.commonutils.Toaster;
import com.gianlu.timeless.NetIO.WakaTime;

import androidx.annotation.NonNull;

public class GrantActivity extends ActivityWithDialog implements WakaTime.InitializationListener {
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

        builder = new WakaTime.Builder(this);

        Button grant = findViewById(R.id.grantActivity_grant);
        grant.setOnClickListener(v -> builder.startFlow());
    }

    @Override
    public void onWakatimeInitialized(@NonNull WakaTime instance) {
        dismissDialog();

        try {
            startActivity(new Intent(GrantActivity.this, LoadingActivity.class)
                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK));
            Prefs.putBoolean(PK.FIRST_RUN, false);
        } catch (ActivityNotFoundException ex) {
            Toaster.with(GrantActivity.this).message(R.string.failedCheckingWakatimePermissions).ex(ex).show();
        }
    }

    @Override
    public void onException(@NonNull Exception ex) {
        dismissDialog();
        Toaster.with(GrantActivity.this).message(R.string.failedCheckingWakatimePermissions).ex(ex).show();
    }
}
