package com.gianlu.timeless.Main;

import android.app.Activity;
import android.content.Context;
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
import com.gianlu.timeless.Listing.CardsAdapter;
import com.gianlu.timeless.NetIO.WakaTime;
import com.gianlu.timeless.NetIO.WakaTimeException;
import com.gianlu.timeless.Objects.Summary;
import com.gianlu.timeless.R;
import com.gianlu.timeless.Utils;

import java.util.List;

public class MainFragment extends Fragment {
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
        final SwipeRefreshLayout layout = (SwipeRefreshLayout) inflater.inflate(R.layout.main_fragment, container, false);
        layout.setColorSchemeResources(Utils.getColors());
        final ProgressBar loading = (ProgressBar) layout.findViewById(R.id.stats_loading);
        final RecyclerView list = (RecyclerView) layout.findViewById(R.id.stats_list);
        list.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        final TextView error = (TextView) layout.findViewById(R.id.stats_error);
        final WakaTime.Range range = (WakaTime.Range) getArguments().getSerializable("range");

        if (range == null) {
            loading.setEnabled(false);
            error.setVisibility(View.VISIBLE);
            return layout;
        }

        layout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                WakaTime.getInstance().getRangeSummary(range.getStartAndEnd(), new WakaTime.ISummary() {
                    @Override
                    public void onSummary(final List<Summary> summaries, final Summary summary) {
                        Activity activity = getActivity();
                        if (activity != null) {
                            activity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    layout.setRefreshing(false);
                                    error.setVisibility(View.GONE);

                                    CardsAdapter.CardsList cardsList = new CardsAdapter.CardsList()
                                            .addSummary(summary);

                                    if (range != WakaTime.Range.TODAY)
                                        cardsList.addProjectsBarChart(getString(R.string.periodActivity), summaries);

                                    list.setAdapter(new CardsAdapter(getContext(), cardsList
                                            .addPieChart(getString(R.string.projectsSummary), summary.projects)
                                            .addPieChart(getString(R.string.languagesSummary), summary.languages)
                                            .addPieChart(getString(R.string.editorsSummary), summary.editors)
                                            .addPieChart(getString(R.string.operatingSystemsSummary), summary.operating_systems)));
                                }
                            });
                        }
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
                                        error.setText(ex.getMessage());
                                        error.setVisibility(View.VISIBLE);
                                    }
                                });
                            }
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

        WakaTime.getInstance().getRangeSummary(range.getStartAndEnd(), new WakaTime.ISummary() {
            @Override
            public void onSummary(final List<Summary> summaries, final Summary summary) {
                Activity activity = getActivity();
                if (activity != null) {
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            loading.setVisibility(View.GONE);
                            list.setVisibility(View.VISIBLE);
                            error.setVisibility(View.GONE);

                            CardsAdapter.CardsList cardsList = new CardsAdapter.CardsList()
                                    .addSummary(summary);

                            if (range != WakaTime.Range.TODAY)
                                cardsList.addProjectsBarChart(getString(R.string.periodActivity), summaries);

                            list.setAdapter(new CardsAdapter(getContext(), cardsList
                                    .addPieChart(getString(R.string.projectsSummary), summary.projects)
                                    .addPieChart(getString(R.string.languagesSummary), summary.languages)
                                    .addPieChart(getString(R.string.editorsSummary), summary.editors)
                                    .addPieChart(getString(R.string.operatingSystemsSummary), summary.operating_systems)));
                        }
                    });
                }
            }

            @Override
            public void onException(final Exception ex) {
                if (ex instanceof WakaTimeException) {
                    Activity activity = getActivity();
                    if (activity != null) {
                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                loading.setVisibility(View.GONE);
                                error.setText(ex.getMessage());
                                error.setVisibility(View.VISIBLE);
                            }
                        });
                    }
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
