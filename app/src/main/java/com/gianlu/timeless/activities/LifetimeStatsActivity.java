package com.gianlu.timeless.activities;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;

import com.gianlu.commonutils.dialogs.ActivityWithDialog;
import com.gianlu.commonutils.misc.RecyclerMessageView;
import com.gianlu.commonutils.misc.SuperTextView;
import com.gianlu.commonutils.ui.Toaster;
import com.gianlu.timeless.R;
import com.gianlu.timeless.Utils;
import com.gianlu.timeless.api.WakaTime;
import com.gianlu.timeless.api.models.LifetimeStats;
import com.gianlu.timeless.api.models.Project;
import com.gianlu.timeless.api.models.Projects;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class LifetimeStatsActivity extends ActivityWithDialog {
    private static final String TAG = LifetimeStatsActivity.class.getSimpleName();
    private WakaTime wakaTime;
    private RecyclerMessageView projects;
    private SuperTextView total;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lifetime_stats);
        setTitle(R.string.lifetimeStats);

        total = findViewById(R.id.lifetimeStats_total);
        projects = findViewById(R.id.lifetimeStats_projects);
        projects.linearLayoutManager(RecyclerView.VERTICAL, false);
        projects.dividerDecoration(RecyclerView.VERTICAL);

        Toolbar toolbar = findViewById(R.id.lifetimeStats_toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) actionBar.setDisplayHomeAsUpEnabled(true);

        try {
            wakaTime = WakaTime.get();
        } catch (WakaTime.MissingCredentialsException ex) {
            ex.resolve(this);
            return;
        }

        showProgress(R.string.loadingData);
        wakaTime.getLifetimeStats(null, this, new WakaTime.OnResult<LifetimeStats>() {
            @Override
            public void onResult(@NonNull LifetimeStats result) {
                dismissDialog();

                if (!result.upToDate) {
                    Toaster.with(LifetimeStatsActivity.this).message(R.string.lifetimeStatsNotUpdated).show();
                    onBackPressed();
                    return;
                }

                float days = (float) result.totalSeconds / TimeUnit.DAYS.toSeconds(1);
                if (days > 0.01)
                    total.setHtml(R.string.lifetimeStatsTotalWithDays, Utils.timeFormatterHours(result.totalSeconds, false), days);
                else
                    total.setHtml(R.string.lifetimeStatsTotal, Utils.timeFormatterHours(result.totalSeconds, false));
            }

            @Override
            public void onException(@NonNull Exception ex) {
                Log.e(TAG, "Failed loading lifetime stats.", ex);
                Toaster.with(LifetimeStatsActivity.this).message(R.string.failedLoading_reason, ex.getMessage()).show();
                dismissDialog();
                onBackPressed();
            }
        });

        wakaTime.getProjects(null, new WakaTime.OnResult<Projects>() {
            @Override
            public void onResult(@NonNull Projects result) {
                projects.loadListData(new ProjectsLifetimeStatsAdapter(LifetimeStatsActivity.this, result), false);
            }

            @Override
            public void onException(@NonNull Exception ex) {
                Log.e(TAG, "Failed loading projects.", ex);
                projects.showError(R.string.failedLoading);
            }
        });
    }

    private void projectsCountUpdated(int count) {
        if (count == 0) projects.showInfo(R.string.cannotDisplayProjectsLifetimeStats);
        else projects.showList();
    }

    private class ProjectsLifetimeStatsAdapter extends RecyclerView.Adapter<ProjectsLifetimeStatsAdapter.ViewHolder> {
        private final List<Project> projects;
        private final LayoutInflater inflater;

        ProjectsLifetimeStatsAdapter(@NonNull Context context, @NonNull List<Project> projects) {
            this.inflater = LayoutInflater.from(context);
            this.projects = projects;
            projectsCountUpdated(getItemCount());
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new ViewHolder(parent);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Project project = projects.get(position);
            holder.text.setHtml(R.string.lifetimeStatsProjectLoading, project.name);

            wakaTime.getLifetimeStats(project.name, null, new WakaTime.OnResult<LifetimeStats>() {
                @Override
                public void onResult(@NonNull LifetimeStats result) {
                    if (!result.upToDate) {
                        int index = projects.indexOf(project);
                        if (index != -1) {
                            projects.remove(index);
                            notifyItemRemoved(index);
                        }

                        return;
                    }

                    if (result.project == null || result.project.isEmpty()) {
                        projects.remove(holder.getAdapterPosition());
                        notifyItemRemoved(holder.getAdapterPosition());
                        projectsCountUpdated(getItemCount());
                    } else {
                        notifyItemChanged(holder.getAdapterPosition(), result);
                    }
                }

                @Override
                public void onException(@NonNull Exception ex) {
                    Log.e(TAG, "Failed loading lifetime stats for project: " + project.name, ex);

                    int index = projects.indexOf(project);
                    if (index != -1) {
                        projects.remove(index);
                        notifyItemRemoved(index);
                    }
                }
            });
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position, @NonNull List<Object> payloads) {
            if (payloads.isEmpty()) {
                onBindViewHolder(holder, position);
                return;
            }

            Object obj = payloads.get(0);
            if (obj instanceof LifetimeStats) {
                LifetimeStats update = (LifetimeStats) payloads.get(0);
                if (!Objects.equals(update.project, projects.get(position).name))
                    return;

                float days = (float) update.totalSeconds / TimeUnit.DAYS.toSeconds(1);
                if (days > 0.01)
                    holder.text.setHtml(R.string.lifetimeStatsProjectWithDays, update.project, Utils.timeFormatterHours(update.totalSeconds, false), days);
                else
                    holder.text.setHtml(R.string.lifetimeStatsProject, update.project, Utils.timeFormatterHours(update.totalSeconds, false));
            }
        }

        @Override
        public int getItemCount() {
            return projects.size();
        }

        private class ViewHolder extends RecyclerView.ViewHolder {
            final SuperTextView text;

            ViewHolder(@NonNull ViewGroup parent) {
                super(inflater.inflate(R.layout.item_project_lifetime_stats, parent, false));
                text = (SuperTextView) itemView;
            }
        }
    }
}
