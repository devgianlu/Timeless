package com.gianlu.timeless.Activities.Projects;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.gianlu.commonutils.SuperTextView;
import com.gianlu.timeless.Models.LoggedEntity;
import com.gianlu.timeless.R;
import com.gianlu.timeless.Utils;

import java.util.ArrayList;
import java.util.List;

public class FilesAdapter extends RecyclerView.Adapter<FilesAdapter.ViewHolder> {
    private final List<LoggedEntity> files;
    private final LayoutInflater inflater;

    public FilesAdapter(Context context, List<LoggedEntity> files) {
        this.files = new ArrayList<>();
        for (LoggedEntity entity : files)
            if (entity.total_seconds > 0)
                this.files.add(entity);

        inflater = LayoutInflater.from(context);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(inflater.inflate(R.layout.logged_entity_item, parent, false));
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        final LoggedEntity file = files.get(position);

        String[] path = file.name.split("/");
        if (path.length == 1)
            path = file.name.split("\\u005c");

        holder.name.setCompactedText(file.name, path[path.length - 1]);
        holder.time.setText(Utils.timeFormatterHours(file.total_seconds, true));
    }

    @Override
    public int getItemCount() {
        return files.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        final SuperTextView name;
        final TextView time;

        ViewHolder(View itemView) {
            super(itemView);

            name = itemView.findViewById(R.id.loggedEntityItem_name);
            time = itemView.findViewById(R.id.loggedEntityItem_time);
        }
    }
}
