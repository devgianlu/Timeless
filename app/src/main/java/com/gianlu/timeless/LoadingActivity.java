package com.gianlu.timeless;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.gianlu.commonutils.ConnectivityChecker;
import com.gianlu.commonutils.Logging;
import com.gianlu.commonutils.Toaster;
import com.gianlu.timeless.Models.User;
import com.gianlu.timeless.NetIO.WakaTime;
import com.gianlu.timeless.NetIO.WakaTimeException;

public class LoadingActivity extends AppCompatActivity {
    private Intent goTo;
    private boolean finished = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (PreferenceManager.getDefaultSharedPreferences(this).getBoolean("firstRun", true)) {
            startActivity(new Intent(this, GrantActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
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

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                finished = true;
                if (goTo != null) startActivity(goTo);
            }
        }, 1000);

        Logging.clearLogs(this);

        new Thread(new Runnable() {
            @Override
            public void run() {
                if (ConnectivityChecker.checkSync(LoadingActivity.this)) {
                    WakaTime.getInstance().refreshToken(LoadingActivity.this, new WakaTime.IRefreshToken() {
                        @Override
                        public void onRefreshed(WakaTime wakaTime) {
                            wakaTime.getCurrentUser(new WakaTime.IUser() {
                                @Override
                                public void onUser(User user) {
                                    goTo(MainActivity.class, user);
                                }

                                @Override
                                public void onException(Exception ex) {
                                    Toaster.show(LoadingActivity.this, Utils.Messages.FAILED_LOADING, ex);
                                    finish();
                                }

                                @Override
                                public void onInvalidToken(WakaTimeException ex) {
                                    Utils.invalidToken(LoadingActivity.this, ex);
                                }
                            });
                        }

                        @Override
                        public void onInvalidToken(Exception ex) {
                            Toaster.show(LoadingActivity.this, Utils.Messages.INVALID_TOKEN, ex);
                            deleteFile("token");
                            goTo(GrantActivity.class, null);
                        }

                        @Override
                        public void onException(Exception ex) {
                            Toaster.show(LoadingActivity.this, Utils.Messages.CANT_REFRESH_TOKEN, ex);
                            goTo(GrantActivity.class, null);
                        }
                    });
                } else {
                    Toaster.show(LoadingActivity.this, Toaster.Message.OFFLINE, new Runnable() {
                        @Override
                        public void run() {
                            finish();
                        }
                    });
                }
            }
        }).start();
    }

    private void goTo(Class goTo, @Nullable User user) {
        Intent intent = new Intent(LoadingActivity.this, goTo).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        if (user != null) intent.putExtra("user", user);
        if (finished) startActivity(intent);
        else this.goTo = intent;
    }
}
