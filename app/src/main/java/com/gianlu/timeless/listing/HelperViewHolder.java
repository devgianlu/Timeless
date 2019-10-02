package com.gianlu.timeless.listing;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.RecyclerView;

public abstract class HelperViewHolder extends RecyclerView.ViewHolder {
    private final Listener listener;

    HelperViewHolder(@NonNull Listener listener, @NonNull LayoutInflater inflater, @NonNull ViewGroup parent, @LayoutRes int res) {
        super(inflater.inflate(res, parent, false));
        this.listener = listener;
    }

    @NonNull
    protected Context getContext() {
        return itemView.getContext();
    }

    protected void showDialog(@NonNull DialogFragment fragment, @Nullable String tag) {
        listener.showDialog(fragment, tag);
    }

    protected void showDialog(@NonNull AlertDialog.Builder builder) {
        listener.showDialog(builder);
    }

    public interface Listener {
        void showDialog(@NonNull AlertDialog.Builder builder);

        void showDialog(@NonNull DialogFragment fragment, @Nullable String tag);
    }
}
