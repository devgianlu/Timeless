package com.gianlu.timeless.colors;

import androidx.annotation.ColorRes;
import androidx.annotation.NonNull;

import com.gianlu.commonutils.typography.MaterialColors;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

public final class PersistentColorMapper extends ColorsMapper {
    private static final EnumMap<Type, PersistentColorMapper> instances = new EnumMap<>(Type.class);
    private final Map<String, Integer> map = new HashMap<>(20);
    private final MaterialColors colors = MaterialColors.getShuffledInstance();

    @NonNull
    public static ColorsMapper get(@NonNull Type type) {
        PersistentColorMapper mapper = instances.get(type);
        if (mapper == null) {
            mapper = new PersistentColorMapper();
            instances.put(type, mapper);
        }

        return mapper;
    }

    @ColorRes
    @Override
    public synchronized int getColorRes(@NonNull String val) {
        Integer color = map.get(val);
        if (color == null) {
            color = colors.next();
            map.put(val, color);
        }

        return color;
    }

    public enum Type {
        MACHINES, PROJECTS, BRANCHES, OTHERS
    }
}
