package com.gianlu.timeless.Models;

import android.support.annotation.Keep;
import android.support.annotation.NonNull;

import com.gianlu.commonutils.CommonUtils;
import com.gianlu.timeless.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.text.ParseException;

public class Commit implements Serializable {
    public final String message;
    public final String hash;
    public final long committer_date;
    public final String author_email;
    public final long total_seconds;
    public final String author_name;
    public final String author_username;
    public final String html_url;
    public final String ref;

    @Keep
    public Commit(JSONObject obj) throws JSONException, ParseException {
        message = obj.getString("message").replace("\n", " ");
        total_seconds = obj.getLong("total_seconds");
        hash = obj.getString("hash");
        html_url = obj.getString("html_url");
        ref = CommonUtils.getStupidString(obj, "ref");
        author_email = CommonUtils.getStupidString(obj, "author_email");
        author_name = CommonUtils.getStupidString(obj, "author_name");
        author_username = CommonUtils.getStupidString(obj, "author_username");
        committer_date = Utils.getISOParser().parse(obj.getString("committer_date")).getTime();
    }

    @NonNull
    public String truncated_hash() {
        return hash.substring(0, 8);
    }

    @NonNull
    public String getAuthor() {
        if (author_name != null) return author_name;
        else if (author_username != null) return author_username;
        else if (author_email != null) return author_email;
        else return "Unknown";
    }
}
