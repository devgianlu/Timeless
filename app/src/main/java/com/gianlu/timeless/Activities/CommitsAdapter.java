package com.gianlu.timeless.Activities;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.gianlu.timeless.NetIO.Commit;
import com.gianlu.timeless.R;

import java.util.List;

public class CommitsAdapter extends RecyclerView.Adapter<CommitsAdapter.ViewHolder> {
    private final List<Commit> commits;
    private final LayoutInflater inflater;

    public CommitsAdapter(Context context, List<Commit> commits) {
        this.commits = commits;

        inflater = LayoutInflater.from(context);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(inflater.inflate(R.layout.commit_item, parent, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Commit commit = commits.get(position);

        holder.message.setText(commit.message);
    }

    @Override
    public int getItemCount() {
        return commits.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final TextView author;
        public final TextView message;

        public ViewHolder(View itemView) {
            super(itemView);

            author = (TextView) itemView.findViewById(R.id.commit_author);
            message = (TextView) itemView.findViewById(R.id.commit_message);
        }
    }
}
