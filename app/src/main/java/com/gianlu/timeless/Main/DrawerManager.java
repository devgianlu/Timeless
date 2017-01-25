package com.gianlu.timeless.Main;

import android.app.Activity;
import android.content.res.Configuration;
import android.graphics.Color;
import android.support.annotation.ColorRes;
import android.support.annotation.DrawableRes;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.gianlu.timeless.LetterIcon;
import com.gianlu.timeless.NetIO.User;
import com.gianlu.timeless.R;

public class DrawerManager {
    private final Activity context;
    private final DrawerLayout drawerLayout;
    private final LinearLayout drawerList;
    private final LinearLayout drawerFooterList;
    private IDrawerListener listener;
    private ActionBarDrawerToggle actionBarDrawerToggle;

    public DrawerManager(Activity context, DrawerLayout drawerLayout) {
        this.context = context;
        this.drawerLayout = drawerLayout;
        this.drawerList = (LinearLayout) drawerLayout.findViewById(R.id.mainDrawer_list);
        this.drawerFooterList = (LinearLayout) drawerLayout.findViewById(R.id.mainDrawer_footerList);

        drawerLayout.findViewById(R.id.mainDrawerHeader_logOut).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (listener != null)
                    listener.onLogOut();
            }
        });

        context.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        context.getWindow().setStatusBarColor(Color.TRANSPARENT);
    }

    public DrawerManager setToolbar(Toolbar toolbar) {
        actionBarDrawerToggle = new ActionBarDrawerToggle(context, drawerLayout, toolbar, R.string.openDrawer, R.string.closeDrawer);
        actionBarDrawerToggle.setDrawerIndicatorEnabled(true);
        actionBarDrawerToggle.syncState();
        return this;
    }

    public void syncTogglerState() {
        if (actionBarDrawerToggle != null)
            actionBarDrawerToggle.syncState();
    }

    public void onTogglerConfigurationChanged(Configuration conf) {
        if (actionBarDrawerToggle != null)
            actionBarDrawerToggle.onConfigurationChanged(conf);
    }

    public DrawerManager setUser(User user) {
        ((LetterIcon) drawerLayout.findViewById(R.id.mainDrawerHeader_currentAccount)).setUser(user);
        ((TextView) drawerLayout.findViewById(R.id.mainDrawerHeader_profileUsername)).setText(user.username == null ? context.getString(R.string.noUsername) : user.username);
        ((TextView) drawerLayout.findViewById(R.id.mainDrawerHeader_profileEmail)).setText(user.email);

        return this;
    }

    public DrawerManager setDrawerListener(IDrawerListener listener) {
        this.listener = listener;
        return this;
    }

    private void setDrawerState(boolean open, boolean animate) {
        if (open)
            drawerLayout.openDrawer(GravityCompat.START, animate);
        else
            drawerLayout.closeDrawer(GravityCompat.START, animate);
    }

    private View newItem(@DrawableRes int icon, String title, boolean primary) {
        return newItem(icon, title, primary, -1, -1);
    }

    private View newItem(@DrawableRes int icon, String title, boolean primary, @ColorRes int textColorRes, @ColorRes int tintRes) {
        int textColor;
        if (textColorRes != -1)
            textColor = ContextCompat.getColor(context, textColorRes);
        else if (primary)
            textColor = Color.BLACK;
        else
            textColor = ContextCompat.getColor(context, R.color.colorPrimary_ripple);

        View view = View.inflate(context, R.layout.material_drawer_item_primary, null);
        if (tintRes != -1)
            view.setBackgroundColor(ContextCompat.getColor(context, tintRes));
        ((ImageView) view.findViewById(R.id.materialDrawer_itemIcon)).setImageResource(icon);
        ((TextView) view.findViewById(R.id.materialDrawer_itemName)).setText(title);
        ((TextView) view.findViewById(R.id.materialDrawer_itemName)).setTextColor(textColor);

        return view;
    }

    public DrawerManager buildMenu() {
        drawerList.removeAllViews();

        View home = newItem(R.drawable.ic_home_black_48dp, context.getString(R.string.home), true, R.color.colorAccent, R.color.colorPrimary_drawerSelected);
        home.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (listener != null)
                    setDrawerState(false, listener.onListItemSelected(DrawerListItems.HOME));
            }
        });
        drawerList.addView(home, 0);

        // Footer group
        drawerFooterList.removeAllViews();

        View divider = new View(context);
        divider.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 2));
        divider.setBackgroundResource(R.color.colorPrimary_ripple);
        drawerFooterList.addView(divider, 0);

        View preferences = newItem(R.drawable.ic_settings_black_48dp, context.getString(R.string.preferences), false);
        preferences.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (listener != null)
                    setDrawerState(false, listener.onListItemSelected(DrawerListItems.PREFERENCES));
            }
        });
        drawerFooterList.addView(preferences);

        View support = newItem(R.drawable.ic_report_problem_black_48dp, context.getString(R.string.support), false);
        support.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (listener != null)
                    setDrawerState(false, listener.onListItemSelected(DrawerListItems.SUPPORT));
            }
        });
        drawerFooterList.addView(support);

        return this;
    }

    public enum DrawerListItems {
        HOME,
        PREFERENCES,
        SUPPORT
    }

    public interface IDrawerListener {
        boolean onListItemSelected(DrawerListItems which);

        void onLogOut();
    }
}
