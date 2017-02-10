package com.gianlu.timeless.Activities.Projects;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
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

import com.gianlu.commonutils.CommonUtils;
import com.gianlu.timeless.Activities.CommitsActivity;
import com.gianlu.timeless.Listing.CardsAdapter;
import com.gianlu.timeless.NetIO.WakaTime;
import com.gianlu.timeless.NetIO.WakaTimeException;
import com.gianlu.timeless.Objects.Duration;
import com.gianlu.timeless.Objects.Project;
import com.gianlu.timeless.Objects.Summary;
import com.gianlu.timeless.R;
import com.gianlu.timeless.Utils;

import java.util.Date;
import java.util.List;

public class ProjectFragment extends Fragment {
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
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.projectFragment_commits:
                Project project = (Project) getArguments().getSerializable("project");
                if (project != null)
                    startActivity(new Intent(getContext(), CommitsActivity.class).putExtra("project_id", project.id));
                break;
        }
        return true;
    }

    @Nullable
    @Override
    public View onCreateView(final LayoutInflater inflater, @Nullable final ViewGroup container, @Nullable Bundle savedInstanceState) {
        final SwipeRefreshLayout layout = (SwipeRefreshLayout) inflater.inflate(R.layout.project_fragment, container, false);
        layout.setColorSchemeResources(Utils.getColors());
        final ProgressBar loading = (ProgressBar) layout.findViewById(R.id.projectFragment_loading);
        final TextView error = (TextView) layout.findViewById(R.id.projectFragment_error);
        final RecyclerView list = (RecyclerView) layout.findViewById(R.id.projectFragment_list);
        list.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        final Project project = (Project) getArguments().getSerializable("project");
        final Date start = (Date) getArguments().getSerializable("start");
        final Date end = (Date) getArguments().getSerializable("end");
        if (project == null || start == null || end == null) {
            loading.setEnabled(false);
            error.setVisibility(View.VISIBLE);
            return layout;
        }

        layout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                WakaTime.getInstance().getRangeSummary(start, end, project, new WakaTime.ISummary() {
                    @Override
                    public void onSummary(final List<Summary> summaries, final Summary summary) {
                        WakaTime.getInstance().getDurations(getContext(), new Date(), project, new WakaTime.IDurations() {
                            @Override
                            public void onDurations(final List<Duration> durations) {
                                final Activity activity = getActivity();
                                if (activity != null) {
                                    activity.runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            layout.setRefreshing(false);
                                            error.setVisibility(View.GONE);

                                            CardsAdapter.CardsList cards = new CardsAdapter.CardsList()
                                                    .addSummary(summary);

                                            if (durations.size() > 0)
                                                cards.addBubbleChart(getString(R.string.app_name), durations);

                                            list.setAdapter(new CardsAdapter(getContext(), cards
                                                    .addLineChart(getString(R.string.periodActivity), summaries)
                                                    .addPieChart(getString(R.string.languagesSummary), summary.languages)
                                                    .addFileList(getString(R.string.filesSummary), summary.entities)));
                                        }
                                    });
                                }
                            }

                            @Override
                            public void onException(final Exception ex) {
                                ex.printStackTrace();
                            }
                        });
                    }

                    @Override
                    public void onException(final Exception ex) {
                        if (ex instanceof WakaTimeException) {
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    layout.setRefreshing(false);
                                    error.setText(ex.getMessage());
                                    error.setVisibility(View.VISIBLE);
                                }
                            });
                        } else {
                            CommonUtils.UIToast(getActivity(), Utils.ToastMessages.FAILED_REFRESHING, ex, new Runnable() {
                                @Override
                                public void run() {
                                    layout.setRefreshing(false);
                                }
                            });
                        }
                    }
                });
            }
        });

        WakaTime.getInstance().getRangeSummary(start, end, project, new WakaTime.ISummary() {
            @Override
            public void onSummary(final List<Summary> summaries, final Summary summary) {
                WakaTime.getInstance().getDurations(getContext(), new Date(), project, new WakaTime.IDurations() {
                    @Override
                    public void onDurations(final List<Duration> durations) {
                        final Activity activity = getActivity();
                        if (activity != null) {
                            activity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    error.setVisibility(View.GONE);
                                    loading.setVisibility(View.GONE);
                                    list.setVisibility(View.VISIBLE);

                                    CardsAdapter.CardsList cards = new CardsAdapter.CardsList()
                                            .addSummary(summary);

                                    if (durations.size() > 0)
                                        cards.addBubbleChart(getString(R.string.app_name), durations);

                                    list.setAdapter(new CardsAdapter(getContext(), cards
                                            .addLineChart(getString(R.string.periodActivity), summaries)
                                            .addPieChart(getString(R.string.languagesSummary), summary.languages)
                                            .addFileList(getString(R.string.filesSummary), summary.entities)));
                                }
                            });
                        }
                    }

                    @Override
                    public void onException(Exception ex) {
                        ex.printStackTrace();
                    }
                });
            }

            @Override
            public void onException(final Exception ex) {
                if (ex instanceof WakaTimeException) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            loading.setVisibility(View.GONE);
                            error.setText(ex.getMessage());
                            error.setVisibility(View.VISIBLE);
                        }
                    });
                } else {
                    CommonUtils.UIToast(getActivity(), Utils.ToastMessages.FAILED_LOADING, ex, new Runnable() {
                        @Override
                        public void run() {
                            loading.setVisibility(View.GONE);
                            error.setVisibility(View.VISIBLE);
                        }
                    });
                }
            }
        });

        return layout;
    }
}
