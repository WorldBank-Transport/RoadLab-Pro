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
public class TimeIntervalListAdapter extends BaseListAdapter {

    public TimeIntervalListAdapter(Context context) {
        super(context);
    }

    public TimeIntervalListAdapter(Context context, List list) {
        super(context, list);
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        if (view == null) {
            view = layoutInflater.inflate(R.layout.item_list_adapter, parent, false);
        }
        PreferencesUtil.TIMEINTERVAL interval = (PreferencesUtil.TIMEINTERVAL) getItem(position);
        final TextView text = ViewHolder.get(view, R.id.text);
        text.setText(getContext().getString(R.string.ms_format, String.valueOf(interval.getTimeInterval())));
        return view;
    }
}
