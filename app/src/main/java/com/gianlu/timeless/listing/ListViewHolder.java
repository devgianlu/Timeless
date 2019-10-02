package com.gianlu.timeless.listing;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.StringRes;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.gianlu.timeless.R;
import com.gianlu.timeless.activities.projects.FilesAdapter;
import com.gianlu.timeless.api.models.LoggedEntities;

class ListViewHolder extends RecyclerView.ViewHolder {
    private final TextView title;
    private final RecyclerView list;

    ListViewHolder(LayoutInflater inflater, ViewGroup parent) {
        super(inflater.inflate(R.layout.card_list, parent, false));

        title = itemView.findViewById(R.id.listCard_title);
        list = itemView.findViewById(R.id.listCard_list);
    }

    void bind(@StringRes int title, LoggedEntities entities) {
        this.title.setText(title);

        list.setLayoutManager(new LinearLayoutManager(list.getContext(), RecyclerView.VERTICAL, false));
        list.setAdapter(new FilesAdapter(list.getContext(), entities));
    }
}
