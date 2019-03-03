package com.gianlu.timeless;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;

import com.gianlu.commonutils.ConnectivityChecker;
import com.gianlu.commonutils.Dialogs.ActivityWithDialog;
import com.gianlu.commonutils.OfflineActivity;
import com.gianlu.commonutils.Preferences.Prefs;
import com.gianlu.commonutils.Toaster;
import com.gianlu.timeless.Models.User;
import com.gianlu.timeless.NetIO.WakaTime;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;

public class LoadingActivity extends ActivityWithDialog implements WakaTime.InitializationListener {
    private Intent goTo;
    private boolean finished = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Prefs.getBoolean(PK.FIRST_RUN, true)) {
            startActivity(new Intent(this, GrantActivity.class)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
            finish();
            return;
        }

        setContentView(R.layout.activity_loading);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null)
            actionBar.hide();

        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);

        new Handler().postDelayed(() -> {
            finished = true;
            if (goTo != null) startActivity(goTo);
        }, 1000);

        ConnectivityChecker.checkAsync(new ConnectivityChecker.OnCheck() {
            @Override
            public void goodToGo() {
                new WakaTime.Builder(LoadingActivity.this).alreadyAuthorized(LoadingActivity.this);
            }

            @Override
            public void offline() {
                OfflineActivity.startActivity(LoadingActivity.this, LoadingActivity.class);
            }
        });
    }

    private void goTo(Class goTo, @Nullable User user) {
        Intent intent = new Intent(LoadingActivity.this, goTo).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        if (user != null) intent.putExtra("user", user);
        if (finished) startActivity(intent);
        else this.goTo = intent;
    }

    @Override
    public void onWakatimeInitialized(@NonNull WakaTime instance) {
        instance.getCurrentUser(this, new WakaTime.OnResult<User>() {
            @Override
            public void onResult(@NonNull User user) {
                goTo(MainActivity.class, user);
            }

            @Override
            public void onException(@NonNull Exception ex) {
                Toaster.with(LoadingActivity.this).message(R.string.failedLoading).ex(ex).show();
                finish();
            }
        });
    }

    @Override
    public void onException(@NonNull Exception ex) {
        Toaster.with(LoadingActivity.this).message(R.string.failedRefreshingToken).ex(ex).show();
        goTo(GrantActivity.class, null);
    }
}
