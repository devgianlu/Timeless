package com.gianlu.timeless.activities.commits;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import com.gianlu.commonutils.CommonUtils;
import com.gianlu.commonutils.bottomsheet.ModalBottomSheetHeaderView;
import com.gianlu.commonutils.bottomsheet.ThemedModalBottomSheet;
import com.gianlu.commonutils.misc.SuperTextView;
import com.gianlu.timeless.R;
import com.gianlu.timeless.Utils;
import com.gianlu.timeless.api.models.Commit;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.Date;

public class CommitSheet extends ThemedModalBottomSheet<Commit, Void> {
    @NonNull
    public static CommitSheet get() {
        return new CommitSheet();
    }

    @Override
    protected void onCreateHeader(@NonNull LayoutInflater inflater, @NonNull ModalBottomSheetHeaderView parent, @NonNull Commit commit) {
        parent.setBackgroundColorRes(R.color.colorPrimary);
        parent.setTitle(commit.message);
    }

    @Override
    protected void onCreateBody(@NonNull LayoutInflater inflater, @NonNull ViewGroup parent, @NonNull Commit commit) {
        inflater.inflate(R.layout.sheet_commit, parent, true);

        SuperTextView author = parent.findViewById(R.id.commitSheet_author);
        SuperTextView date = parent.findViewById(R.id.commitSheet_date);
        SuperTextView hash = parent.findViewById(R.id.commitSheet_hash);
        SuperTextView ref = parent.findViewById(R.id.commitSheet_ref);
        SuperTextView timeSpent = parent.findViewById(R.id.commitSheet_timeSpent);

        author.setHtml(R.string.commitAuthor, commit.getAuthor());
        date.setHtml(R.string.commitDate, Utils.getDateTimeFormatter().format(new Date(commit.committer_date)));
        hash.setHtml(R.string.commitHash, commit.hash);

        String time = CommonUtils.timeFormatter(commit.total_seconds);
        if (time.equals("∞")) {
            timeSpent.setVisibility(View.GONE);
        } else {
            timeSpent.setVisibility(View.VISIBLE);
            timeSpent.setHtml(R.string.commitTimeSpent, time);
        }

        if (commit.ref != null) {
            ref.setVisibility(View.VISIBLE);
            ref.setHtml(R.string.commitReference, commit.ref);
        } else {
            ref.setVisibility(View.GONE);
        }

        isLoading(false);
    }

    @Override
    protected boolean onCustomizeAction(@NonNull FloatingActionButton action, @NonNull final Commit commit) {
        action.setImageResource(R.drawable.baseline_open_in_browser_24);
        action.setColorFilter(Color.WHITE);
        action.setOnClickListener(v -> startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(commit.html_url))));
        return true;
    }
}
