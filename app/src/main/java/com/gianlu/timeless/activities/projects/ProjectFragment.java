package com.gianlu.timeless.activities.projects;

import android.os.Bundle;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.gianlu.commonutils.dialogs.FragmentWithDialog;
import com.gianlu.commonutils.lifecycle.LifecycleAwareHandler;
import com.gianlu.commonutils.misc.RecyclerMessageView;
import com.gianlu.commonutils.typography.MaterialColors;
import com.gianlu.timeless.R;
import com.gianlu.timeless.activities.CommitsActivity;
import com.gianlu.timeless.api.WakaTime;
import com.gianlu.timeless.api.WakaTimeException;
import com.gianlu.timeless.api.models.Durations;
import com.gianlu.timeless.api.models.Project;
import com.gianlu.timeless.api.models.Summaries;
import com.gianlu.timeless.listing.CardsAdapter;
import com.gianlu.timeless.listing.HelperViewHolder;
import com.gianlu.timeless.listing.PieChartViewHolder.ChartContext;

import java.util.Date;
import java.util.List;

public class ProjectFragment extends FragmentWithDialog implements CardsAdapter.OnBranches, WakaTime.BatchStuff, HelperViewHolder.Listener {
    private Date start;
    private Date end;
    private Project project;
    private RecyclerMessageView rmv;
    private List<String> currentBranches = null;
    private WakaTime wakaTime;

    @NonNull
    public static ProjectFragment getInstance(@NonNull Project project, @NonNull Pair<Date, Date> range) {
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
    public void onCreateOptionsMenu(@NonNull Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.project, menu);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.projectFragment_commits).setVisible(project != null && project.hasRepository);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.projectFragment_commits) {
            if (project == null) return false;
            CommitsActivity.startActivity(requireContext(), project.id);
        }

        return true;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable final ViewGroup container, @Nullable Bundle savedInstanceState) {
        rmv = new RecyclerMessageView(requireContext());
        rmv.linearLayoutManager(RecyclerView.VERTICAL, false);
        rmv.dividerDecoration(RecyclerView.VERTICAL);

        Bundle args = getArguments();
        if (args == null
                || (project = (Project) args.getSerializable("project")) == null
                || (start = (Date) args.getSerializable("start")) == null
                || (end = (Date) args.getSerializable("end")) == null) {
            rmv.showError(R.string.errorMessage);
            return rmv;
        }

        try {
            wakaTime = WakaTime.get();
        } catch (WakaTime.ShouldGetAccessToken ex) {
            ex.resolve(getContext());
            return rmv;
        }

        rmv.enableSwipeRefresh(() -> wakaTime.batch(null, ProjectFragment.this, true), MaterialColors.getInstance().getColorsRes());
        wakaTime.batch(null, this, false);

        return rmv;
    }

    @Override
    public void onBranchesChanged(@NonNull List<String> branches) {
        currentBranches = branches;
        rmv.startLoading();
        wakaTime.batch(null, this, false);
    }

    @Override
    public void request(@NonNull WakaTime.Requester requester, @NonNull LifecycleAwareHandler ui) throws Exception {
        Summaries summaries = requester.summaries(start, end, project, currentBranches);

        CardsAdapter.CardsList cards = new CardsAdapter.CardsList();
        if (summaries.availableBranches != null && summaries.selectedBranches != null)
            cards.addBranchSelector(summaries.availableBranches, summaries.selectedBranches, this);

        cards.addGlobalSummary(summaries.globalSummary, CardsAdapter.SummaryContext.PROJECTS)
                .addPieChart(R.string.languages, ChartContext.IRRELEVANT, summaries.globalSummary.interval(), summaries.globalSummary.languages)
                .addPieChart(R.string.branches, ChartContext.IRRELEVANT, summaries.globalSummary.interval(), summaries.globalSummary.branches)
                .addFileList(R.string.files, summaries.globalSummary.entities);

        if (start.getTime() == end.getTime()) {
            Durations durations = requester.durations(start, project, summaries.availableBranches);
            cards.addDurations(cards.hasBranchSelector() ? 2 : 1, durations);
        } else {
            cards.addLineChart(cards.hasBranchSelector() ? 2 : 1, R.string.periodActivity, summaries);
        }

        if (getContext() == null) return;
        final CardsAdapter adapter = new CardsAdapter(getContext(), cards, project, this);
        ui.post(this, () -> rmv.loadListData(adapter));
    }

    @Override
    public void somethingWentWrong(@NonNull Exception ex) {
        if (ex instanceof WakaTimeException) rmv.showError(ex.getMessage());
        else rmv.showError(R.string.failedLoading_reason, ex.getMessage());
    }
}
