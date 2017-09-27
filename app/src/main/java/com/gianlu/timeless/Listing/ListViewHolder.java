package com.gianlu.timeless.Listing;

import android.content.Context;
import android.support.annotation.StringRes;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;

import com.gianlu.timeless.Activities.Projects.FilesAdapter;
import com.gianlu.timeless.Models.LoggedEntity;
import com.gianlu.timeless.R;

import java.util.List;

class ListViewHolder extends RecyclerView.ViewHolder {
    private final TextView title;
    private final RecyclerView list;

    ListViewHolder(LayoutInflater inflater, ViewGroup parent) {
        super(inflater.inflate(R.layout.list_card, parent, false));

        title = itemView.findViewById(R.id.listCard_title);
        list = itemView.findViewById(R.id.listCard_list);
    }

    void bind(Context context, @StringRes int title, List<LoggedEntity> entities) {
        this.title.setText(title);

        list.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false));
        list.setAdapter(new FilesAdapter(context, entities));
    }
}
