package com.gianlu.timeless.Objects;

import com.gianlu.timeless.Utils;

import org.json.JSONException;
import org.json.JSONObject;

// TODO
public class Commit {
    public final String message;
    public final String author_email;
    public final String author_name;
    public final String author_username;
    public final String html_url;

    public Commit(JSONObject obj) throws JSONException {
        message = obj.getString("message");
        html_url = obj.getString("html_url");
        author_email = Utils.parseStupidNullJSON(obj, "author_email");
        author_name = Utils.parseStupidNullJSON(obj, "author_name");
        author_username = Utils.parseStupidNullJSON(obj, "author_username");
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
