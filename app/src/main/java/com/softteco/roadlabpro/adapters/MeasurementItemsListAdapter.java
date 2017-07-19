package com.softteco.roadlabpro.adapters;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.softteco.roadlabpro.R;
import com.softteco.roadlabpro.sqlite.MeasurementsDataHelper;
import com.softteco.roadlabpro.sqlite.model.MeasurementItem;
import com.softteco.roadlabpro.sqlite.model.MeasurementItemType;
import com.softteco.roadlabpro.sqlite.model.MeasurementModel;
import com.softteco.roadlabpro.util.DateUtil;
import com.softteco.roadlabpro.util.FileUtils;

import java.util.Date;
import java.util.List;

public class MeasurementItemsListAdapter extends BaseListAdapter {

    private MeasurementModel measurementModel;

    public MeasurementItemsListAdapter(Context context, List list, MeasurementModel measurementModel) {
        super(context, list);
        this.measurementModel = measurementModel;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        if (view == null) {
            view = layoutInflater.inflate(R.layout.measurement_item_list_item, parent, false);
        }
        MeasurementItem item = (MeasurementItem) getItem(position);
        ImageView icon = (ImageView) view.findViewById(R.id.icon);
        final TextView titleText = (TextView) view.findViewById(R.id.measurement_item_title);
        final TextView detailsText = (TextView) view.findViewById(R.id.measurement_item_details);
        final TextView iriText = (TextView) view.findViewById(R.id.measurement_item_iri);
        String titleStr = getTitle(item);
        String descrStr = getDescription(item);
        String iriStr = getIri(item);
        titleText.setText(titleStr);
        detailsText.setText(descrStr);
        iriText.setText(iriStr);
        MeasurementItemType type = item.getType();
        if (type != null) {
            int iconId = type.getAppIcon();
            icon.setImageResource(iconId);
        }
        return view;
    }

    private String getTitle(MeasurementItem item) {
        //return MeasurementsDataHelper.getInstance().getItemTitleStr(item);
        return FileUtils.getExportItemName(getContext(), item.getId(), item.getTime());
    }

    private String getDescription(MeasurementItem item) {
        return item.getDescription();
    }

    private String getIri(MeasurementItem item) {
        return MeasurementsDataHelper.getInstance().getIRIStr(item.getIri());
    }
}
