package com.gianlu.timeless.Main;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.gianlu.commonutils.CommonUtils;
import com.gianlu.timeless.NetIO.WakaTime;
import com.gianlu.timeless.NetIO.WakaTimeException;
import com.gianlu.timeless.Objects.Summary;
import com.gianlu.timeless.R;
import com.gianlu.timeless.Utils;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class MainFragment extends Fragment {
    public static MainFragment getInstance(Context context, Range range) {
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
        final LinearLayout list = (LinearLayout) layout.findViewById(R.id.stats_list);
        final TextView error = (TextView) layout.findViewById(R.id.stats_error);
        final Range range = (Range) getArguments().getSerializable("range");

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
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                layout.setRefreshing(false);
                                error.setVisibility(View.GONE);

                                list.removeAllViews();
                                list.addView(Summary.createSummaryCard(getContext(), inflater, list, summary));
                                if (range != Range.TODAY)
                                    list.addView(Summary.createProjectsBarChartCard(getContext(), inflater, list, R.string.periodActivity, summaries));
                                list.addView(Summary.createPieChartCard(getContext(), inflater, list, R.string.projectsSummary, summary.projects));
                                list.addView(Summary.createPieChartCard(getContext(), inflater, list, R.string.languagesSummary, summary.languages));
                                list.addView(Summary.createPieChartCard(getContext(), inflater, list, R.string.editorsSummary, summary.editors));
                                list.addView(Summary.createPieChartCard(getContext(), inflater, list, R.string.operatingSystemsSummary, summary.operating_systems));
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

        WakaTime.getInstance().getRangeSummary(range.getStartAndEnd(), new WakaTime.ISummary() {
            @Override
            public void onSummary(final List<Summary> summaries, final Summary summary) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        loading.setVisibility(View.GONE);
                        list.setVisibility(View.VISIBLE);
                        error.setVisibility(View.GONE);

                        list.removeAllViews();
                        list.addView(Summary.createSummaryCard(getContext(), inflater, list, summary));
                        if (range != Range.TODAY)
                            list.addView(Summary.createProjectsBarChartCard(getContext(), inflater, list, R.string.periodActivity, summaries));
                        list.addView(Summary.createPieChartCard(getContext(), inflater, list, R.string.projectsSummary, summary.projects));
                        list.addView(Summary.createPieChartCard(getContext(), inflater, list, R.string.languagesSummary, summary.languages));
                        list.addView(Summary.createPieChartCard(getContext(), inflater, list, R.string.editorsSummary, summary.editors));
                        list.addView(Summary.createPieChartCard(getContext(), inflater, list, R.string.operatingSystemsSummary, summary.operating_systems));
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

    public enum Range {
        TODAY,
        LAST_7_DAYS,
        LAST_30_DAYS;

        public String getFormal(Context context) {
            switch (this) {
                case TODAY:
                    return context.getString(R.string.today);
                default:
                case LAST_7_DAYS:
                    return context.getString(R.string.last_7_days);
                case LAST_30_DAYS:
                    return context.getString(R.string.last_30_days);
            }
        }

        public Pair<Date, Date> getStartAndEnd() {
            Calendar cal = Calendar.getInstance();
            Date end = cal.getTime();

            switch (this) {
                case TODAY:
                    break;
                default:
                case LAST_7_DAYS:
                    cal.add(Calendar.DATE, -7);
                    break;
                case LAST_30_DAYS:
                    cal.add(Calendar.DATE, -30);
                    break;
            }

            return new Pair<>(cal.getTime(), end);
        }
    }
}
