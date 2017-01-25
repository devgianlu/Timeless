package com.gianlu.timeless.Main;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.gianlu.timeless.GrantActivity;
import com.gianlu.timeless.NetIO.Stats;
import com.gianlu.timeless.NetIO.WakaTime;
import com.gianlu.timeless.R;

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
        SwipeRefreshLayout layout = (SwipeRefreshLayout) inflater.inflate(R.layout.stats_fragment, container, false);
        final LinearLayout list = (LinearLayout) layout.findViewById(R.id.stats_list);
        final Stats.Range range = (Stats.Range) getArguments().getSerializable("range");

        WakaTime.getInstance().getStats(range, new WakaTime.IStats() {
            @Override
            public void onStats(final Stats stats) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        list.addView(Stats.createRangeProjectsSummary(getContext(), inflater, list, stats));
                        list.addView(Stats.createRangeLanguagesSummary(getContext(), inflater, list, stats));
                    }
                });
            }

            @Override
            public void onException(Exception ex) {
                startActivity(new Intent(getContext(), GrantActivity.class));
                ex.printStackTrace();
            }
        });

        return layout;
    }
}
