package com.gianlu.timeless.Activities.Commits;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.gianlu.commonutils.InfiniteRecyclerView;
import com.gianlu.timeless.Models.Commit;
import com.gianlu.timeless.Models.Commits;
import com.gianlu.timeless.Models.Project;
import com.gianlu.timeless.NetIO.WakaTime;
import com.gianlu.timeless.NetIO.WakaTimeException;
import com.gianlu.timeless.R;
import com.gianlu.timeless.Utils;

import java.util.Date;

class CommitsAdapter extends InfiniteRecyclerView.InfiniteAdapter<CommitsAdapter.ViewHolder, Commit> {
    private final Project project;

    CommitsAdapter(final Context context, final Commits commits) {
        super(context, commits.commits, commits.total_pages, ContextCompat.getColor(context, R.color.colorPrimary_shadow), true);
        this.project = commits.project;
    }

    @Nullable
    @Override
    protected Date getDateFromItem(Commit item) {
        return new Date(item.committer_date);
    }

    @Override
    protected void userBindViewHolder(CommitsAdapter.ViewHolder holder, int position) {
        final Commit commit = items.get(position).getItem();
        holder.message.setText(commit.message);
        holder.author.setText(commit.getAuthor());
        holder.hash.setText(commit.truncated_hash);
        holder.date.setText(Utils.getDateTimeFormatter().format(new Date(commit.committer_date)));
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(commit.html_url)));
            }
        });
    }

    @Override
    protected ViewHolder createViewHolder(ViewGroup parent) {
        return new ViewHolder(inflater.inflate(R.layout.commit_item, parent, false));
    }

    @Override
    protected void moreContent(int page, final IContentProvider<Commit> provider) {
        WakaTime.getInstance().getCommits(context, project, page, new WakaTime.ICommits() {
            @Override
            public void onCommits(Commits commits) {
                provider.onMoreContent(commits.commits);
            }

            @Override
            public void onException(Exception ex) {
                provider.onFailed(ex);
            }

            @Override
            public void onWakaTimeException(WakaTimeException ex) {
                provider.onFailed(ex);
            }
        });
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        final TextView author;
        final TextView message;
        final TextView hash;
        final TextView date;

        ViewHolder(View itemView) {
            super(itemView);

            author = (TextView) itemView.findViewById(R.id.commit_author);
            message = (TextView) itemView.findViewById(R.id.commit_message);
            hash = (TextView) itemView.findViewById(R.id.commit_hash);
            date = (TextView) itemView.findViewById(R.id.commit_date);
        }
    }
}
