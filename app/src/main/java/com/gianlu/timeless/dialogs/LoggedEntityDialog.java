package com.gianlu.timeless.dialogs;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.fragment.app.DialogFragment;

import com.gianlu.commonutils.CommonUtils;
import com.gianlu.timeless.R;
import com.gianlu.timeless.Utils;

public class LoggedEntityDialog extends DialogFragment {
    private DialogInterface.OnDismissListener dismissListener = null;

    @NonNull
    public static LoggedEntityDialog get(@NonNull String title, @ColorInt int bgColor, int timeSeconds, @Nullable Action action) {
        LoggedEntityDialog dialog = new LoggedEntityDialog();
        Bundle args = new Bundle();
        args.putString("title", title);
        args.putParcelable("action", action);
        args.putInt("bgColor", bgColor);
        args.putInt("timeSeconds", timeSeconds);
        dialog.setArguments(args);
        return dialog;
    }

    public void setOnDismissListener(DialogInterface.OnDismissListener listener) {
        this.dismissListener = listener;
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
        Button actionBtn = layout.findViewById(R.id.loggedEntityDialog_action);

        Bundle args = getArguments();
        if (args != null) {
            int bgColor = args.getInt("bgColor", Color.WHITE);
            int fgColor = CommonUtils.blackOrWhiteText(bgColor);

            layout.setBackgroundColor(bgColor);
            title.setText(args.getString("title", null));
            time.setText(Utils.timeFormatterHours(args.getInt("timeSeconds", 0), true));

            title.setTextColor(fgColor);
            time.setTextColor(fgColor);
            actionBtn.setTextColor(fgColor);

            Action action = args.getParcelable("action");
            if (action != null) {
                actionBtn.setVisibility(View.VISIBLE);
                actionBtn.setText(action.textRes);
                actionBtn.setOnClickListener(v -> {
                    startActivity(action.intent);
                    dismiss();
                });
            } else {
                actionBtn.setVisibility(View.GONE);
            }
        }

        return layout;
    }

    public static class Action implements Parcelable {
        public static final Creator<Action> CREATOR = new Creator<Action>() {
            @Override
            public Action createFromParcel(Parcel in) {
                return new Action(in);
            }

            @Override
            public Action[] newArray(int size) {
                return new Action[size];
            }
        };
        private final int textRes;
        private final Intent intent;

        public Action(@StringRes int textRes, @NonNull Intent intent) {
            this.textRes = textRes;
            this.intent = intent;
        }

        protected Action(Parcel in) {
            textRes = in.readInt();
            intent = in.readParcelable(Intent.class.getClassLoader());
        }

        @Override
        public int describeContents() {
            return hashCode();
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(textRes);
            dest.writeParcelable(intent, flags);
        }
    }
}
