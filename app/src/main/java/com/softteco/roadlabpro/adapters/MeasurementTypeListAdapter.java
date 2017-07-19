package com.softteco.roadlabpro.adapters;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.softteco.roadlabpro.R;
import com.softteco.roadlabpro.util.PreferencesUtil;
import com.softteco.roadlabpro.util.ViewHolder;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by ppp on 09.06.2015.
 */
public class MeasurementTypeListAdapter extends BaseListAdapter {

    private Map<PreferencesUtil.MEASUREMENT_TYPES, Boolean> map;

    public MeasurementTypeListAdapter(Context context, List list) {
        super(context, list);
        map = new HashMap<PreferencesUtil.MEASUREMENT_TYPES, Boolean>();
        initSelection();
    }

    public void initSelection() {
        boolean showIntervals = PreferencesUtil.getInstance().getShowIntervalsFilter();
        boolean showBumps = PreferencesUtil.getInstance().getShowBumpsFilter();
        map.put(PreferencesUtil.MEASUREMENT_TYPES.INTERVAL, showIntervals);
        map.put(PreferencesUtil.MEASUREMENT_TYPES.BUMP, showBumps);
    }

    public void turnSelection(int position) {
        PreferencesUtil.MEASUREMENT_TYPES type =
            (PreferencesUtil.MEASUREMENT_TYPES) getItem(position);
        boolean selection = getSelectionByType(type);
        map.put(type, !selection);
    }

    public void saveSelection() {
        boolean showIntervals = map.get(PreferencesUtil.MEASUREMENT_TYPES.INTERVAL);
        boolean showBumps = map.get(PreferencesUtil.MEASUREMENT_TYPES.BUMP);
        PreferencesUtil.getInstance().setShowIntervalsFilter(showIntervals);
        PreferencesUtil.getInstance().setShowBumpsFilter(showBumps);
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        if (view == null) {
            view = layoutInflater.inflate(R.layout.item_mesurement_types_list, parent, false);
        }
        PreferencesUtil.MEASUREMENT_TYPES type = (PreferencesUtil.MEASUREMENT_TYPES) getItem(position);
        final TextView text = ViewHolder.get(view, R.id.text);
        final ImageView icon = ViewHolder.get(view, R.id.image);
        text.setText(String.format("%s", getMeasurementNameByType(type)));
        boolean selected = getSelectionByType(type);
        setSelection(icon, selected);
        return view;
    }

    private void setSelection(ImageView icon, boolean selected) {
        if (selected) {
            icon.setImageResource(R.drawable.list_import_ok_icon);
        } else {
            icon.setImageResource(0);
        }
    }

    private boolean getSelectionByType(PreferencesUtil.MEASUREMENT_TYPES type) {
        Boolean selection = map.get(type);
        if (selection == null) {
            return false;
        }
        return selection;
    }

    private String getMeasurementNameByType(PreferencesUtil.MEASUREMENT_TYPES id) {
        switch (id) {
            case INTERVAL:
                return getContext().getString(R.string.intervals);
            case BUMP:
                return getContext().getString(R.string.bumps);
        }
        return "";
    }
}
