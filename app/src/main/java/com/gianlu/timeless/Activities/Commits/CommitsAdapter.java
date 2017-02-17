package com.gianlu.timeless.Activities.Commits;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.gianlu.commonutils.CommonUtils;
import com.gianlu.timeless.Activities.ProjectsActivity;
import com.gianlu.timeless.NetIO.WakaTime;
import com.gianlu.timeless.Objects.Commit;
import com.gianlu.timeless.Objects.Commits;
import com.gianlu.timeless.Objects.Project;
import com.gianlu.timeless.R;
import com.gianlu.timeless.Utils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

class CommitsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int TYPE_LOADING = 0;
    private static final int TYPE_ITEM = 1;
    private static final int TYPE_SEPARATOR = 2;
    private final List<Object> objs;
    private final LayoutInflater inflater;
    private final Context context;
    private final Project project;
    private boolean updating;
    private long currDay = -1;

    CommitsAdapter(final Activity context, RecyclerView list, final Commits commits) {
        this.context = context;
        this.project = commits.project;
        inflater = LayoutInflater.from(context);

        objs = new ArrayList<>();
        populateObjs(commits);

        list.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                if (!recyclerView.canScrollVertically(1) && !updating && commits.next_page > 0) {
                    updating = true;
                    objs.add(null);
                    notifyItemInserted(objs.size() - 1);
                    WakaTime.getInstance().getCommits(context, commits.project, commits.next_page, new WakaTime.ICommits() {
                        @Override
                        public void onCommits(Commits newCommits) {
                            objs.remove(objs.size() - 1);
                            commits.update(newCommits);
                            populateObjs(newCommits);

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
                            objs.remove(objs.size() - 1);
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

    private int countForDate(Date date) {
        int pos = objs.indexOf(date);
        int count = 0;
        for (int i = pos + 1; i < objs.size(); i++) {
            if (objs.get(i) instanceof Date)
                break;
            else
                count++;
        }

        return count;
    }

    private void populateObjs(Commits commits) {
        for (Commit commit : commits.commits) {
            if (currDay != commit.committer_date / 86400000) {
                objs.add(new Date(commit.committer_date));
                currDay = commit.committer_date / 86400000;
            }

            objs.add(commit);
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case TYPE_ITEM:
                return new ItemViewHolder(inflater.inflate(R.layout.commit_item, parent, false));
            case TYPE_LOADING:
                return new LoadingViewHolder(inflater.inflate(R.layout.loading_item, parent, false));
            case TYPE_SEPARATOR:
                return new SeparatorViewHolder(inflater.inflate(R.layout.separator_item, parent, false));
            default:
                return null;
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (objs.get(position) == null)
            return TYPE_LOADING;
        else if (objs.get(position) instanceof Commit)
            return TYPE_ITEM;
        else if (objs.get(position) instanceof Date)
            return TYPE_SEPARATOR;
        else
            return TYPE_LOADING;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof ItemViewHolder) {
            final Commit commit = (Commit) objs.get(position);
            ItemViewHolder castHolder = (ItemViewHolder) holder;
            castHolder.message.setText(commit.message);
            castHolder.author.setText(commit.getAuthor());
            castHolder.hash.setText(commit.truncated_hash);
            castHolder.date.setText(Utils.getDateFormatter().format(new Date(commit.committer_date)));
            castHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(commit.html_url)));
                }
            });
        } else if (holder instanceof SeparatorViewHolder) {
            final Date date = (Date) objs.get(position);
            SeparatorViewHolder castHolder = (SeparatorViewHolder) holder;
            castHolder.date.setText(Utils.getOnlyDateFormatter().format(date) + " (" + countForDate(date) + ")");
            castHolder.project.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    context.startActivity(new Intent(context, ProjectsActivity.class)
                            .putExtra("project_id", project.id)
                            .putExtra("date", date));
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return objs.size();
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

    private class SeparatorViewHolder extends RecyclerView.ViewHolder {
        final TextView date;
        final ImageButton project;

        SeparatorViewHolder(View itemView) {
            super(itemView);

            date = (TextView) itemView.findViewById(R.id.separatorItem_date);
            project = (ImageButton) itemView.findViewById(R.id.separatorItem_project);
        }
    }
}
