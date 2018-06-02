package com.gianlu.timeless.Activities.Projects;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.gianlu.commonutils.Dialogs.DialogUtils;
import com.gianlu.commonutils.MaterialColors;
import com.gianlu.commonutils.RecyclerViewLayout;
import com.gianlu.timeless.Activities.CommitsActivity;
import com.gianlu.timeless.Charting.SaveChartFragment;
import com.gianlu.timeless.Listing.CardsAdapter;
import com.gianlu.timeless.Models.Durations;
import com.gianlu.timeless.Models.Project;
import com.gianlu.timeless.Models.Summaries;
import com.gianlu.timeless.NetIO.WakaTime;
import com.gianlu.timeless.NetIO.WakaTimeException;
import com.gianlu.timeless.R;

import java.util.Date;
import java.util.List;

public class ProjectFragment extends SaveChartFragment implements CardsAdapter.OnBranches, WakaTime.BatchStuff, CardsAdapter.Listener {
    private Date start;
    private Date end;
    private Project project;
    private RecyclerViewLayout layout;
    private List<String> currentBranches = null;
    private WakaTime wakaTime;

    @NonNull
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
        Bundle args = getArguments();
        return args == null ? null : (Project) args.getSerializable("project");
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable final ViewGroup container, @Nullable Bundle savedInstanceState) {
        layout = new RecyclerViewLayout(inflater);
        layout.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));

        Bundle args = getArguments();
        if (args == null
                || (project = (Project) args.getSerializable("project")) == null
                || (start = (Date) args.getSerializable("start")) == null
                || (end = (Date) args.getSerializable("end")) == null) {
            layout.showMessage(R.string.errorMessage, true);
            return layout;
        }

        try {
            wakaTime = WakaTime.get();
        } catch (WakaTime.ShouldGetAccessToken ex) {
            ex.resolve(getContext());
            return layout;
        }

        layout.enableSwipeRefresh(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                wakaTime.batch(ProjectFragment.this, true);
            }
        }, MaterialColors.getInstance().getColorsRes());

        wakaTime.batch(this, false);

        return layout;
    }

    @Override
    public void onBranchesChanged(@NonNull List<String> branches) {
        currentBranches = branches;
        layout.startLoading();
        wakaTime.batch(this, false);
    }

    @Override
    public void request(@NonNull WakaTime.Requester requester, @NonNull Handler ui) throws Exception {
        Summaries summaries = requester.summaries(start, end, project, currentBranches);

        CardsAdapter.CardsList cards = new CardsAdapter.CardsList();
        cards.addBranchSelector(summaries.availableBranches, summaries.selectedBranches, this)
                .addGlobalSummary(summaries.globalSummary)
                .addPieChart(R.string.languages, summaries.globalSummary.languages)
                .addPieChart(R.string.branches, summaries.globalSummary.branches)
                .addFileList(R.string.files, summaries.globalSummary.entities);

        if (start.getTime() == end.getTime()) {
            Durations durations = requester.durations(start, project, summaries.availableBranches);
            cards.addDurations(cards.hasBranchSelector() ? 2 : 1, R.string.durations, durations);
        } else {
            cards.addLineChart(cards.hasBranchSelector() ? 2 : 1, R.string.periodActivity, summaries);
        }

        if (getContext() == null) return;
        final CardsAdapter adapter = new CardsAdapter(getContext(), cards, this, this);
        ui.post(new Runnable() {
            @Override
            public void run() {
                layout.loadListData(adapter);
            }
        });
    }

    @Override
    public void somethingWentWrong(@NonNull Exception ex) {
        if (ex instanceof WakaTimeException) layout.showMessage(ex.getMessage(), false);
        else layout.showMessage(R.string.failedLoading_reason, true, ex.getMessage());
    }

    @Override
    public void showDialog(@NonNull AlertDialog.Builder builder) {
        DialogUtils.showDialog(getActivity(), builder);
    }
}
