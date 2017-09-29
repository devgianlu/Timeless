package com.gianlu.timeless.Charting;

import android.Manifest;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.Toast;

import com.gianlu.commonutils.CommonUtils;
import com.gianlu.commonutils.Toaster;
import com.gianlu.timeless.Listing.CardsAdapter;
import com.gianlu.timeless.R;
import com.gianlu.timeless.Utils;

import java.io.File;
import java.io.IOException;

public abstract class SaveChartFragment extends Fragment implements ISaveChart {
    private static final int REQUEST_CODE = 3453;
    private CardsAdapter.IPermissionRequest handler;

    @Override
    public final void onWritePermissionRequested(CardsAdapter.IPermissionRequest handler) {
        this.handler = handler;
        if (shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            CommonUtils.showDialog(getActivity(), new AlertDialog.Builder(getActivity())
                    .setTitle(R.string.writeExternalStorageRequest_title)
                    .setMessage(R.string.writeExternalStorageRequest_message)
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CODE);
                        }
                    }));
        } else {
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CODE);
        }
    }

    @Override
    public final void onSaveRequested(View chart, String name) {
        try {
            File dest = SaveChartUtils.save(getContext(), chart, getProject(), name);
            Toaster.show(getActivity(), getString(R.string.savedIn, dest.getPath()), Toast.LENGTH_LONG, null, null, null);
        } catch (IOException ex) {
            Toaster.show(getActivity(), Utils.Messages.FAILED_SAVING_CHART, ex);
        }
    }

    @Override
    public final void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (handler != null && requestCode == REQUEST_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) handler.onGranted();
            else Toaster.show(getActivity(), Utils.Messages.WRITE_DENIED);
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }
}
