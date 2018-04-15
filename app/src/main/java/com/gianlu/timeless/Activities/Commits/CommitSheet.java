package com.gianlu.timeless.Activities.Commits;

import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.gianlu.commonutils.CommonUtils;
import com.gianlu.commonutils.NiceBaseBottomSheet;
import com.gianlu.commonutils.SuperTextView;
import com.gianlu.timeless.Models.Commit;
import com.gianlu.timeless.R;
import com.gianlu.timeless.Utils;

import java.util.Date;

public class CommitSheet extends NiceBaseBottomSheet {
    CommitSheet(ViewGroup parent) {
        super(parent, R.layout.sheet_header_commit, R.layout.sheet_commit, false);
    }

    @Override
    protected boolean onPrepareAction(@NonNull FloatingActionButton fab, final Object... payloads) {
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.getContext().startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(((Commit) payloads[0]).html_url)));
            }
        });

        return true;
    }

    @Override
    protected void onCreateHeaderView(@NonNull ViewGroup parent, Object... payloads) {
        TextView message = parent.findViewById(R.id.commitSheet_message);
        message.setText(((Commit) payloads[0]).message);

        parent.setBackgroundResource(R.color.colorPrimary);
    }

    @Override
    protected void onCreateContentView(@NonNull ViewGroup parent, Object... payloads) {
        SuperTextView author = parent.findViewById(R.id.commitSheet_author);
        SuperTextView date = parent.findViewById(R.id.commitSheet_date);
        SuperTextView hash = parent.findViewById(R.id.commitSheet_hash);
        SuperTextView ref = parent.findViewById(R.id.commitSheet_ref);
        SuperTextView timeSpent = parent.findViewById(R.id.commitSheet_timeSpent);

        Commit commit = (Commit) payloads[0];

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
    }
}
