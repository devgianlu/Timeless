package com.gianlu.timeless.Charting;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.view.View;

import com.gianlu.timeless.Models.Project;

public interface OnSaveChart {
    @Nullable
    Project getProject();

    void saveImage(@NonNull View chart, @StringRes int title);
}
