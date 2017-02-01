package com.gianlu.timeless;

import com.gianlu.timeless.Objects.User;

public class CurrentUser {
    private static User user;

    public static User get() {
        return user;
    }

    public static void set(User user) {
        CurrentUser.user = user;
    }
}
