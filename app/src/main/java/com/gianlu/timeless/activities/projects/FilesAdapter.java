package com.gianlu.timeless.activities.projects;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.gianlu.commonutils.misc.SuperTextView;
import com.gianlu.timeless.R;
import com.gianlu.timeless.Utils;
import com.gianlu.timeless.api.models.LoggedEntity;

import java.util.ArrayList;
import java.util.List;

public class FilesAdapter extends RecyclerView.Adapter<FilesAdapter.ViewHolder> {
    private final List<LoggedEntity> files;
    private final LayoutInflater inflater;

    public FilesAdapter(@NonNull Context context, List<LoggedEntity> files) {
        this.files = new ArrayList<>();
        for (LoggedEntity entity : files)
            if (entity.total_seconds > 0)
                this.files.add(entity);

        inflater = LayoutInflater.from(context);
    }

    @Override
    @NonNull
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(parent);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        final LoggedEntity file = files.get(position);

        String[] path = file.name.split("/");
        if (path.length == 1)
            path = file.name.split("\\u005c");

        holder.name.setCompactableText(file.name, path[path.length - 1]);
        holder.time.setText(Utils.timeFormatterHours(file.total_seconds, true));
    }

    @Override
    public int getItemCount() {
        return files.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        final SuperTextView name;
        final TextView time;

        ViewHolder(@NonNull ViewGroup parent) {
            super(inflater.inflate(R.layout.item_logged_entity, parent, false));

            name = itemView.findViewById(R.id.loggedEntityItem_name);
            time = itemView.findViewById(R.id.loggedEntityItem_time);
        }
    }
}
