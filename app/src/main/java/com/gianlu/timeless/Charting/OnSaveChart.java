package com.gianlu.timeless.Charting;

import android.view.View;

import com.gianlu.timeless.Models.Project;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;

public interface OnSaveChart {
    @Nullable
    Project getProject();

    void saveImage(@NonNull View chart, @StringRes int title);
}
