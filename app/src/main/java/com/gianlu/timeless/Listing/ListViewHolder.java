package com.gianlu.timeless.Listing;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;

import com.gianlu.timeless.Activities.Projects.FilesAdapter;
import com.gianlu.timeless.Models.LoggedEntities;
import com.gianlu.timeless.R;

import androidx.annotation.StringRes;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

class ListViewHolder extends RecyclerView.ViewHolder {
    private final TextView title;
    private final RecyclerView list;

    ListViewHolder(LayoutInflater inflater, ViewGroup parent) {
        super(inflater.inflate(R.layout.card_list, parent, false));

        title = itemView.findViewById(R.id.listCard_title);
        list = itemView.findViewById(R.id.listCard_list);
    }

    void bind(Context context, @StringRes int title, LoggedEntities entities) {
        this.title.setText(title);

        list.setLayoutManager(new LinearLayoutManager(context, RecyclerView.VERTICAL, false));
        list.setAdapter(new FilesAdapter(context, entities));
    }
}
