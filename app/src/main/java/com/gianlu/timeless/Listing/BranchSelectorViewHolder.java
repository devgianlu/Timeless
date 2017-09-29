package com.gianlu.timeless.Listing;


import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import com.gianlu.commonutils.CommonUtils;
import com.gianlu.commonutils.SuperTextView;
import com.gianlu.commonutils.Toaster;
import com.gianlu.timeless.R;
import com.gianlu.timeless.ThisApplication;
import com.gianlu.timeless.Utils;
import com.google.android.gms.analytics.HitBuilders;

import java.util.ArrayList;
import java.util.List;

class BranchSelectorViewHolder extends RecyclerView.ViewHolder {
    private final ImageButton select;
    private final SuperTextView selected;
    private List<String> selectedBranches = null;

    BranchSelectorViewHolder(LayoutInflater inflater, ViewGroup parent) {
        super(inflater.inflate(R.layout.branch_selector_item, parent, false));

        select = itemView.findViewById(R.id.branchSelectorItem_select);
        selected = itemView.findViewById(R.id.branchSelectorItem_selected);
    }

    void bind(final Context context, final Config config) {
        if (config.selectedBranches.isEmpty())
            selectedBranches = new ArrayList<>(config.branches);
        else selectedBranches = new ArrayList<>(config.selectedBranches);

        selected.setHtml(R.string.selectedBranches, CommonUtils.join(selectedBranches, ", "));
        select.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showBranchesDialog(context, config.branches, config.listener);
            }
        });
    }

    private void showBranchesDialog(final Context context, final List<String> allBranches, final CardsAdapter.IBranches listener) {
        final boolean[] selectedBranchesBoolean = new boolean[allBranches.size()];
        for (int i = 0; i < allBranches.size(); i++)
            selectedBranchesBoolean[i] = selectedBranches.contains(allBranches.get(i));

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(R.string.selectBranches)
                .setMultiChoiceItems(allBranches.toArray(new String[0]), selectedBranchesBoolean, new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                        selectedBranchesBoolean[which] = isChecked;
                    }
                })
                .setPositiveButton(R.string.apply, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        List<String> selectedBranchesTemp = new ArrayList<>();
                        for (int i = 0; i < selectedBranchesBoolean.length; i++)
                            if (selectedBranchesBoolean[i])
                                selectedBranchesTemp.add(allBranches.get(i));

                        if (selectedBranchesTemp.isEmpty()) {
                            Toaster.show(context, Utils.Messages.NO_BRANCHES_SELECTED);
                            return;
                        }

                        selectedBranches.clear();
                        selectedBranches.addAll(selectedBranchesTemp);

                        if (listener != null)
                            listener.onBranchesChanged(selectedBranches);
                    }
                })
                .setNegativeButton(android.R.string.cancel, null);

        CommonUtils.showDialog(context, builder);

        ThisApplication.sendAnalytics(context, new HitBuilders.EventBuilder()
                .setCategory(ThisApplication.CATEGORY_USER_INPUT)
                .setAction(ThisApplication.ACTION_CHANGE_SELECTED_BRANCHES)
                .build());
    }

    static class Config {
        private final List<String> branches;
        private final List<String> selectedBranches;
        private final CardsAdapter.IBranches listener;

        Config(List<String> branches, List<String> selectedBranches, CardsAdapter.IBranches listener) {
            this.branches = branches;
            this.selectedBranches = selectedBranches;
            this.listener = listener;
        }
    }
}
