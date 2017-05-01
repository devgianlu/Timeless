package com.gianlu.timeless.Models;

import com.gianlu.timeless.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;

public class Commit {
    public final String message;
    public final String truncated_hash;
    public final long committer_date;
    public final String author_email;
    public final String author_name;
    public final String author_username;
    public final String html_url;

    public Commit(JSONObject obj) throws JSONException, ParseException {
        message = obj.getString("message").replace('\n', '\0');
        truncated_hash = obj.getString("truncated_hash");
        html_url = obj.getString("html_url");
        author_email = Utils.parseStupidNullJSON(obj, "author_email");
        author_name = Utils.parseStupidNullJSON(obj, "author_name");
        author_username = Utils.parseStupidNullJSON(obj, "author_username");
        committer_date = Utils.getISOParser().parse(obj.getString("committer_date")).getTime();
    }

    public String getAuthor() {
        if (author_name != null)
            return author_name;
        else if (author_username != null)
            return author_username;
        else if (author_email != null)
            return author_email;
        else
            return "Unknown";
    }
}
