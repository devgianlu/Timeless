package com.gianlu.timeless;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.gianlu.commonutils.Dialogs.ActivityWithDialog;
import com.gianlu.commonutils.Dialogs.DialogUtils;
import com.gianlu.commonutils.Preferences.Prefs;
import com.gianlu.commonutils.Toaster;
import com.gianlu.timeless.NetIO.WakaTime;

public class GrantActivity extends ActivityWithDialog {

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        if (intent.getDataString() != null) {
            showDialog(DialogUtils.progressDialog(this, R.string.checkingPermissions));
            WakaTime.accessToken(this, intent.getDataString(), new WakaTime.OnAccessToken() {
                @Override
                public void onTokenAccepted() {
                    Prefs.putBoolean(GrantActivity.this, PKeys.FIRST_RUN, false);
                    dismissDialog();
                    startActivity(new Intent(GrantActivity.this, LoadingActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK));
                }

                @Override
                public void onException(Throwable ex) {
                    dismissDialog();
                    Toaster.show(GrantActivity.this, Utils.Messages.CANT_CHECK_GRANT, ex);
                }
            });
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grant);

        Button grant = findViewById(R.id.grantActivity_grant);
        grant.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(WakaTime.authorizationUrl())));
            }
        });
    }
}
