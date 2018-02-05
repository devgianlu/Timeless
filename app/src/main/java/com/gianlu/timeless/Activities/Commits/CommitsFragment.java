package com.gianlu.timeless.Activities.Commits;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.gianlu.commonutils.RecyclerViewLayout;
import com.gianlu.timeless.Models.Commit;
import com.gianlu.timeless.Models.Commits;
import com.gianlu.timeless.Models.Project;
import com.gianlu.timeless.NetIO.WakaTime;
import com.gianlu.timeless.NetIO.WakaTimeException;
import com.gianlu.timeless.R;
import com.gianlu.timeless.Utils;

public class CommitsFragment extends Fragment implements WakaTime.ICommits, CommitsAdapter.IAdapter {
    private RecyclerViewLayout recyclerViewLayout;
    private CommitSheet sheet;

    public static CommitsFragment getInstance(Project project) {
        CommitsFragment fragment = new CommitsFragment();
        Bundle args = new Bundle();
        args.putSerializable("project", project);
        args.putString("title", project.name);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        CoordinatorLayout layout = (CoordinatorLayout) inflater.inflate(R.layout.fragment_commits, container, false);
        recyclerViewLayout = (RecyclerViewLayout) layout.getChildAt(0);
        recyclerViewLayout.disableSwipeRefresh();
        recyclerViewLayout.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));

        sheet = new CommitSheet(layout);

        Project project;
        Bundle args = getArguments();
        if (args == null || (project = (Project) args.getSerializable("project")) == null) {
            recyclerViewLayout.showMessage(R.string.errorMessage, true);
            return layout;
        }

        WakaTime.getInstance().getCommits(project, this);

        return layout;
    }

    @Override
    public void onCommits(Commits commits) {
        if (!isAdded()) return;
        recyclerViewLayout.loadListData(new CommitsAdapter(getContext(), commits, CommitsFragment.this));
    }

    @Override
    public void onException(Exception ex) {
        if (ex instanceof WakaTimeException) {
            recyclerViewLayout.showMessage(ex.getMessage(), false);
        } else {
            recyclerViewLayout.showMessage(R.string.failedLoading_reason, true, ex.getMessage());
        }
    }

    @Override
    public void onInvalidToken(WakaTimeException ex) {
        Utils.invalidToken(getContext(), ex);
    }

    @Override
    public void onCommitSelected(Project project, Commit commit) {
        sheet.expand(commit);
    }
}

