package com.gianlu.timeless.listing;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageButton;

import androidx.annotation.NonNull;

import com.gianlu.commonutils.CommonUtils;
import com.gianlu.commonutils.misc.SuperTextView;
import com.gianlu.commonutils.ui.Toaster;
import com.gianlu.timeless.R;
import com.gianlu.timeless.ThisApplication;
import com.gianlu.timeless.Utils;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;
import java.util.List;

class BranchSelectorViewHolder extends HelperViewHolder {
    private final ImageButton select;
    private final SuperTextView selected;
    private List<String> selectedBranches = null;

    BranchSelectorViewHolder(Listener listener, LayoutInflater inflater, ViewGroup parent) {
        super(listener, inflater, parent, R.layout.item_branch_selector);

        select = itemView.findViewById(R.id.branchSelectorItem_select);
        selected = itemView.findViewById(R.id.branchSelectorItem_selected);
    }

    void bind(Config config) {
        if (config.selectedBranches.isEmpty())
            selectedBranches = new ArrayList<>(config.branches);
        else
            selectedBranches = new ArrayList<>(config.selectedBranches);

        selected.setHtml(R.string.selectedBranches, CommonUtils.join(selectedBranches, ", "));
        select.setOnClickListener(v -> showBranchesDialog(v.getContext(), config.branches, config.listener));
    }

    private void showBranchesDialog(@NonNull Context context, List<String> allBranches, CardsAdapter.OnBranches branchesListener) {
        final boolean[] selectedBranchesBoolean = new boolean[allBranches.size()];
        for (int i = 0; i < allBranches.size(); i++)
            selectedBranchesBoolean[i] = selectedBranches.contains(allBranches.get(i));

        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(context);
        builder.setTitle(R.string.selectBranches)
                .setMultiChoiceItems(allBranches.toArray(new String[0]), selectedBranchesBoolean, (dialog, which, isChecked) -> selectedBranchesBoolean[which] = isChecked)
                .setPositiveButton(R.string.apply, (dialog, which) -> {
                    List<String> selectedBranchesTemp = new ArrayList<>();
                    for (int i = 0; i < selectedBranchesBoolean.length; i++)
                        if (selectedBranchesBoolean[i])
                            selectedBranchesTemp.add(allBranches.get(i));

                    if (selectedBranchesTemp.isEmpty()) {
                        Toaster.with(context).message(R.string.noBranchesSelected).show();
                        return;
                    }

                    selectedBranches.clear();
                    selectedBranches.addAll(selectedBranchesTemp);

                    if (branchesListener != null)
                        branchesListener.onBranchesChanged(selectedBranches);
                })
                .setNegativeButton(android.R.string.cancel, null);

        showDialog(builder);
        ThisApplication.sendAnalytics(Utils.ACTION_CHANGE_SELECTED_BRANCHES);
    }

    static class Config {
        private final List<String> branches;
        private final List<String> selectedBranches;
        private final CardsAdapter.OnBranches listener;

        Config(List<String> branches, List<String> selectedBranches, CardsAdapter.OnBranches listener) {
            this.branches = branches;
            this.selectedBranches = selectedBranches;
            this.listener = listener;
        }
    }
}
