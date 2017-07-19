package com.softteco.roadlabpro.ui;

import android.content.Context;

import com.softteco.roadlabpro.R;
import com.softteco.roadlabpro.adapters.BaseListAdapter;
import com.softteco.roadlabpro.adapters.SwipeItemOptionsListAdapter;

import java.util.List;

public class SwipeListItemOptionsDialog extends SettingsListDialog {

    private List<SwipeListItemSelectionTypes> items;
    private int type = 0;

    public SwipeListItemOptionsDialog(Context context, int type, SettingsDialogListener listener) {
        super(context, listener);
        this.type = type;
    }

    @Override
    protected void initUI() {
        super.initUI();
        setDlgTitle(getContext().getString(R.string.options_title));
    }

    @Override
    protected BaseListAdapter getAdapter() {
        items = getMenuItems();
        return new SwipeItemOptionsListAdapter(getContext(), items);
    }

    private List<SwipeListItemSelectionTypes> getMenuItems() {
        switch (type) {
            case 0:
                return SwipeListItemSelectionTypes.getTypesForProject();
            case 1:
                return SwipeListItemSelectionTypes.getTypesForRoad();
            case 2:
                return SwipeListItemSelectionTypes.getTypesForMeasurement();
            case 3:
                return SwipeListItemSelectionTypes.getTypesForTag();
            default:
                return SwipeListItemSelectionTypes.getTypesForProject();
        }
    }

    @Override
    protected void onListItemClick(int position) {
        SwipeListItemSelectionTypes type = items.get(position);
        if (dialogListener != null) {
            dialogListener.onRequestClose(type.ordinal());
        }
        dismiss();
    }
}
