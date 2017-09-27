package com.gianlu.timeless.Main;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.gianlu.commonutils.Toaster;
import com.gianlu.timeless.Charting.SaveChartFragment;
import com.gianlu.timeless.GrantActivity;
import com.gianlu.timeless.Listing.CardsAdapter;
import com.gianlu.timeless.Models.Duration;
import com.gianlu.timeless.Models.GlobalSummary;
import com.gianlu.timeless.Models.Project;
import com.gianlu.timeless.Models.Summary;
import com.gianlu.timeless.NetIO.WakaTime;
import com.gianlu.timeless.NetIO.WakaTimeException;
import com.gianlu.timeless.R;
import com.gianlu.timeless.Utils;

import java.util.Date;
import java.util.List;

public class MainFragment extends SaveChartFragment implements WakaTime.ISummary {
    private WakaTime.Range range;
    private SwipeRefreshLayout layout;
    private ProgressBar loading;
    private RecyclerView list;
    private TextView error;
    private WakaTime wakaTime;

    public static MainFragment getInstance(Context context, WakaTime.Range range) {
        MainFragment fragment = new MainFragment();
        Bundle args = new Bundle();
        args.putString("title", range.getFormal(context));
        args.putSerializable("range", range);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(final LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        layout = (SwipeRefreshLayout) inflater.inflate(R.layout.main_fragment, container, false);
        layout.setColorSchemeResources(Utils.getColors());
        loading = layout.findViewById(R.id.stats_loading);
        list = layout.findViewById(R.id.stats_list);
        list.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        error = layout.findViewById(R.id.stats_error);

        range = (WakaTime.Range) getArguments().getSerializable("range");
        if (range == null) {
            loading.setEnabled(false);
            error.setVisibility(View.VISIBLE);
            return layout;
        }

        wakaTime = WakaTime.getInstance();

        layout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                wakaTime.getRangeSummary(range.getStartAndEnd(), MainFragment.this);
            }
        });

        wakaTime.getRangeSummary(range.getStartAndEnd(), this);

        return layout;
    }

    @Override
    public void onSummary(final List<Summary> summaries, final GlobalSummary globalSummary) {
        if (!isAdded()) return;

        final CardsAdapter.CardsList cards = new CardsAdapter.CardsList()
                .addGlobalSummary(globalSummary)
                .addPieChart(R.string.projectsSummary, globalSummary.projects)
                .addPieChart(R.string.languagesSummary, globalSummary.languages)
                .addPieChart(R.string.editorsSummary, globalSummary.editors)
                .addPieChart(R.string.operatingSystemsSummary, globalSummary.operating_systems);

        if (range == WakaTime.Range.TODAY) {
            wakaTime.getRangeSummary(range.getWeekBefore(), new WakaTime.ISummary() {
                @Override
                public void onSummary(final List<Summary> beforeSummaries, final GlobalSummary beforeGlobalSummary) {
                    wakaTime.getDurations(new Date(), new WakaTime.IDurations() {
                        @Override
                        public void onDurations(final List<Duration> durations) {
                            Activity activity = getActivity();
                            if (activity != null) {
                                activity.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        layout.setRefreshing(false);
                                        loading.setVisibility(View.GONE);
                                        list.setVisibility(View.VISIBLE);
                                        error.setVisibility(View.GONE);

                                        cards.addDurations(1, R.string.durationsSummary, durations);
                                        cards.addPercentage(1, R.string.averageImprovement, globalSummary.total_seconds, Summary.doTotalSecondsAverage(beforeSummaries));

                                        list.setAdapter(new CardsAdapter(getContext(), cards, MainFragment.this));
                                    }
                                });
                            }
                        }

                        @Override
                        public void onException(final Exception ex) {
                            MainFragment.this.onException(ex);
                        }

                        @Override
                        public void onWakaTimeException(WakaTimeException ex) {
                            MainFragment.this.onWakaTimeException(ex);
                        }
                    });
                }

                @Override
                public void onWakaTimeException(WakaTimeException ex) {
                    Toaster.show(getActivity(), Utils.ToastMessages.INVALID_TOKEN, ex);
                    startActivity(new Intent(getContext(), GrantActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
                }

                @Override
                public void onException(Exception ex) {
                    onSummary(null, null);
                }
            });
        } else {
            Activity activity = getActivity();
            if (activity != null) {
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        layout.setRefreshing(false);
                        loading.setVisibility(View.GONE);
                        list.setVisibility(View.VISIBLE);
                        error.setVisibility(View.GONE);

                        list.setAdapter(new CardsAdapter(getContext(), cards
                                .addProjectsBarChart(1, R.string.periodActivity, summaries), MainFragment.this));
                    }
                });
            }
        }
    }

    @Override
    public void onWakaTimeException(WakaTimeException ex) {
        Toaster.show(getActivity(), Utils.ToastMessages.INVALID_TOKEN, ex);
        startActivity(new Intent(getContext(), GrantActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
    }

    @Override
    public void onException(final Exception ex) {
        if (ex instanceof WakaTimeException) {
            Activity activity = getActivity();
            if (activity != null) {
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        layout.setRefreshing(false);
                        loading.setVisibility(View.GONE);
                        error.setText(ex.getMessage());
                        error.setVisibility(View.VISIBLE);
                    }
                });
            }
        } else {
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
                        error.setVisibility(View.VISIBLE);
                    }
                });
            }
        }
    }

    @Nullable
    @Override
    public Project getProject() {
        return null;
    }
}
