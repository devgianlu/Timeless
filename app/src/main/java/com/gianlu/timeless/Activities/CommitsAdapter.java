package com.gianlu.timeless.Activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.gianlu.commonutils.CommonUtils;
import com.gianlu.timeless.NetIO.WakaTime;
import com.gianlu.timeless.Objects.Commit;
import com.gianlu.timeless.Objects.Commits;
import com.gianlu.timeless.R;
import com.gianlu.timeless.Utils;

import java.util.Date;

class CommitsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int TYPE_LOADING = 0;
    private static final int TYPE_ITEM = 1;
    private final Commits commits;
    private final LayoutInflater inflater;
    private final Context context;
    private boolean updating;

    CommitsAdapter(final Activity context, RecyclerView list, final Commits commits) {
        this.commits = commits;
        this.context = context;

        inflater = LayoutInflater.from(context);

        list.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                if (!recyclerView.canScrollVertically(1) && !updating && commits.next_page > 0) {
                    updating = true;
                    commits.commits.add(null);
                    notifyItemInserted(commits.commits.size() - 1);
                    WakaTime.getInstance().getCommits(commits.project, commits.next_page, new WakaTime.ICommits() {
                        @Override
                        public void onCommits(Commits newCommits) {
                            commits.commits.remove(commits.commits.size() - 1);
                            commits.merge(newCommits);

                            context.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    notifyDataSetChanged();
                                }
                            });

                            updating = false;
                        }

                        @Override
                        public void onException(Exception ex) {
                            commits.commits.remove(commits.commits.size() - 1);
                            CommonUtils.UIToast(context, Utils.ToastMessages.FAILED_LOADING, ex, new Runnable() {
                                @Override
                                public void run() {
                                    notifyDataSetChanged();
                                }
                            });
                            updating = false;
                        }
                    });
                }
            }
        });
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == TYPE_ITEM)
            return new ItemViewHolder(inflater.inflate(R.layout.commit_item, parent, false));
        else
            return new LoadingViewHolder(inflater.inflate(R.layout.loading_item, parent, false));
    }

    @Override
    public int getItemViewType(int position) {
        return commits.commits.get(position) == null ? TYPE_LOADING : TYPE_ITEM;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof ItemViewHolder) {
            final Commit commit = commits.commits.get(position);
            ItemViewHolder castHolder = (ItemViewHolder) holder;
            castHolder.message.setText(commit.message);
            castHolder.author.setText(commit.getAuthor());
            castHolder.hash.setText(commit.truncated_hash);
            castHolder.date.setText(Utils.dateFormatter.format(new Date(commit.committer_date)));
            castHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(commit.html_url)));
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return commits.commits.size();
    }

    private class ItemViewHolder extends RecyclerView.ViewHolder {
        final TextView author;
        final TextView message;
        final TextView hash;
        final TextView date;

        ItemViewHolder(View itemView) {
            super(itemView);

            author = (TextView) itemView.findViewById(R.id.commit_author);
            message = (TextView) itemView.findViewById(R.id.commit_message);
            hash = (TextView) itemView.findViewById(R.id.commit_hash);
            date = (TextView) itemView.findViewById(R.id.commit_date);
        }
    }

    private class LoadingViewHolder extends RecyclerView.ViewHolder {
        final ProgressBar loading;

        LoadingViewHolder(View itemView) {
            super(itemView);

            loading = (ProgressBar) ((ViewGroup) itemView).getChildAt(0);
        }
    }
}
