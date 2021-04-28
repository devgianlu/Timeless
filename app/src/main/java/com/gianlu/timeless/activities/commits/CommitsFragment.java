package com.gianlu.timeless.activities.commits;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.gianlu.commonutils.dialogs.DialogUtils;
import com.gianlu.commonutils.misc.RecyclerMessageView;
import com.gianlu.commonutils.typography.MaterialColors;
import com.gianlu.timeless.R;
import com.gianlu.timeless.activities.ProjectsActivity;
import com.gianlu.timeless.api.WakaTime;
import com.gianlu.timeless.api.WakaTimeException;
import com.gianlu.timeless.api.models.Commit;
import com.gianlu.timeless.api.models.Commits;
import com.gianlu.timeless.api.models.Project;

public class CommitsFragment extends Fragment implements WakaTime.OnResult<Commits>, CommitsAdapter.Listener {
    private RecyclerMessageView layout;
    private WakaTime wakaTime;

    @NonNull
    public static CommitsFragment getInstance(@NonNull Project project) {
        CommitsFragment fragment = new CommitsFragment();
        fragment.setHasOptionsMenu(true);
        Bundle args = new Bundle();
        args.putSerializable("project", project);
        args.putString("title", project.name);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.commits, menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.commitsMenu_project) {
            Project project;
            Bundle args = getArguments();
            if (args != null && (project = (Project) args.getSerializable("project")) != null) {
                ProjectsActivity.startActivity(requireContext(), null, project.id);
                return true;
            }
        }

        return super.onOptionsItemSelected(item);
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
        layout = new RecyclerMessageView(requireContext());
        layout.linearLayoutManager(RecyclerView.VERTICAL, false);

        Project project;
        Bundle args = getArguments();
        if (args == null || (project = (Project) args.getSerializable("project")) == null) {
            layout.showError(R.string.errorMessage);
            return layout;
        }

        try {
            wakaTime = WakaTime.get();
        } catch (WakaTime.MissingCredentialsException ex) {
            ex.resolve(getContext());
            return layout;
        }

        layout.enableSwipeRefresh(() -> {
            wakaTime.skipNextRequestCache();
            wakaTime.getCommits(project, 1, null, this);
        }, MaterialColors.getInstance().getColorsRes());

        wakaTime.getCommits(project, 1, null, this);

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

