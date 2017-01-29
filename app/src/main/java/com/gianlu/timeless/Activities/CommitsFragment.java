package com.gianlu.timeless.Activities;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.gianlu.commonutils.CommonUtils;
import com.gianlu.timeless.NetIO.WakaTime;
import com.gianlu.timeless.Objects.Commits;
import com.gianlu.timeless.Objects.Project;
import com.gianlu.timeless.R;
import com.gianlu.timeless.Utils;

public class CommitsFragment extends Fragment {
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
        final SwipeRefreshLayout layout = (SwipeRefreshLayout) inflater.inflate(R.layout.commits_fragment, container, false);
        layout.setColorSchemeResources(Utils.getColors());
        final ProgressBar loading = (ProgressBar) layout.findViewById(R.id.commitsFragment_loading);
        final TextView error = (TextView) layout.findViewById(R.id.commitsFragment_error);
        final RecyclerView list = (RecyclerView) layout.findViewById(R.id.commitsFragment_list);
        list.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));

        layout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                WakaTime.getInstance().getCommits((Project) getArguments().getSerializable("project"), new WakaTime.ICommits() {
                    @Override
                    public void onCommits(final Commits commits) {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                layout.setRefreshing(false);
                                error.setVisibility(View.GONE);
                                list.setAdapter(new CommitsAdapter(getActivity(), list, commits));
                            }
                        });
                    }

                    @Override
                    public void onException(Exception ex) {
                        CommonUtils.UIToast(getActivity(), Utils.ToastMessages.FAILED_REFRESHING, ex, new Runnable() {
                            @Override
                            public void run() {
                                layout.setRefreshing(false);
                            }
                        });
                    }
                });
            }
        });

        WakaTime.getInstance().getCommits((Project) getArguments().getSerializable("project"), new WakaTime.ICommits() {
            @Override
            public void onCommits(final Commits commits) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        error.setVisibility(View.GONE);
                        loading.setVisibility(View.GONE);
                        list.setVisibility(View.VISIBLE);
                        list.setAdapter(new CommitsAdapter(getActivity(), list, commits));
                    }
                });
            }

            @Override
            public void onException(Exception ex) {
                CommonUtils.UIToast(getActivity(), Utils.ToastMessages.FAILED_LOADING, ex, new Runnable() {
                    @Override
                    public void run() {
                        loading.setVisibility(View.GONE);
                        error.setVisibility(View.VISIBLE);
                    }
                });
            }
        });

        return layout;
    }
}
