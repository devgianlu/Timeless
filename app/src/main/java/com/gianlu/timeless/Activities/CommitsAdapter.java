package com.gianlu.timeless.Activities;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.gianlu.commonutils.CommonUtils;
import com.gianlu.timeless.NetIO.Commit;
import com.gianlu.timeless.NetIO.Commits;
import com.gianlu.timeless.NetIO.WakaTime;
import com.gianlu.timeless.R;
import com.gianlu.timeless.Utils;

public class CommitsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int TYPE_LOADING = 0;
    private static final int TYPE_ITEM = 1;
    private final Commits commits;
    private final LayoutInflater inflater;
    private boolean updating;

    public CommitsAdapter(final Activity context, RecyclerView list, final Commits commits) {
        this.commits = commits;

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
            Commit commit = commits.commits.get(position);
            ItemViewHolder castHolder = (ItemViewHolder) holder;
            castHolder.message.setText(commit.message);
        }
    }

    @Override
    public int getItemCount() {
        return commits.commits.size();
    }

    public class ItemViewHolder extends RecyclerView.ViewHolder {
        public final TextView author;
        public final TextView message;

        public ItemViewHolder(View itemView) {
            super(itemView);

            author = (TextView) itemView.findViewById(R.id.commit_author);
            message = (TextView) itemView.findViewById(R.id.commit_message);
        }
    }

    public class LoadingViewHolder extends RecyclerView.ViewHolder {
        public final ProgressBar loading;

        public LoadingViewHolder(View itemView) {
            super(itemView);

            loading = (ProgressBar) ((ViewGroup) itemView).getChildAt(0);
        }
    }
}
