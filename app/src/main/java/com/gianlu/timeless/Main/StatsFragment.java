package com.gianlu.timeless.Main;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.gianlu.commonutils.CommonUtils;
import com.gianlu.timeless.NetIO.Stats;
import com.gianlu.timeless.NetIO.WakaTime;
import com.gianlu.timeless.R;
import com.gianlu.timeless.Utils;

public class StatsFragment extends Fragment {
    public static StatsFragment getInstance(Context context, Stats.Range range) {
        StatsFragment fragment = new StatsFragment();
        Bundle args = new Bundle();
        args.putString("title", range.getFormal(context));
        args.putSerializable("range", range);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(final LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final SwipeRefreshLayout layout = (SwipeRefreshLayout) inflater.inflate(R.layout.stats_fragment, container, false);
        layout.setColorSchemeResources(Utils.COLORS);
        final ProgressBar loading = (ProgressBar) layout.findViewById(R.id.stats_loading);
        final LinearLayout list = (LinearLayout) layout.findViewById(R.id.stats_list);
        final TextView error = (TextView) layout.findViewById(R.id.stats_error);
        final Stats.Range range = (Stats.Range) getArguments().getSerializable("range");

        layout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                WakaTime.getInstance().getStats(range, new WakaTime.IStats() {
                    @Override
                    public void onStats(final Stats stats) {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                list.removeAllViews();
                                layout.setRefreshing(false);
                                error.setVisibility(View.GONE);
                                list.addView(Stats.createSummaryCard(getContext(), inflater, list, stats));
                                list.addView(Stats.createPieChartCard(getContext(), inflater, list, R.string.projectsSummary, stats.projects));
                                list.addView(Stats.createPieChartCard(getContext(), inflater, list, R.string.languagesSummary, stats.languages));
                                list.addView(Stats.createPieChartCard(getContext(), inflater, list, R.string.editorsSummary, stats.editors));
                            }
                        });
                    }

                    @Override
                    public void onException(Exception ex) {
                        CommonUtils.UIToast(getActivity(), Utils.ToastMessages.FAILED_REFRESHING, ex);
                    }
                });
            }
        });

        WakaTime.getInstance().getStats(range, new WakaTime.IStats() {
            @Override
            public void onStats(final Stats stats) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        loading.setVisibility(View.GONE);
                        list.setVisibility(View.VISIBLE);
                        error.setVisibility(View.GONE);
                        list.addView(Stats.createSummaryCard(getContext(), inflater, list, stats));
                        list.addView(Stats.createPieChartCard(getContext(), inflater, list, R.string.projectsSummary, stats.projects));
                        list.addView(Stats.createPieChartCard(getContext(), inflater, list, R.string.languagesSummary, stats.languages));
                        list.addView(Stats.createPieChartCard(getContext(), inflater, list, R.string.editorsSummary, stats.editors));
                    }
                });
            }

            @Override
            public void onException(Exception ex) {
                CommonUtils.UIToast(getActivity(), Utils.ToastMessages.FAILED_LOADING, ex, new Runnable() {
                    @Override
                    public void run() {
                        error.setVisibility(View.VISIBLE);
                    }
                });
            }
        });

        return layout;
    }
}
