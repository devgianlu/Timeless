package com.gianlu.timeless;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.gianlu.commonutils.CommonUtils;
import com.gianlu.commonutils.Preferences.Prefs;
import com.gianlu.commonutils.Toaster;
import com.gianlu.timeless.NetIO.WakaTime;

public class GrantActivity extends AppCompatActivity {

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        if (intent.getDataString() != null) {
            final ProgressDialog pd = CommonUtils.fastIndeterminateProgressDialog(this, R.string.checkingPermissions);
            CommonUtils.showDialog(this, pd);
            WakaTime.accessToken(this, intent.getDataString(), new WakaTime.OnAccessToken() {
                @Override
                public void onTokenAccepted() {
                    Prefs.putBoolean(GrantActivity.this, PKeys.FIRST_RUN, false);
                    pd.dismiss();
                    startActivity(new Intent(GrantActivity.this, LoadingActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK));
                }

                @Override
                public void onException(Throwable ex) {
                    pd.dismiss();
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
