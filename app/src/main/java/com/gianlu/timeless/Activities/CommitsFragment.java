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
        SwipeRefreshLayout layout = (SwipeRefreshLayout) inflater.inflate(R.layout.commits_fragment, container, false);
        layout.setColorSchemeResources(Utils.getColors());

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
                                list.setAdapter(new CommitsAdapter(getActivity(), list, commits));
                            }
                        });
                    }

                    @Override
                    public void onException(Exception ex) {
                        ex.printStackTrace();
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
                        list.setAdapter(new CommitsAdapter(getActivity(), list, commits));
                    }
                });
            }

            @Override
            public void onException(Exception ex) {
                ex.printStackTrace();
            }
        });

        return layout;
    }
}
