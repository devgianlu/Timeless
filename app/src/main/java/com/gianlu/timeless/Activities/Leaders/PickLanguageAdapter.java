package com.gianlu.timeless.Activities.Leaders;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Typeface;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.gianlu.timeless.Models.LoggedEntity;
import com.gianlu.timeless.R;
import com.gianlu.timeless.Utils;

import java.util.List;
import java.util.Objects;

public class PickLanguageAdapter extends BaseAdapter {
    private final LayoutInflater inflater;
    private final String selectedLang;
    private final List<LoggedEntity> languages;

    public PickLanguageAdapter(Context context, @Nullable String selectedLang, List<LoggedEntity> languages) {
        this.selectedLang = selectedLang;
        this.languages = languages;
        this.inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return languages.size();
    }

    @Override
    public LoggedEntity getItem(int position) {
        return languages.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    @SuppressLint("ViewHolder")
    public View getView(int position, View view, ViewGroup parent) {
        String name = getItem(position).name;
        if (view == null)
            view = inflater.inflate(R.layout.pick_language_item, parent, false);

        TextView language = (TextView) ((ViewGroup) view).getChildAt(0);
        language.setText(name);

        if (Objects.equals(name, selectedLang)) {
            language.setTextColor(ContextCompat.getColor(inflater.getContext(), R.color.colorAccent));
            language.setTypeface(Typeface.DEFAULT_BOLD);
        } else {
            language.setTextColor(Utils.getTextViewDefaultColor(view.getContext()));
            language.setTypeface(Typeface.DEFAULT);
        }

        return view;
    }
}
