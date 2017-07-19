package com.softteco.roadlabpro.adapters;

import android.content.Context;
import android.text.TextUtils;
import android.util.TimeUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.softteco.roadlabpro.R;
import com.softteco.roadlabpro.ui.SaveMeasurementSelectionTypes;
import com.softteco.roadlabpro.util.TimeUtil;
import com.softteco.roadlabpro.util.ViewHolder;
import com.softteco.roadlabpro.view.TypefaceTextView;

import java.util.List;

/**
 * Created by ppp on 09.06.2015.
 */
public class SaveMeasurementListAdapter extends BaseListAdapter {

    private CharSequence defaultItemTitle;
    private long defaultItemTime;

    public SaveMeasurementListAdapter(Context context, List list) {
        super(context, list);
    }

    public void refreshDefaultItem(CharSequence title, long time) {
        this.defaultItemTitle = title;
        this.defaultItemTime = time;
        notifyDataSetChanged();
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        if (view == null) {
            view = layoutInflater.inflate(R.layout.item_list_save_to_adapter, parent, false);
        }
        SaveMeasurementSelectionTypes type = (SaveMeasurementSelectionTypes) getItem(position);
        TypefaceTextView titleText = ViewHolder.get(view, R.id.titleText);
        TypefaceTextView timeText = ViewHolder.get(view, R.id.timeText);
        if (position == 0) {
            String timeStr = TimeUtil.formatMillisMinSecFmt(defaultItemTime);
            timeText.setVisibility(View.VISIBLE);
            String boldText = getContext().getString(R.string.font_roboto_bold);
            titleText.setFont(boldText);
            setDefaultItemTitle(titleText);
            timeText.setText(timeStr);
        } else {
            timeText.setVisibility(View.GONE);
            String regularText = getContext().getString(R.string.font_roboto_regular);
            titleText.setFont(regularText);
            titleText.setText(getContext().getString(type.getNameId()));
        }
        return view;
    }

    private void setDefaultItemTitle(TextView titleText) {
        if (TextUtils.isEmpty(defaultItemTitle)) {
            defaultItemTitle = getContext().getString(R.string.default_folder_name) + " - "
            + getContext().getString(R.string.default_road_name);
        }
        titleText.setText(defaultItemTitle);
    }
}
