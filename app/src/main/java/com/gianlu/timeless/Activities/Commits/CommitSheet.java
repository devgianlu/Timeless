package com.gianlu.timeless.Activities.Commits;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
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

public class CommitSheet extends BaseModalBottomSheet {
    @NonNull
    public static CommitSheet get(@NonNull Commit commit) {
        CommitSheet sheet = new CommitSheet();
        Bundle args = new Bundle();
        args.putSerializable("commit", commit);
        sheet.setArguments(args);
        return sheet;
    }

    @Override
    protected boolean onCreateHeader(@NonNull LayoutInflater inflater, @NonNull ViewGroup parent, @NonNull Bundle args) {
        return false;
    }

    @Override
    protected void onCreateBody(@NonNull LayoutInflater inflater, @NonNull ViewGroup parent, @NonNull Bundle args) throws MissingArgumentException {
        inflater.inflate(R.layout.sheet_commit, parent, true);

        SuperTextView author = parent.findViewById(R.id.commitSheet_author);
        SuperTextView date = parent.findViewById(R.id.commitSheet_date);
        SuperTextView hash = parent.findViewById(R.id.commitSheet_hash);
        SuperTextView ref = parent.findViewById(R.id.commitSheet_ref);
        SuperTextView timeSpent = parent.findViewById(R.id.commitSheet_timeSpent);

        Commit commit = (Commit) args.getSerializable("commit");
        if (commit == null) throw new MissingArgumentException();

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
    protected void onCustomizeToolbar(@NonNull Toolbar toolbar, @NonNull Bundle args) throws MissingArgumentException {
        toolbar.setBackgroundResource(R.color.colorPrimary);

        Commit commit = (Commit) args.getSerializable("commit");
        if (commit == null) throw new MissingArgumentException();

        toolbar.setTitle(commit.message);
    }

    @Override
    protected boolean onCustomizeAction(@NonNull FloatingActionButton action, @NonNull Bundle args) throws MissingArgumentException {
        final Commit commit = (Commit) args.getSerializable("commit");
        if (commit == null) throw new MissingArgumentException();

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
