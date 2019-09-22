package com.gianlu.timeless.activities.commits;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.gianlu.commonutils.CasualViews.InfiniteRecyclerView;
import com.gianlu.timeless.R;
import com.gianlu.timeless.Utils;
import com.gianlu.timeless.api.WakaTime;
import com.gianlu.timeless.api.models.Commit;
import com.gianlu.timeless.api.models.Commits;
import com.gianlu.timeless.api.models.Project;

import java.util.Date;

public class CommitsAdapter extends InfiniteRecyclerView.InfiniteAdapter<CommitsAdapter.ViewHolder, Commit> {
    private final Project project;
    private final Listener listener;
    private final WakaTime wakaTime;

    CommitsAdapter(Context context, Commits commits, WakaTime wakaTime, Listener listener) {
        super(context, new Config<Commit>().items(commits).maxPages(commits.total_pages).separators(context, true));
        this.project = commits.project;
        this.listener = listener;
        this.wakaTime = wakaTime;
    }

    @Nullable
    @Override
    protected Date getDateFromItem(Commit item) {
        return new Date(item.committer_date);
    }

    @Override
    protected void userBindViewHolder(@NonNull CommitsAdapter.ViewHolder holder, @NonNull ItemEnclosure<Commit> item, int position) {
        final Commit commit = item.getItem();
        holder.message.setText(commit.message);
        holder.author.setText(commit.getAuthor());
        holder.hash.setText(commit.truncated_hash());
        holder.date.setText(Utils.getDateTimeFormatter().format(new Date(commit.committer_date)));
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onCommitSelected(project, commit);
        });
    }

    @NonNull
    @Override
    protected ViewHolder createViewHolder(@NonNull ViewGroup parent) {
        return new ViewHolder(inflater.inflate(R.layout.item_commit, parent, false));
    }

    @Override
    protected void moreContent(int page, @NonNull ContentProvider<Commit> provider) {
        wakaTime.getCommits(project, page, null, new WakaTime.OnResult<Commits>() {
            @Override
            public void onResult(@NonNull Commits commits) {
                provider.onMoreContent(commits);
            }

            @Override
            public void onException(@NonNull Exception ex) {
                provider.onFailed(ex);
            }
        });
    }

    public interface Listener {
        void onCommitSelected(@NonNull Project project, @NonNull Commit commit);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        final TextView author;
        final TextView message;
        final TextView hash;
        final TextView date;

        ViewHolder(View itemView) {
            super(itemView);

            author = itemView.findViewById(R.id.commit_author);
            message = itemView.findViewById(R.id.commit_message);
            hash = itemView.findViewById(R.id.commit_hash);
            date = itemView.findViewById(R.id.commit_date);
        }
    }
}
