package com.gianlu.timeless.Activities.Projects;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.gianlu.commonutils.Toaster;
import com.gianlu.timeless.Activities.CommitsActivity;
import com.gianlu.timeless.Charting.SaveChartFragment;
import com.gianlu.timeless.GrantActivity;
import com.gianlu.timeless.Listing.CardsAdapter;
import com.gianlu.timeless.Models.Duration;
import com.gianlu.timeless.Models.GlobalSummary;
import com.gianlu.timeless.Models.Project;
import com.gianlu.timeless.Models.Summary;
import com.gianlu.timeless.NetIO.WakaTime;
import com.gianlu.timeless.NetIO.WakaTimeException;
import com.gianlu.timeless.R;
import com.gianlu.timeless.Utils;

import java.util.Date;
import java.util.List;

public class ProjectFragment extends SaveChartFragment implements WakaTime.ISummary {
    private Date start;
    private Date end;
    private Project project;
    private SwipeRefreshLayout layout;
    private ProgressBar loading;
    private TextView error;
    private RecyclerView list;
    private WakaTime wakaTime;

    public static ProjectFragment getInstance(Project project, Pair<Date, Date> range) {
        ProjectFragment fragment = new ProjectFragment();
        fragment.setHasOptionsMenu(true);
        Bundle args = new Bundle();
        args.putSerializable("project", project);
        args.putSerializable("start", range.first);
        args.putSerializable("end", range.second);
        args.putString("title", project.name);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.project, menu);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.projectFragment_commits).setVisible(project != null && project.hasRepository);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.projectFragment_commits:
                if (project == null) break;
                startActivity(new Intent(getContext(), CommitsActivity.class).putExtra("project_id", project.id));
                break;
        }

        return true;
    }

    @Nullable
    @Override
    public Project getProject() {
        return (Project) getArguments().getSerializable("project");
    }

    @Nullable
    @Override
    public View onCreateView(final LayoutInflater inflater, @Nullable final ViewGroup container, @Nullable Bundle savedInstanceState) {
        layout = (SwipeRefreshLayout) inflater.inflate(R.layout.project_fragment, container, false);
        layout.setColorSchemeResources(Utils.getColors());
        loading = layout.findViewById(R.id.projectFragment_loading);
        error = layout.findViewById(R.id.projectFragment_error);
        list = layout.findViewById(R.id.projectFragment_list);
        list.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));

        project = (Project) getArguments().getSerializable("project");
        start = (Date) getArguments().getSerializable("start");
        end = (Date) getArguments().getSerializable("end");

        if (project == null || start == null || end == null) {
            loading.setEnabled(false);
            error.setVisibility(View.VISIBLE);
            return layout;
        }

        wakaTime = WakaTime.getInstance();

        layout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                wakaTime.getRangeSummary(start, end, project, ProjectFragment.this);
            }
        });

        wakaTime.getRangeSummary(start, end, project, this);

        return layout;
    }

    @Override
    public void onSummary(final List<Summary> summaries, final GlobalSummary globalSummary) {
        if (!isAdded()) return;

        if (start.getTime() == end.getTime()) {
            wakaTime.getDurations(start, project, new WakaTime.IDurations() {
                @Override
                public void onDurations(final List<Duration> durations) {
                    if (!isAdded()) return;

                    layout.setRefreshing(false);
                    error.setVisibility(View.GONE);
                    loading.setVisibility(View.GONE);
                    list.setVisibility(View.VISIBLE);

                    list.setAdapter(new CardsAdapter(getContext(), new CardsAdapter.CardsList()
                            .addGlobalSummary(globalSummary)
                            .addDurations(R.string.durationsSummary, durations)
                            .addPieChart(R.string.languagesSummary, globalSummary.languages)
                            .addFileList(R.string.filesSummary, globalSummary.entities), ProjectFragment.this));
                }

                @Override
                public void onException(final Exception ex) {
                    ProjectFragment.this.onException(ex);
                }

                @Override
                public void onWakaTimeException(WakaTimeException ex) {
                    ProjectFragment.this.onWakaTimeException(ex);
                }
            });
        } else {
            error.setVisibility(View.GONE);
            loading.setVisibility(View.GONE);
            list.setVisibility(View.VISIBLE);

            list.setAdapter(new CardsAdapter(getContext(), new CardsAdapter.CardsList()
                    .addGlobalSummary(globalSummary)
                    .addLineChart(R.string.periodActivity, summaries)
                    .addPieChart(R.string.languagesSummary, globalSummary.languages)
                    .addFileList(R.string.filesSummary, globalSummary.entities), ProjectFragment.this));
        }
    }

    @Override
    public void onWakaTimeException(WakaTimeException ex) {
        Toaster.show(getActivity(), Utils.ToastMessages.INVALID_TOKEN, ex);
        startActivity(new Intent(getContext(), GrantActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
    }

    @Override
    public void onException(final Exception ex) {
        if (ex instanceof WakaTimeException) {
            layout.setRefreshing(false);
            loading.setVisibility(View.GONE);
            error.setText(ex.getMessage());
            error.setVisibility(View.VISIBLE);
        } else {
            if (layout.isRefreshing()) {
                Toaster.show(getActivity(), Utils.ToastMessages.FAILED_REFRESHING, ex, new Runnable() {
                    @Override
                    public void run() {
                        layout.setRefreshing(false);
                    }
                });
            } else {
                Toaster.show(getActivity(), Utils.ToastMessages.FAILED_LOADING, ex, new Runnable() {
                    @Override
                    public void run() {
                        loading.setVisibility(View.GONE);
                        error.setVisibility(View.VISIBLE);
                    }
                });
            }
        }
    }
}
