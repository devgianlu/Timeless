package com.gianlu.timeless.Charting;

import android.Manifest;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.Toast;

import com.gianlu.commonutils.Dialogs.ActivityWithDialog;
import com.gianlu.commonutils.Toaster;
import com.gianlu.timeless.Listing.CardsAdapter;
import com.gianlu.timeless.R;
import com.gianlu.timeless.Utils;

import java.io.File;
import java.io.IOException;

public abstract class SaveChartAppCompatActivity extends ActivityWithDialog implements ISaveChart {
    private static final int REQUEST_CODE = 4534;
    private CardsAdapter.IPermissionRequest handler;

    @Override
    public final void onWritePermissionRequested(CardsAdapter.IPermissionRequest handler) {
        this.handler = handler;
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            showDialog(new AlertDialog.Builder(this)
                    .setTitle(R.string.writeExternalStorageRequest_title)
                    .setMessage(R.string.writeExternalStorageRequest_message)
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            ActivityCompat.requestPermissions(SaveChartAppCompatActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CODE);
                        }
                    }));
        } else {
            ActivityCompat.requestPermissions(SaveChartAppCompatActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CODE);
        }
    }

    @Override
    public final void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (handler != null && requestCode == REQUEST_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) handler.onGranted();
            else Toaster.show(this, Utils.Messages.WRITE_DENIED);
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @Override
    public final void onSaveRequested(View chart, String name) {
        try {
            File dest = SaveChartUtils.save(this, chart, getProject(), name);
            Toaster.show(this, getString(R.string.savedIn, dest.getPath()), Toast.LENGTH_LONG, null, null, null);
        } catch (IOException ex) {
            Toaster.show(this, Utils.Messages.FAILED_SAVING_CHART, ex);
        }
    }
}
