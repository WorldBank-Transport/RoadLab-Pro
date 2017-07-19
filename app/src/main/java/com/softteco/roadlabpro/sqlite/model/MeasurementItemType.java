package com.softteco.roadlabpro.sqlite.model;

import com.softteco.roadlabpro.R;
import com.softteco.roadlabpro.util.Constants;

public enum MeasurementItemType {

    INTERVAl(R.drawable.interval_icon, -1, Constants.ROAD_INTERVALS),
    BUMP(R.drawable.bump_icon, R.drawable.ic_map_bump_red, Constants.BUMPS),
    TAG(R.drawable.tag_icon, R.drawable.ic_map_tag_black, Constants.TAGS),
    GEO_TAG(R.drawable.tag_icon, -1, Constants.GEO_TAGS),
    TAG_MEDIA(R.drawable.tag_icon, -1, Constants.TAG_MEDIA),
    SUMMARY(R.drawable.interval_icon, -1, Constants.PROJECT_SUMMARY);

    private int appIcon;
    private int mapIcon;
    private String name;

    private MeasurementItemType(int appIcon, int mapIcon, String name) {
        this.appIcon = appIcon;
        this.name = name;
        this.mapIcon = mapIcon;
    }

    public int getAppIcon() {
        return appIcon;
    }

    public int getMapIcon() {
        return mapIcon;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
