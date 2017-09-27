package com.gianlu.timeless.Activities.Commits;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.gianlu.commonutils.CommonUtils;
import com.gianlu.commonutils.InfiniteRecyclerView;
import com.gianlu.commonutils.SuperTextView;
import com.gianlu.commonutils.Toaster;
import com.gianlu.timeless.GrantActivity;
import com.gianlu.timeless.Models.Commit;
import com.gianlu.timeless.Models.Commits;
import com.gianlu.timeless.Models.Project;
import com.gianlu.timeless.NetIO.WakaTime;
import com.gianlu.timeless.NetIO.WakaTimeException;
import com.gianlu.timeless.R;
import com.gianlu.timeless.Utils;

import java.util.Date;

public class CommitsFragment extends Fragment implements WakaTime.ICommits, CommitsAdapter.IAdapter {
    private ProgressBar loading;
    private TextView error;
    private InfiniteRecyclerView list;
    private SwipeRefreshLayout layout;

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
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        layout = (SwipeRefreshLayout) inflater.inflate(R.layout.commits_fragment, container, false);
        layout.setColorSchemeResources(Utils.getColors());

        loading = layout.findViewById(R.id.commitsFragment_loading);
        error = layout.findViewById(R.id.commitsFragment_error);
        list = layout.findViewById(R.id.commitsFragment_list);
        list.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));

        final WakaTime wakaTime = WakaTime.getInstance();

        layout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                wakaTime.getCommits((Project) getArguments().getSerializable("project"), CommitsFragment.this);
            }
        });

        wakaTime.getCommits((Project) getArguments().getSerializable("project"), this);

        return layout;
    }

    @Override
    public void onCommits(final Commits commits) {
        if (!isAdded()) return;
        layout.setRefreshing(false);
        error.setVisibility(View.GONE);
        loading.setVisibility(View.GONE);
        list.setVisibility(View.VISIBLE);
        list.setAdapter(new CommitsAdapter(getContext(), commits, CommitsFragment.this));
    }

    @Override
    public void onException(Exception ex) {
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
                    layout.setRefreshing(false);
                    error.setVisibility(View.VISIBLE);
                }
            });
        }
    }

    @Override
    public void onWakaTimeException(WakaTimeException ex) {
        Toaster.show(getActivity(), Utils.ToastMessages.INVALID_TOKEN, ex);
        startActivity(new Intent(getContext(), GrantActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
    }

    @Override
    public void onCommitSelected(Project project, final Commit commit) {
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

