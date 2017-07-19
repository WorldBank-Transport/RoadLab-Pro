package com.softteco.roadlabpro.adapters;

import android.content.Context;
import android.database.Cursor;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.softteco.roadlabpro.R;
import com.softteco.roadlabpro.sqlite.MeasurementsDataHelper;
import com.softteco.roadlabpro.sqlite.dao.MeasurementDAO;
import com.softteco.roadlabpro.sqlite.model.MeasurementItem;
import com.softteco.roadlabpro.sqlite.model.MeasurementModel;
import com.softteco.roadlabpro.util.DateUtil;
import com.softteco.roadlabpro.util.DistanceUtil;
import com.softteco.roadlabpro.util.FileUtils;

import java.util.Date;

public class MeasurementAdapter extends CursorAdapter {

    private final String TAG = MeasurementAdapter.class.getName();

    private MeasurementDAO measurementDAO;

    public MeasurementAdapter(Context context, Cursor c, int flags, MeasurementDAO dao, long folderId) {
        super(context, c, flags);
        this.measurementDAO = dao;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.item_link_list, parent, false);
    }

    @Override
    public void bindView(View view, final Context context, final Cursor cursor) {
        final TextView roadName = (TextView) view.findViewById(R.id.road_list_title);
        final TextView measurementDetailsText = (TextView) view.findViewById(R.id.road_list_details);
        final TextView txtDate = (TextView) view.findViewById(R.id.road_list_date);
        final ImageView imageView = (ImageView) view.findViewById(R.id.road_list_item_image);
        final MeasurementModel model = measurementDAO.cursorToMeasurement(cursor);
        String distanceStr = MeasurementsDataHelper.
                getDistanceStrHtml(context, model.getOverallDistance(), model.getPathDistance());
        String detailsStr = context.getResources().
        getString(R.string.list_item_meas_details, model.getIntervalsNumber(), distanceStr);
        measurementDetailsText.setText(Html.fromHtml(detailsStr));
        String measurementFmtStr = FileUtils.getExportItemName(context, model.getId(), model.getTime());
        roadName.setText(measurementFmtStr);
        String avgIriStr = getIri(context, model);
        txtDate.setText(avgIriStr);
        imageView.setVisibility(model.isUploaded() ? View.VISIBLE : View.INVISIBLE);
    }

    private String getIri(Context context, MeasurementModel model) {
        return MeasurementsDataHelper.getInstance().getIRIStr(model.getAvgIRI());
    }

    @Override
    public void changeCursor(Cursor cursor) {
        super.changeCursor(cursor);
    }
}