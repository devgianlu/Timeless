package com.gianlu.timeless.charts;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;

import com.gianlu.timeless.Models.Project;

public interface OnSaveChart {
    @Nullable
    Project getProject();

    void saveImage(@NonNull View chart, @StringRes int title);
}
