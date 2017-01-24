package com.gianlu.timeless;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.gianlu.commonutils.CommonUtils;
import com.gianlu.timeless.NetIO.InvalidTokenException;
import com.gianlu.timeless.NetIO.WakaTime;

public class GrantActivity extends AppCompatActivity {

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        if (intent.getDataString() != null) {
            final ProgressDialog pd = CommonUtils.fastIndeterminateProgressDialog(this, R.string.checkingPermissions);
            CommonUtils.showDialog(this, pd);
            WakaTime.getInstance().newAccessToken(this, intent.getDataString(), new WakaTime.INewAccessToken() {
                @Override
                public void onTokenAccepted() {
                    PreferenceManager.getDefaultSharedPreferences(GrantActivity.this).edit().putBoolean("firstRun", false).apply();
                    pd.dismiss();
                    startActivity(new Intent(GrantActivity.this, MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK));
                }

                @Override
                public void onTokenRejected(InvalidTokenException ex) {
                    pd.dismiss();
                    CommonUtils.UIToast(GrantActivity.this, Utils.ToastMessages.TOKEN_REJECTED, ex);
                }

                @Override
                public void onException(Exception ex) {
                    pd.dismiss();
                    CommonUtils.UIToast(GrantActivity.this, Utils.ToastMessages.CANT_CHECK_GRANT, ex);
                }
            });
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grant);

        Button grant = (Button) findViewById(R.id.grantActivity_grant);
        grant.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(WakaTime.getInstance().getAuthorizationUrl())));
            }
        });
    }
}
