package com.gianlu.timeless.dialogs;

import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.gianlu.commonutils.CommonUtils;
import com.gianlu.timeless.R;
import com.gianlu.timeless.Utils;

public class LoggedEntityDialog extends DialogFragment {
    private DialogInterface.OnDismissListener dismissListener = null;

    @NonNull
    public static LoggedEntityDialog get(@NonNull String title, @ColorInt int bgColor, int timeSeconds) {
        LoggedEntityDialog dialog = new LoggedEntityDialog();
        Bundle args = new Bundle();
        args.putString("title", title);
        args.putInt("bgColor", bgColor);
        args.putInt("timeSeconds", timeSeconds);
        dialog.setArguments(args);
        return dialog;
    }

    @NonNull
    public LoggedEntityDialog setOnDismissListener(DialogInterface.OnDismissListener listener) {
        this.dismissListener = listener;
        return this;
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);

        if (dismissListener != null) dismissListener.onDismiss(dialog);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        LinearLayout layout = (LinearLayout) inflater.inflate(R.layout.dialog_logged_entity, container, false);
        TextView title = layout.findViewById(R.id.loggedEntityDialog_title);
        TextView time = layout.findViewById(R.id.loggedEntityDialog_time);

        Bundle args = getArguments();
        if (args != null) {
            int bgColor = args.getInt("bgColor", Color.WHITE);
            int fgColor = CommonUtils.blackOrWhiteText(bgColor);

            layout.setBackgroundColor(bgColor);
            title.setText(args.getString("title", null));
            time.setText(Utils.timeFormatterHours(args.getInt("timeSeconds", 0), true));

            title.setTextColor(fgColor);
            time.setTextColor(fgColor);
        }

        return layout;
    }
}
