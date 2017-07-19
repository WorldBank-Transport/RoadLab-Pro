package com.softteco.roadlabpro.adapters;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.softteco.roadlabpro.R;
import com.softteco.roadlabpro.util.PreferencesUtil;
import com.softteco.roadlabpro.util.ViewHolder;

import java.util.List;

/**
 * Created by ppp on 09.06.2015.
 */
public class SyncModeListAdapter extends BaseListAdapter {

    public SyncModeListAdapter(Context context) {
        super(context);
    }

    public SyncModeListAdapter(Context context, List list) {
        super(context, list);
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        if (view == null) {
            view = layoutInflater.inflate(R.layout.item_list_adapter, parent, false);
        }
        PreferencesUtil.SYNC_MODES syncMode = (PreferencesUtil.SYNC_MODES) getItem(position);
        final TextView text = ViewHolder.get(view, R.id.text);
        text.setText(String.format("%s", getSuspensionNameByType(syncMode)));
        return view;
    }

    private String getSuspensionNameByType(PreferencesUtil.SYNC_MODES type) {
        switch (type) {
            case AUTO:
                return getContext().getString(R.string.always);
            case WIFI:
                return getContext().getString(R.string.wifi_only);
            case MANUAL:
                return getContext().getString(R.string.off);
        }
        return "";
    }
}
