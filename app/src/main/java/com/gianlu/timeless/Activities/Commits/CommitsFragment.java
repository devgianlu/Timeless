package com.gianlu.timeless.Activities.Commits;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.gianlu.commonutils.Dialogs.DialogUtils;
import com.gianlu.commonutils.MaterialColors;
import com.gianlu.commonutils.RecyclerViewLayout;
import com.gianlu.timeless.Models.Commit;
import com.gianlu.timeless.Models.Commits;
import com.gianlu.timeless.Models.Project;
import com.gianlu.timeless.NetIO.WakaTime;
import com.gianlu.timeless.NetIO.WakaTimeException;
import com.gianlu.timeless.R;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class CommitsFragment extends Fragment implements WakaTime.OnResult<Commits>, CommitsAdapter.Listener {
    private RecyclerViewLayout layout;
    private WakaTime wakaTime;

    @NonNull
    public static CommitsFragment getInstance(@NonNull Project project) {
        CommitsFragment fragment = new CommitsFragment();
        Bundle args = new Bundle();
        args.putSerializable("project", project);
        args.putString("title", project.name);
        fragment.setArguments(args);
        return fragment;
    }

    public boolean onBackPressed() {
        if (DialogUtils.hasVisibleDialog(getActivity())) {
            DialogUtils.dismissDialog(getActivity());
            return false;
        }

        return true;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        layout = new RecyclerViewLayout(requireContext());
        layout.setLayoutManager(new LinearLayoutManager(getContext(), RecyclerView.VERTICAL, false));

        final Project project;
        Bundle args = getArguments();
        if (args == null || (project = (Project) args.getSerializable("project")) == null) {
            layout.showError(R.string.errorMessage);
            return layout;
        }

        try {
            wakaTime = WakaTime.get();
        } catch (WakaTime.ShouldGetAccessToken ex) {
            ex.resolve(getContext());
            return layout;
        }

        layout.enableSwipeRefresh(() -> {
            wakaTime.skipNextRequestCache();
            wakaTime.getCommits(project, 1, CommitsFragment.this);
        }, MaterialColors.getInstance().getColorsRes());

        wakaTime.getCommits(project, 1, this);

        return layout;
    }

    @Override
    public void onResult(@NonNull Commits commits) {
        if (!isAdded()) return;
        layout.loadListData(new CommitsAdapter(getContext(), commits, wakaTime, this));
    }

    @Override
    public void onException(@NonNull Exception ex) {
        if (ex instanceof WakaTimeException) layout.showError(ex.getMessage());
        else layout.showError(R.string.failedLoading_reason, ex.getMessage());
    }

    @Override
    public void onCommitSelected(@NonNull Project project, @NonNull Commit commit) {
        CommitSheet.get().show(getActivity(), commit);
    }
}

