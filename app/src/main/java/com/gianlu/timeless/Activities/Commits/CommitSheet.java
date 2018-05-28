package com.gianlu.timeless.Activities.Commits;

import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.gianlu.commonutils.BottomSheet.BaseModalBottomSheet;
import com.gianlu.commonutils.CommonUtils;
import com.gianlu.commonutils.SuperTextView;
import com.gianlu.timeless.Models.Commit;
import com.gianlu.timeless.R;
import com.gianlu.timeless.Utils;

import java.util.Date;

public class CommitSheet extends BaseModalBottomSheet<Commit, Void> {
    @NonNull
    public static CommitSheet get() {
        return new CommitSheet();
    }

    @Override
    protected boolean onCreateHeader(@NonNull LayoutInflater inflater, @NonNull ViewGroup parent, @NonNull Commit commit) {
        return false;
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
    protected void onCustomizeToolbar(@NonNull Toolbar toolbar, @NonNull Commit commit) {
        toolbar.setBackgroundResource(R.color.colorPrimary);
        toolbar.setTitle(commit.message);
    }

    @Override
    protected boolean onCustomizeAction(@NonNull FloatingActionButton action, @NonNull final Commit commit) {
        action.setImageResource(R.drawable.ic_open_in_browser_white_48dp);
        action.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(commit.html_url)));
            }
        });

        return true;
    }
}
