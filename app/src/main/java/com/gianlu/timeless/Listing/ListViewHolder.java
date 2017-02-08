package com.gianlu.timeless.Listing;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;

import com.gianlu.timeless.R;

public class ListViewHolder extends RecyclerView.ViewHolder {
    public final TextView title;
    public final RecyclerView list;

    public ListViewHolder(LayoutInflater inflater, ViewGroup parent) {
        super(inflater.inflate(R.layout.list_card, parent, false));

        title = (TextView) itemView.findViewById(R.id.listCard_title);
        list = (RecyclerView) itemView.findViewById(R.id.listCard_list);
    }
}
