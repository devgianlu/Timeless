package com.gianlu.timeless.api.models;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.gianlu.commonutils.CommonUtils;
import com.gianlu.commonutils.drawer.BaseDrawerProfile;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

public class User implements Serializable, BaseDrawerProfile {
    private static final User dummyUser = new User("", "", "", "", "");
    public final String email;
    public final String username;
    public final String id;
    private final String website;
    private final String full_name;

    public User(JSONObject obj) throws JSONException {
        id = obj.getString("id");
        email = CommonUtils.getStupidString(obj, "email");
        username = CommonUtils.getStupidString(obj, "username");
        full_name = CommonUtils.getStupidString(obj, "full_name");

        if (!obj.has("website")) website = null;
        else website = obj.getString("website");
    }

    private User(String email, String username, String id, String website, String full_name) {
        this.email = email;
        this.username = username;
        this.id = id;
        this.website = website;
        this.full_name = full_name;
    }

    @NotNull
    public static User dummyUser() {
        return dummyUser;
    }

    public boolean isDummyUser() {
        return this == dummyUser();
    }

    @Nullable
    public String getWebsite() {
        return website == null || website.isEmpty() ? null : website.trim();
    }

    @NonNull
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
    public String getPrimaryText(@NonNull Context context) {
        return getDisplayName();
    }

    @NonNull
    @Override
    public String getSecondaryText(@NonNull Context context) {
        return email;
    }
}
