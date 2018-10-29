package com.gianlu.timeless.Listing;


import android.content.Context;
import android.content.DialogInterface;
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

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

class BranchSelectorViewHolder extends RecyclerView.ViewHolder {
    private final ImageButton select;
    private final SuperTextView selected;
    private List<String> selectedBranches = null;

    BranchSelectorViewHolder(LayoutInflater inflater, ViewGroup parent) {
        super(inflater.inflate(R.layout.item_branch_selector, parent, false));

        select = itemView.findViewById(R.id.branchSelectorItem_select);
        selected = itemView.findViewById(R.id.branchSelectorItem_selected);
    }

    void bind(@NonNull final Context context, final Config config, final CardsAdapter.Listener listener) {
        if (config.selectedBranches.isEmpty())
            selectedBranches = new ArrayList<>(config.branches);
        else selectedBranches = new ArrayList<>(config.selectedBranches);

        selected.setHtml(R.string.selectedBranches, CommonUtils.join(selectedBranches, ", "));
        select.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showBranchesDialog(context, config.branches, config.listener, listener);
            }
        });
    }

    private void showBranchesDialog(@NonNull final Context context, final List<String> allBranches, final CardsAdapter.OnBranches branchesListener, CardsAdapter.Listener listener) {
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
                            Toaster.with(context).message(R.string.noBranchesSelected).show();
                            return;
                        }

                        selectedBranches.clear();
                        selectedBranches.addAll(selectedBranchesTemp);

                        if (branchesListener != null)
                            branchesListener.onBranchesChanged(selectedBranches);
                    }
                })
                .setNegativeButton(android.R.string.cancel, null);

        if (listener != null) listener.showDialog(builder);
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
