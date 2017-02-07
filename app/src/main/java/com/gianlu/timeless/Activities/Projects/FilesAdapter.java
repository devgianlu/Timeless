package com.gianlu.timeless.Activities.Projects;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.gianlu.timeless.Objects.LoggedEntity;
import com.gianlu.timeless.R;
import com.gianlu.timeless.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class FilesAdapter extends RecyclerView.Adapter<FilesAdapter.ViewHolder> {
    private static final Integer NAME_PATH = 0;
    private static final Integer NAME_NAME = 1;
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

        final Spanned fileName = Html.fromHtml("<b>" + path[path.length - 1] + "</b>");
        String rebuiltPath = file.name.startsWith("/") ? "/" : "";
        for (int i = 0; i < path.length - 1; i++)
            rebuiltPath += path[i] + "/";

        final Spanned filePath = Html.fromHtml(rebuiltPath + "<b>" + path[path.length - 1] + "</b>");

        holder.name.setText(fileName);
        holder.name.setTag(NAME_NAME);
        holder.name.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Objects.equals(holder.name.getTag(), NAME_PATH)) {
                    holder.name.setText(fileName);
                    holder.name.setTag(NAME_NAME);
                } else {
                    holder.name.setText(filePath);
                    holder.name.setTag(NAME_PATH);
                }
            }
        });
        holder.time.setText(Utils.timeFormatterHours(file.total_seconds, true));
    }

    @Override
    public int getItemCount() {
        return files.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        final TextView name;
        final TextView time;

        ViewHolder(View itemView) {
            super(itemView);

            name = (TextView) itemView.findViewById(R.id.loggedEntityItem_name);
            time = (TextView) itemView.findViewById(R.id.loggedEntityItem_time);
        }
    }
}
