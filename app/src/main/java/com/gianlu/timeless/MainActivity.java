package com.gianlu.timeless;

import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;

import com.gianlu.timeless.NetIO.InvalidTokenException;
import com.gianlu.timeless.NetIO.User;
import com.gianlu.timeless.NetIO.WakaTime;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (PreferenceManager.getDefaultSharedPreferences(this).getBoolean("firstRun", true)) {
            startActivity(new Intent(MainActivity.this, GrantActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
            return;
        }

        WakaTime.getInstance().getCurrentUser(this, new WakaTime.IUser() {
            @Override
            public void onUser(User user) {
                System.out.println(user);
            }

            @Override
            public void onException(Exception ex) {
                if (ex instanceof InvalidTokenException) {
                    startActivity(new Intent(MainActivity.this, GrantActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
                }

                ex.printStackTrace();
            }
        });
    }
}
