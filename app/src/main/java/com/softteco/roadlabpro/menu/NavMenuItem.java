package com.softteco.roadlabpro.menu;

import com.softteco.roadlabpro.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Aleksey on 06.05.2015.
 */
public enum NavMenuItem {
    HEADER (R.drawable.ic_menu_quick_start, R.string.title_menu_quick_start, true),
    QUICK_START (R.drawable.ic_menu_quick_start, R.string.title_menu_quick_start, true),
    NEW_PROJECT (R.drawable.ic_menu_add_project, R.string.title_menu_new_project, true),
    NEW_ROAD (R.drawable.ic_menu_add_project, R.string.title_menu_new_road, true),
    SELECT_PROJECT (R.drawable.ic_menu_road_issue, R.string.title_menu_select_project, true),
    TAGS(R.drawable.ic_menu_my_issue, R.string.title_menu_tags, true),
    PROJECTS (R.drawable.ic_menu_road_issue, R.string.title_menu_projects, true),
    MEASUREMENT (R.drawable.ic_menu_summary, R.string.title_menu_new_line_measurement, true),
    LOGIN (R.drawable.ic_menu_my_issue, R.string.title_menu_signin, true),
    SETTINGS (R.drawable.ic_menu_settings, R.string.title_menu_settings, true),
    ABOUT (R.drawable.ic_menu_my_about, R.string.title_menu_about, true);

    private int iconId;
    private int titleId;
    private boolean isShowInMenu;

    NavMenuItem(final int iconId, final int titleId, final boolean isShowInMenu) {
        this.setIconId(iconId);
        this.setTitle(titleId);
        this.setShowInMenu(isShowInMenu);
    }

    public int getIconId() {
        return iconId;
    }

    public void setIconId(final int iconId) {
        this.iconId = iconId;
    }

    public int getTitleId() {
        return titleId;
    }

    public void setTitle(final int i) {
        this.titleId = i;
    }

    private void setShowInMenu(final boolean b) {
        this.isShowInMenu = b;
    }

    public boolean isShowInMenu() {
        return isShowInMenu;
    }

    public static List<NavMenuItem> getItems(boolean quickStart) {
        List<NavMenuItem> items = new ArrayList<NavMenuItem>();
        if (quickStart) {
            items.add(QUICK_START);
        }
        items.add(NEW_ROAD);
        items.add(NEW_PROJECT);
        items.add(SELECT_PROJECT);
        items.add(TAGS);
        items.add(PROJECTS);
        //items.add(MEASUREMENT);
        //items.add(LOGIN);
        items.add(SETTINGS);
        items.add(ABOUT);
        return items;
    }
}
