package com.gianlu.timeless.Objects;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.gianlu.timeless.R;
import com.gianlu.timeless.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;

class LoggedEntity {
    public final String name;
    public long total_seconds;

    public LoggedEntity(JSONObject obj) throws JSONException {
        name = obj.getString("name");
        total_seconds = obj.getLong("total_seconds");
    }

    public static void sum(List<LoggedEntity> parents, List<LoggedEntity> children) {
        for (LoggedEntity child : children)
            if (parents.contains(child))
                parents.get(parents.indexOf(child)).total_seconds += child.total_seconds;
            else
                parents.add(child);
    }

    public static long sumSeconds(List<LoggedEntity> entities) {
        long sum = 0;
        for (LoggedEntity entity : entities)
            sum += entity.total_seconds;

        return sum;
    }

    public static LinearLayout createSimpleItem(LayoutInflater inflater, ViewGroup parent, LoggedEntity entity) {
        SimpleViewHolder holder = new SimpleViewHolder((LinearLayout) inflater.inflate(R.layout.logged_entity_item, parent, false));
        holder.name.setText(entity.name);
        holder.time.setText(Utils.timeFormatterHours(entity.total_seconds, true));
        return holder.rootView;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof LoggedEntity) {
            LoggedEntity entity = (LoggedEntity) obj;
            return Objects.equals(entity.name, name);
        }

        return false;
    }

    public static class TotalSecondsComparator implements Comparator<LoggedEntity> {
        @Override
        public int compare(LoggedEntity o1, LoggedEntity o2) {
            if (o1.total_seconds == o2.total_seconds)
                return 0;
            else if (o1.total_seconds > o2.total_seconds)
                return -1;
            else
                return 1;
        }
    }

    private static class SimpleViewHolder {
        final LinearLayout rootView;
        final TextView name;
        final TextView time;

        public SimpleViewHolder(LinearLayout rootView) {
            this.rootView = rootView;

            name = (TextView) rootView.findViewById(R.id.loggedEntityItem_name);
            time = (TextView) rootView.findViewById(R.id.loggedEntityItem_time);
        }
    }
}
