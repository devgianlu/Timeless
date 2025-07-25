package com.gianlu.timeless;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;

import com.gianlu.commonutils.dialogs.ActivityWithDialog;
import com.gianlu.commonutils.misc.FakeLoadingWithLogoView;
import com.gianlu.commonutils.network.ConnectivityChecker;
import com.gianlu.commonutils.preferences.Prefs;
import com.gianlu.commonutils.ui.OfflineActivity;
import com.gianlu.commonutils.ui.Toaster;
import com.gianlu.timeless.api.WakaTime;
import com.gianlu.timeless.api.models.User;

import java.io.IOException;

public class LoadingActivity extends ActivityWithDialog implements WakaTime.InitializationListener {
    private static final String TAG = LoadingActivity.class.getSimpleName();
    private FakeLoadingWithLogoView view;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Prefs.getBoolean(PK.FIRST_RUN, true)) {
            startActivity(new Intent(this, GrantActivity.class)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
            finish();
            return;
        }

        view = new FakeLoadingWithLogoView(this);
        view.setLogoRes(R.drawable.ic_launcher);

        setContentView(view);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null)
            actionBar.hide();

        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                | View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);

        view.startFakeAnimation(false);
        ConnectivityChecker.checkAsync(new ConnectivityChecker.OnCheck() {
            @Override
            public void goodToGo() {
                new WakaTime.Builder(LoadingActivity.this).alreadyAuthorized(LoadingActivity.this);
            }

            @Override
            public void offline() {
                view.endFakeAnimation(() -> OfflineActivity.startActivity(LoadingActivity.this, "Timeless", LoadingActivity.class), false);
            }
        });
    }

    private void start(@NonNull Class<?> goTo, @Nullable User user) {
        Intent intent = new Intent(LoadingActivity.this, goTo).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        if (user != null) intent.putExtra("user", user);
        startActivity(intent);
    }

    @Override
    public void onWakatimeInitialized(@NonNull WakaTime instance) {
        instance.getCurrentUser(this, new WakaTime.OnResult<User>() {
            @Override
            public void onResult(@NonNull User user) {
                view.endFakeAnimation(() -> start(MainActivity.class, user), false);
            }

            @Override
            public void onException(@NonNull Exception ex) {
                Log.e(TAG, "Failed getting user info.", ex);
                view.endFakeAnimation(() -> {
                    Toaster.with(LoadingActivity.this).message(R.string.failedLoading).show();
                    finish();
                }, false);
            }
        });
    }

    @Override
    public void onException(@NonNull Exception ex) {
        Log.e(TAG, "Failed refreshing token.", ex);
        view.endFakeAnimation(() -> {
            Toaster.with(this).message(R.string.failedRefreshingToken).show();
            if (ex instanceof IOException) {
                OfflineActivity.startActivity(this, "Timeless", LoadingActivity.class);
            } else if (ex instanceof WakaTime.MissingCredentialsException) {
                start(GrantActivity.class, null);
            }
        }, false);
    }
}
