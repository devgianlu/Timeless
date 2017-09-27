package com.gianlu.timeless.Charting;

import android.support.annotation.Nullable;
import android.view.View;

import com.gianlu.timeless.Listing.CardsAdapter;
import com.gianlu.timeless.Models.Project;

public interface ISaveChart {
    void onWritePermissionRequested(CardsAdapter.IPermissionRequest handler);

    void onSaveRequested(View chart, String name);

    @Nullable
    Project getProject();
}
