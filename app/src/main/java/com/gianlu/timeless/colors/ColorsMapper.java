package com.gianlu.timeless.colors;

import android.content.Context;

import androidx.annotation.ColorInt;
import androidx.annotation.ColorRes;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

public abstract class ColorsMapper {

    @ColorRes
    public int getColorRes(@NonNull String val) {
        return 0;
    }

    @ColorInt
    public int getColor(@NonNull Context context, @NonNull String val) {
        return ContextCompat.getColor(context, getColorRes(val));
    }
}
