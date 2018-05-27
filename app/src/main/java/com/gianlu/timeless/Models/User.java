package com.gianlu.timeless.Models;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.gianlu.commonutils.CommonUtils;
import com.gianlu.commonutils.Drawer.BaseDrawerProfile;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

public class User implements Serializable, BaseDrawerProfile {
    public final String email;
    public final String username;
    public final String id;
    private final String website;
    private final String full_name;

    public User(JSONObject obj) throws JSONException {
        id = obj.getString("id");
        website = obj.optString("website", null);
        email = CommonUtils.getStupidString(obj, "email");
        username = CommonUtils.getStupidString(obj, "username");
        full_name = CommonUtils.getStupidString(obj, "full_name");
    }

    @Nullable
    public String getWebsite() {
        return website == null || website.isEmpty() ? null : website.trim();
    }

    public String getDisplayName() {
        if (full_name != null && !full_name.isEmpty()) {
            return full_name;
        } else if (username != null && !username.isEmpty()) {
            return username;
        } else if (email != null && !email.isEmpty()) {
            return email;
        } else {
            return "Anonymous User";
        }
    }

    @NonNull
    @Override
    public String getInitials(Context context) {
        if (username == null && email == null && full_name == null)
            return "??";

        if (full_name != null) {
            StringBuilder letters = new StringBuilder();
            boolean getNext = false;
            for (int i = 0; i < full_name.length(); i++) {
                if (i == 0 || getNext) {
                    letters.append(full_name.charAt(i));
                    getNext = false;
                } else if (full_name.charAt(i) == ' ') {
                    getNext = true;
                }
            }
            return letters.toString();
        } else if (username != null) {
            return username.substring(0, 2);
        } else {
            return email.substring(0, 2);
        }
    }

    @NonNull
    @Override
    public String getProfileName(Context context) {
        return getDisplayName();
    }

    @NonNull
    @Override
    public String getSecondaryText(Context context) {
        return email;
    }
}
