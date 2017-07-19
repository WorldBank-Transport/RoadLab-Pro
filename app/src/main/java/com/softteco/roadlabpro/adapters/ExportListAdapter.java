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
public class ExportListAdapter extends BaseListAdapter {

    public ExportListAdapter(Context context) {
        super(context);
    }

    public ExportListAdapter(Context context, List list) {
        super(context, list);
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        if (view == null) {
            view = layoutInflater.inflate(R.layout.item_list_adapter, parent, false);
        }
        PreferencesUtil.EXPORT_TYPES export = (PreferencesUtil.EXPORT_TYPES) getItem(position);
        final TextView text = ViewHolder.get(view, R.id.text);
        text.setText(String.format("%s", getExportNameByType(export)));
        return view;
    }

    private String getExportNameByType(PreferencesUtil.EXPORT_TYPES type) {
        switch (type) {
            case LOCAL:
                return getContext().getString(R.string.save_to_device);
            case DROPBOX:
                String dataTypeName = PreferencesUtil.getInstance().getSyncProviderTypeName(getContext());
                return getContext().getString(R.string.save_to_dropbox, dataTypeName);
        }
        return "";
    }
}
