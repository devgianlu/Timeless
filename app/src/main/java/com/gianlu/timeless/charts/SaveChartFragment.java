package com.gianlu.timeless.charts;

import android.Manifest;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import com.gianlu.commonutils.dialogs.FragmentWithDialog;
import com.gianlu.commonutils.permissions.AskPermission;
import com.gianlu.commonutils.ui.Toaster;
import com.gianlu.timeless.R;

import java.io.File;

public abstract class SaveChartFragment extends FragmentWithDialog implements OnSaveChart, AskPermission.Listener {
    private View chart;
    private int title;

    @Override
    public final void saveImage(@NonNull View chart, int title) {
        if (getActivity() == null) return;

        this.chart = chart;
        this.title = title;
        AskPermission.ask(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE, this);
    }

    @Override
    public final void permissionGranted(@NonNull String permission) {
        File image = SaveChartUtils.save(chart, title, getProject());
        if (image == null)
            showToast(Toaster.build().message(R.string.failedSavingImage));
        else showToast(Toaster.build().message(R.string.imageSavedTo, image.getAbsolutePath()));
    }

    @Override
    public final void permissionDenied(@NonNull String permission) {
        showToast(Toaster.build().message(R.string.writeDenied));

        chart = null;
        title = 0;
    }

    @Override
    public final void askRationale(@NonNull AlertDialog.Builder builder) {
        builder.setTitle(R.string.writeExternalStorageRequest_title)
                .setMessage(R.string.writeExternalStorageRequest_message);
    }
}
