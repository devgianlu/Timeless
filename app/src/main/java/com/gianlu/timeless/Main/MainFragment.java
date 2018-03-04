package com.gianlu.timeless.Main;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.gianlu.commonutils.Dialogs.DialogUtils;
import com.gianlu.commonutils.RecyclerViewLayout;
import com.gianlu.timeless.Charting.SaveChartFragment;
import com.gianlu.timeless.Listing.CardsAdapter;
import com.gianlu.timeless.Models.Durations;
import com.gianlu.timeless.Models.Project;
import com.gianlu.timeless.Models.Summaries;
import com.gianlu.timeless.Models.Summary;
import com.gianlu.timeless.NetIO.WakaTime;
import com.gianlu.timeless.NetIO.WakaTimeException;
import com.gianlu.timeless.R;

import java.util.Date;

public class MainFragment extends SaveChartFragment implements WakaTime.BatchStuff, CardsAdapter.IAdapter {
    private WakaTime.Range range;
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

        WakaTime.get().batch(this);

        return layout;
    }

    @Nullable
    @Override
    public Project getProject() {
        return null;
    }

    @Override
    public void request(WakaTime.Requester requester, Handler ui) throws Exception {
        Summaries summaries = requester.summaries(range.getStartAndEnd(), null, null);

        CardsAdapter.CardsList cards = new CardsAdapter.CardsList()
                .addGlobalSummary(summaries.globalSummary)
                .addPieChart(R.string.projects, summaries.globalSummary.projects)
                .addPieChart(R.string.languages, summaries.globalSummary.languages)
                .addPieChart(R.string.editors, summaries.globalSummary.editors)
                .addPieChart(R.string.operatingSystems, summaries.globalSummary.operating_systems);

        if (range == WakaTime.Range.TODAY) {
            Summaries weekBefore = requester.summaries(range.getWeekBefore(), null, null);
            Durations durations = requester.durations(new Date(), null, null);

            cards.addDurations(1, R.string.durations, durations.durations);
            cards.addPercentage(1, R.string.averageImprovement, summaries.globalSummary.total_seconds, Summary.doTotalSecondsAverage(weekBefore.summaries));
        } else {
            cards.addProjectsBarChart(1, R.string.periodActivity, summaries.summaries);
        }

        if (getContext() == null) return;
        final CardsAdapter adapter = new CardsAdapter(getContext(), cards, this, this);
        ui.post(new Runnable() {
            @Override
            public void run() {
                layout.loadListData(adapter);
            }
        });
    }

    @Override
    public void somethingWentWrong(Exception ex) {
        if (ex instanceof WakaTimeException) layout.showMessage(ex.getMessage(), false);
        else layout.showMessage(R.string.failedLoading_reason, true, ex.getMessage());
    }

    @Override
    public void showDialog(AlertDialog.Builder builder) {
        DialogUtils.showDialog(getActivity(), builder);
    }
}
