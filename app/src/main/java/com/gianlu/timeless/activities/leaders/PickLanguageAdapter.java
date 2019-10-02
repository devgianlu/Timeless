package com.gianlu.timeless.activities.leaders;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.gianlu.timeless.R;
import com.gianlu.timeless.api.models.LoggedEntity;

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
        view = inflater.inflate(R.layout.item_pick_language, parent, false);

        TextView language = (TextView) ((ViewGroup) view).getChildAt(0);
        language.setText(name);

        if (Objects.equals(name, selectedLang)) {
            language.setTextColor(ContextCompat.getColor(inflater.getContext(), R.color.colorAccent));
            language.setTypeface(Typeface.DEFAULT_BOLD);
        }

        return view;
    }
}
