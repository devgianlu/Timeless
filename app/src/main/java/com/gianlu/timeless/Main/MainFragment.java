package com.gianlu.timeless.Main;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.gianlu.commonutils.RecyclerViewLayout;
import com.gianlu.timeless.Charting.SaveChartFragment;
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
    private WakaTime wakaTime;
    private RecyclerViewLayout layout;

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
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        layout = new RecyclerViewLayout(inflater);
        layout.disableSwipeRefresh();
        layout.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));

        Bundle args = getArguments();
        if (args == null || (range = (WakaTime.Range) args.getSerializable("range")) == null) {
            layout.showMessage(R.string.errorMessage, true);
            return layout;
        }

        wakaTime = WakaTime.getInstance();
        wakaTime.getRangeSummary(range.getStartAndEnd(), this);

        return layout;
    }

    @Override
    public void onSummary(final List<Summary> summaries, final GlobalSummary globalSummary, @Nullable List<String> branches, @Nullable final List<String> selectedBranches) {
        if (!isAdded()) return;

        final CardsAdapter.CardsList cards = new CardsAdapter.CardsList()
                .addGlobalSummary(globalSummary)
                .addPieChart(R.string.projects, globalSummary.projects)
                .addPieChart(R.string.languages, globalSummary.languages)
                .addPieChart(R.string.editors, globalSummary.editors)
                .addPieChart(R.string.operatingSystems, globalSummary.operating_systems);

        if (range == WakaTime.Range.TODAY) {
            wakaTime.getRangeSummary(range.getWeekBefore(), new WakaTime.ISummary() {
                @Override
                public void onSummary(final List<Summary> beforeSummaries, final GlobalSummary beforeGlobalSummary, @Nullable List<String> branches, @Nullable final List<String> selectedBranches) {
                    wakaTime.getDurations(new Date(), null, new WakaTime.IDurations() {
                        @Override
                        public void onDurations(final List<Duration> durations, List<String> branches) {
                            if (!isAdded()) return;

                            cards.addDurations(1, R.string.durations, durations);
                            cards.addPercentage(1, R.string.averageImprovement, globalSummary.total_seconds, Summary.doTotalSecondsAverage(beforeSummaries));

                            layout.loadListData(new CardsAdapter(getContext(), cards, MainFragment.this));
                        }

                        @Override
                        public void onException(final Exception ex) {
                            MainFragment.this.onException(ex);
                        }

                        @Override
                        public void onInvalidToken(WakaTimeException ex) {
                            MainFragment.this.onInvalidToken(ex);
                        }
                    });
                }

                @Override
                public void onInvalidToken(WakaTimeException ex) {
                    MainFragment.this.onInvalidToken(ex);
                }

                @Override
                public void onException(Exception ex) {
                    MainFragment.this.onException(ex);
                }
            });
        } else {
            cards.addProjectsBarChart(1, R.string.periodActivity, summaries);
            layout.loadListData(new CardsAdapter(getContext(), cards, MainFragment.this));
        }
    }

    @Override
    public void onInvalidToken(WakaTimeException ex) {
        Utils.invalidToken(getContext(), ex);
    }

    @Override
    public void onException(final Exception ex) {
        if (ex instanceof WakaTimeException) layout.showMessage(ex.getMessage(), false);
        else layout.showMessage(R.string.failedLoading_reason, true, ex.getMessage());
    }

    @Nullable
    @Override
    public Project getProject() {
        return null;
    }
}
