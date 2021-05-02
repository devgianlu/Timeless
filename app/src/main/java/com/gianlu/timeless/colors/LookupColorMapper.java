package com.gianlu.timeless.colors;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;

import androidx.annotation.NonNull;

import com.gianlu.commonutils.CommonUtils;
import com.gianlu.timeless.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.EnumMap;

public final class LookupColorMapper extends ColorsMapper {
    private static final EnumMap<Type, LookupColorMapper> instances = new EnumMap<>(Type.class);
    private static final String TAG = LookupColorMapper.class.getSimpleName();
    private final ColorsMapper fallbackMapper = PersistentColorMapper.get(PersistentColorMapper.Type.OTHERS);
    private final JSONObject map;

    private LookupColorMapper(@NonNull Context context, @NonNull String name) throws IOException, JSONException {
        JSONObject obj = new JSONObject(CommonUtils.readEntirely(context.getResources().openRawResource(R.raw.colors)));
        map = obj.getJSONObject(name);
    }

    @NonNull
    public static ColorsMapper get(@NonNull Context context, @NonNull Type type) {
        LookupColorMapper mapper = instances.get(type);
        if (mapper == null) {
            try {
                mapper = new LookupColorMapper(context, type.name);
                instances.put(type, mapper);
            } catch (IOException | JSONException ex) {
                Log.e(TAG, "Failed loading lookup map for " + type, ex);
                return PersistentColorMapper.get(PersistentColorMapper.Type.OTHERS);
            }
        }

        return mapper;
    }

    @Override
    public int getColor(@NonNull Context context, @NonNull String val) {
        try {
            String colorHex = map.optString(val);
            if (!colorHex.isEmpty()) return Color.parseColor(colorHex);
        } catch (IllegalArgumentException ex) {
            Log.e(TAG, "Failed parsing color for " + val, ex);
        }

        return fallbackMapper.getColor(context, val);
    }

    public enum Type {
        LANGUAGES("languages"), EDITORS("editors"), OPERATING_SYSTEMS("operating_systems");

        private final String name;

        Type(String name) {
            this.name = name;
        }
    }
}
