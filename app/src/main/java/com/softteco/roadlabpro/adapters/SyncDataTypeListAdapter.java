package com.softteco.roadlabpro.adapters;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.softteco.roadlabpro.R;
import com.softteco.roadlabpro.sync.SyncDataType;
import com.softteco.roadlabpro.util.PreferencesUtil;
import com.softteco.roadlabpro.util.ViewHolder;

import java.util.List;

public class SyncDataTypeListAdapter extends BaseListAdapter {

    public SyncDataTypeListAdapter(Context context) {
        super(context);
    }

    public SyncDataTypeListAdapter(Context context, List list) {
        super(context, list);
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        if (view == null) {
            view = layoutInflater.inflate(R.layout.item_list_adapter, parent, false);
        }
        SyncDataType dataType = (SyncDataType) getItem(position);
        final TextView text = ViewHolder.get(view, R.id.text);
        text.setText(getContext().getString(dataType.getNameId()));
        return view;
    }
}
