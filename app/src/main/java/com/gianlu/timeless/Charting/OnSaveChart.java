package com.gianlu.timeless.Charting;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;

import com.gianlu.timeless.Models.Project;

public interface OnSaveChart {
    void onWritePermissionRequested(@NonNull OnGrantedPermission listener);

    void onSaveRequested(@NonNull View chart, @NonNull String name);

    @Nullable
    Project getProject();
}
