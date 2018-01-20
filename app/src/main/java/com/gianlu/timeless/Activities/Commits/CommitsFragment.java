package com.gianlu.timeless.Activities.Commits;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.gianlu.commonutils.CommonUtils;
import com.gianlu.commonutils.RecyclerViewLayout;
import com.gianlu.commonutils.SuperTextView;
import com.gianlu.timeless.Models.Commit;
import com.gianlu.timeless.Models.Commits;
import com.gianlu.timeless.Models.Project;
import com.gianlu.timeless.NetIO.WakaTime;
import com.gianlu.timeless.NetIO.WakaTimeException;
import com.gianlu.timeless.R;
import com.gianlu.timeless.Utils;

import java.util.Date;

public class CommitsFragment extends Fragment implements WakaTime.ICommits, CommitsAdapter.IAdapter {
    private RecyclerViewLayout layout;

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
        layout = new RecyclerViewLayout(inflater);
        layout.disableSwipeRefresh();
        layout.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));

        Project project;
        Bundle args = getArguments();
        if (args == null || (project = (Project) args.getSerializable("project")) == null) {
            layout.showMessage(R.string.errorMessage, true);
            return layout;
        }

        WakaTime wakaTime = WakaTime.getInstance();
        wakaTime.getCommits(project, this);

        return layout;
    }

    @Override
    public void onCommits(final Commits commits) {
        if (!isAdded()) return;
        layout.loadListData(new CommitsAdapter(getContext(), commits, CommitsFragment.this));
    }

    @Override
    public void onException(Exception ex) {
        if (ex instanceof WakaTimeException) {
            layout.showMessage(ex.getMessage(), false);
        } else {
            layout.showMessage(R.string.failedLoading_reason, true, ex.getMessage());
        }
    }

    @Override
    public void onInvalidToken(WakaTimeException ex) {
        Utils.invalidToken(getContext(), ex);
    }

    @Override
    public void onCommitSelected(Project project, final Commit commit) {
        if (getContext() == null) return;

        LinearLayout layout = new LinearLayout(getContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        int padding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 24, getResources().getDisplayMetrics());
        layout.setPadding(padding, padding, padding, padding);

        layout.addView(new SuperTextView(getContext(), R.string.commitAuthor, commit.getAuthor()));
        layout.addView(new SuperTextView(getContext(), R.string.commitDate, Utils.getDateTimeFormatter().format(new Date(commit.committer_date))));
        layout.addView(new SuperTextView(getContext(), R.string.commitHash, commit.hash));
        if (commit.ref != null)
            layout.addView(new SuperTextView(getContext(), R.string.commitReference, commit.ref));
        layout.addView(new SuperTextView(getContext(), R.string.commitTimeSpent, CommonUtils.timeFormatter(commit.total_seconds)));

        CommonUtils.showDialog(getActivity(), new AlertDialog.Builder(getContext())
                .setTitle(commit.message)
                .setView(layout)
                .setNeutralButton(R.string.seeBrowser, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(commit.html_url)));
                    }
                }));
    }
}

