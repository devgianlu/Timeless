package com.gianlu.timeless.activities;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import java.util.List;

class PagerAdapter extends FragmentStatePagerAdapter {
    private final List<? extends Fragment> fragments;

    PagerAdapter(@NonNull FragmentManager fm, List<? extends Fragment> fragments) {
        super(fm, FragmentStatePagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        this.fragments = fragments;
    }

    @Override
    public int getCount() {
        return fragments.size();
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        return fragments.get(position);
    }

    @Override
    public CharSequence getPageTitle(int position) {
        Bundle args = fragments.get(position).getArguments();
        return args == null ? null : args.getString("title");
    }
}
