package com.gianlu.timeless.Main;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.gianlu.timeless.NetIO.Stats;

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
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return super.onCreateView(inflater, container, savedInstanceState); // TODO: Stats fragment layout
    }
}
