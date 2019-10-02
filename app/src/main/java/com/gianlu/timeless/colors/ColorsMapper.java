package com.gianlu.timeless.colors;

import androidx.annotation.ColorRes;
import androidx.annotation.NonNull;

public interface ColorsMapper {
    @ColorRes
    int getColor(@NonNull String val);
}
