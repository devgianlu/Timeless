package com.gianlu.timeless.Listing;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.gianlu.timeless.DurationsView;
import com.gianlu.timeless.Objects.Duration;
import com.gianlu.timeless.R;
import com.gianlu.timeless.Utils;

import java.util.List;

class DurationsViewHolder extends RecyclerView.ViewHolder {
    public final DurationsView durationsView;
    public final TextView title;
    public final ImageButton save;

    DurationsViewHolder(LayoutInflater inflater, ViewGroup parent) {
        super(inflater.inflate(R.layout.durations_card, parent, false));

        title = (TextView) itemView.findViewById(R.id.durationsCard_title);
        durationsView = (DurationsView) itemView.findViewById(R.id.durationsCard_view);
        save = (ImageButton) itemView.findViewById(R.id.durationsCard_save);
    }

    void bind(final Context context, final String title, List<Duration> durations, final CardsAdapter.ISaveChart handler) {
        this.title.setText(title);

        durationsView.setDurations(durations);

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    handler.onSaveRequested(durationsView, Utils.getFileName(title));
                } else {
                    handler.onWritePermissionRequested(new CardsAdapter.IPermissionRequest() {
                        @Override
                        public void onGranted() {
                            handler.onSaveRequested(durationsView, Utils.getFileName(title));
                        }
                    });
                }
            }
        });
    }
}
