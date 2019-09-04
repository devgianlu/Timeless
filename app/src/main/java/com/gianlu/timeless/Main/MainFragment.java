package com.gianlu.timeless.Main;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.gianlu.commonutils.CasualViews.RecyclerMessageView;
import com.gianlu.commonutils.Lifecycle.LifecycleAwareHandler;
import com.gianlu.commonutils.MaterialColors;
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

public class MainFragment extends SaveChartFragment implements WakaTime.BatchStuff, CardsAdapter.Listener {
    private WakaTime.Range range;
    private RecyclerMessageView layout;
    private WakaTime wakaTime;

    @NonNull
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
        layout = new RecyclerMessageView(requireContext());
        layout.linearLayoutManager(RecyclerView.VERTICAL, false);
        layout.dividerDecoration(RecyclerView.VERTICAL);

        Bundle args = getArguments();
        if (args == null || (range = (WakaTime.Range) args.getSerializable("range")) == null) {
            layout.showError(R.string.errorMessage);
            return layout;
        }

        try {
            wakaTime = WakaTime.get();
        } catch (WakaTime.ShouldGetAccessToken ex) {
            ex.resolve(getContext());
            return layout;
        }

        layout.enableSwipeRefresh(() -> wakaTime.batch(null, MainFragment.this, true), MaterialColors.getInstance().getColorsRes());
        wakaTime.batch(null, this, false);

        return layout;
    }

    @Nullable
    @Override
    public Project getProject() {
        return null;
    }

    @Override
    public void request(@NonNull WakaTime.Requester requester, @NonNull LifecycleAwareHandler ui) throws Exception {
        Summaries summaries = requester.summaries(range.getStartAndEnd(), null, null);

        CardsAdapter.CardsList cards = new CardsAdapter.CardsList()
                .addGlobalSummary(summaries.globalSummary, CardsAdapter.SummaryContext.MAIN)
                .addPieChart(R.string.projects, summaries.globalSummary.projects)
                .addPieChart(R.string.languages, summaries.globalSummary.languages)
                .addPieChart(R.string.editors, summaries.globalSummary.editors)
                .addPieChart(R.string.operatingSystems, summaries.globalSummary.operating_systems);

        if (range == WakaTime.Range.TODAY) {
            Summaries weekBefore = requester.summaries(range.getWeekBefore(), null, null);
            Durations durations = requester.durations(new Date(), null, null);

            cards.addDurations(1, durations);
            cards.addImprovement(1, summaries.globalSummary.total_seconds, Summary.doTotalSecondsAverage(weekBefore));
        } else {
            cards.addProjectsBarChart(1, R.string.periodActivity, summaries);
        }

        if (getContext() == null) return;
        final CardsAdapter adapter = new CardsAdapter(getContext(), cards, this, this);
        ui.post(this, () -> layout.loadListData(adapter));
    }

    @Override
    public void somethingWentWrong(@NonNull Exception ex) {
        if (ex instanceof WakaTimeException) layout.showError(ex.getMessage());
        else layout.showError(R.string.failedLoading_reason, ex.getMessage());
    }
}
