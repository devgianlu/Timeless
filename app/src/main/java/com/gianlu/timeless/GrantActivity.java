package com.gianlu.timeless;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.gianlu.timeless.NetIO.InvalidTokenException;
import com.gianlu.timeless.NetIO.WakaTime;

public class GrantActivity extends AppCompatActivity {

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        if (intent.getCategories().contains(Intent.CATEGORY_BROWSABLE) && intent.getDataString() != null) {
            WakaTime.getInstance().newAccessToken(this, intent.getDataString(), new WakaTime.INewAccessToken() {
                @Override
                public void onTokenAccepted() {
                    System.out.println("TOKEN ACCEPTED!");
                }

                @Override
                public void onTokenRejected(InvalidTokenException ex) {
                    System.out.println("TOKEN REJECTED: " + ex.getMessage());
                }

                @Override
                public void onException(Exception ex) {
                    ex.printStackTrace();
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
