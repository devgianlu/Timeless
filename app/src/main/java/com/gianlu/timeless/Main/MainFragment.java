package com.gianlu.timeless.Main;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.gianlu.commonutils.RecyclerViewLayout;
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
    public View onCreateView(final LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        layout = new RecyclerViewLayout(inflater);
        layout.disableSwipeRefresh();
        layout.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));

        range = (WakaTime.Range) getArguments().getSerializable("range");
        if (range == null) {
            layout.showMessage(R.string.errorMessage, true);
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

                            cards.addDurations(1, R.string.durationsSummary, durations);
                            cards.addPercentage(1, R.string.averageImprovement, globalSummary.total_seconds, Summary.doTotalSecondsAverage(beforeSummaries));

                            layout.loadListData(new CardsAdapter(getContext(), cards, MainFragment.this));
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
            cards.addProjectsBarChart(1, R.string.periodActivity, summaries);
            layout.loadListData(new CardsAdapter(getContext(), cards, MainFragment.this));
        }
    }

    @Override
    public void onWakaTimeException(WakaTimeException ex) {
        Toaster.show(getActivity(), Utils.ToastMessages.INVALID_TOKEN, ex);
        startActivity(new Intent(getContext(), GrantActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
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
