package com.gianlu.timeless.Main;

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
import android.widget.FrameLayout;
import android.widget.ProgressBar;

import com.gianlu.commonutils.MessageLayout;
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
    private FrameLayout layout;
    private ProgressBar loading;
    private RecyclerView list;
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
        layout = (FrameLayout) inflater.inflate(R.layout.recycler_view_layout, container, false);
        SwipeRefreshLayout swipeRefresh = layout.findViewById(R.id.recyclerViewLayout_swipeRefresh);
        swipeRefresh.setEnabled(false);
        swipeRefresh.setVisibility(View.VISIBLE);
        loading = layout.findViewById(R.id.recyclerViewLayout_loading);
        list = layout.findViewById(R.id.recyclerViewLayout_list);
        list.setVisibility(View.GONE);
        list.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));

        range = (WakaTime.Range) getArguments().getSerializable("range");
        if (range == null) {
            swipeRefresh.setVisibility(View.GONE);
            loading.setVisibility(View.GONE);
            MessageLayout.show(layout, R.string.errorMessage, R.drawable.ic_error_outline_black_48dp);
            return layout;
        }

        wakaTime = WakaTime.getInstance();
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
                            if (!isAdded()) return;

                            list.setVisibility(View.VISIBLE);
                            loading.setVisibility(View.GONE);
                            MessageLayout.hide(layout);

                            cards.addDurations(1, R.string.durationsSummary, durations);
                            cards.addPercentage(1, R.string.averageImprovement, globalSummary.total_seconds, Summary.doTotalSecondsAverage(beforeSummaries));

                            list.setAdapter(new CardsAdapter(getContext(), cards, MainFragment.this));
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
                    onSummary(null, null); // FIXME: What's this shit (!!)
                }
            });
        } else {
            loading.setVisibility(View.GONE);
            list.setVisibility(View.VISIBLE);
            MessageLayout.hide(layout);

            cards.addProjectsBarChart(1, R.string.periodActivity, summaries);

            list.setAdapter(new CardsAdapter(getContext(), cards, MainFragment.this));
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
            list.setVisibility(View.GONE);
            loading.setVisibility(View.GONE);
            MessageLayout.show(layout, ex.getMessage(), R.drawable.ic_info_outline_black_48dp);
        } else {
            loading.setVisibility(View.GONE);
            list.setVisibility(View.GONE);
            Toaster.show(getActivity(), Utils.ToastMessages.FAILED_LOADING, ex);
        }
    }

    @Nullable
    @Override
    public Project getProject() {
        return null;
    }
}
