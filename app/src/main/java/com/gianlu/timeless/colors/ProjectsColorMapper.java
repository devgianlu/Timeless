package com.gianlu.timeless.colors;

import androidx.annotation.ColorRes;
import androidx.annotation.NonNull;

import com.gianlu.commonutils.typography.MaterialColors;

import java.util.HashMap;
import java.util.Map;

public class ProjectsColorMapper implements ColorsMapper {
    private static final ProjectsColorMapper instance = new ProjectsColorMapper();
    private final Map<String, Integer> map = new HashMap<>(20);
    private final MaterialColors colors = MaterialColors.getShuffledInstance();

    @NonNull
    public static ProjectsColorMapper get() {
        return instance;
    }

    @ColorRes
    @Override
    public synchronized int getColor(@NonNull String val) {
        Integer color = map.get(val);
        if (color == null) {
            color = colors.next();
            map.put(val, color);
        }

        return color;
    }
}
