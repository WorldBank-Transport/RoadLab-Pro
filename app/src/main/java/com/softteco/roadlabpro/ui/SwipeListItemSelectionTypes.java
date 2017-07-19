package com.softteco.roadlabpro.ui;

import com.softteco.roadlabpro.R;

import java.util.ArrayList;
import java.util.List;

public enum SwipeListItemSelectionTypes {

    RENAME(R.string.rename_item_title),
    EDIT(R.string.edit_item_title),
    SEND(R.string.send_item_title),
    SYNC(R.string.sync_item_title),
    MOVE(R.string.move_item_title),
    SELECT(R.string.select_item_title),
    OPEN(R.string.open_item_title),
    CANCEL(R.string.cancel);

    private int nameId;

    public static List<SwipeListItemSelectionTypes> getTypesForProject() {
        List<SwipeListItemSelectionTypes> list = new ArrayList<SwipeListItemSelectionTypes>();
        list.add(RENAME);
        //list.add(SELECT);
        list.add(SEND);
        list.add(SYNC);
        list.add(CANCEL);
        return list;
    }

    public static List<SwipeListItemSelectionTypes> getTypesForRoad() {
        List<SwipeListItemSelectionTypes> list = new ArrayList<SwipeListItemSelectionTypes>();
        list.add(RENAME);
        list.add(MOVE);
        //list.add(SELECT);
        list.add(SEND);
        list.add(SYNC);
        list.add(CANCEL);
        return list;
    }

    public static List<SwipeListItemSelectionTypes> getTypesForTag() {
        List<SwipeListItemSelectionTypes> list = new ArrayList<SwipeListItemSelectionTypes>();
        list.add(EDIT);
        list.add(MOVE);
        list.add(CANCEL);
        return list;
    }

    public static List<SwipeListItemSelectionTypes> getTypesForMeasurement() {
        List<SwipeListItemSelectionTypes> list = new ArrayList<SwipeListItemSelectionTypes>();
        list.add(MOVE);
        list.add(OPEN);
        list.add(CANCEL);
        return list;
    }

    SwipeListItemSelectionTypes(int nameId) {
        this.nameId = nameId;
    }

    public int getNameId() {
        return nameId;
    }
}
