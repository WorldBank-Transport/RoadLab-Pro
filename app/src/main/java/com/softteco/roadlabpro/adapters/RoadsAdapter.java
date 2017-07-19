package com.softteco.roadlabpro.adapters;

import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.softteco.roadlabpro.R;
import com.softteco.roadlabpro.sqlite.MeasurementsDataHelper;
import com.softteco.roadlabpro.sqlite.dao.RoadDAO;
import com.softteco.roadlabpro.sqlite.model.MeasurementItemType;
import com.softteco.roadlabpro.sqlite.model.RoadModel;
import com.softteco.roadlabpro.util.Constants;
import com.softteco.roadlabpro.util.DateUtil;
import com.softteco.roadlabpro.util.DistanceUtil;

import java.util.Date;

public class RoadsAdapter extends CursorAdapter {

    private final String TAG = RoadsAdapter.class.getName();

    private RoadDAO roadDAO;

    public RoadsAdapter(Context context, Cursor c, int flags, RoadDAO dao, long folderId) {
        super(context, c, flags);
        this.roadDAO = dao;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.item_road_list, parent, false);
    }

    @Override
    public void bindView(View view, final Context context, final Cursor cursor) {
        final TextView roadName = (TextView) view.findViewById(R.id.road_list_title);
        final TextView roadDetailsText = (TextView) view.findViewById(R.id.road_list_details);
        final TextView txtDate = (TextView) view.findViewById(R.id.road_list_date);
        final ImageView imageView = (ImageView) view.findViewById(R.id.road_list_item_image);
        TextView avgIRI = (TextView) view.findViewById(R.id.average_iri_text_text);
        final ViewGroup statsLayout = (ViewGroup) view.findViewById(R.id.stats_layout);
        final RoadModel model = roadDAO.cursorToRoad(cursor);
        roadName.setText(model.getName());
        double avgIriValue = model.getAverageIRI();
        String avgIriStr = MeasurementsDataHelper.getInstance().getIRIStr(avgIriValue);
        avgIRI.setText(avgIriStr);
        String distanceStr = MeasurementsDataHelper.
                getDistanceStrHtml(context, model.getOverallDistance(), model.getPathDistance());
        String detailsStr = context.getResources().
        getString(R.string.list_item_road_details, model.getExperiments(), distanceStr);
        roadDetailsText.setText(Html.fromHtml(detailsStr));
        txtDate.setText(DateUtil.format(new Date(model.getDate()), DateUtil.Format.DDMMYYY));
        imageView.setVisibility(model.isUploaded() ? View.VISIBLE : View.INVISIBLE);
        showRoadStatistics(statsLayout, model);
    }

    private void showRoadStatistics(ViewGroup layout, RoadModel road) {
        TextView statBadText = (TextView) layout.findViewById(R.id.stat_bad_text);
        TextView statNormalText = (TextView) layout.findViewById(R.id.stat_normal_text);
        TextView statGoodText = (TextView) layout.findViewById(R.id.stat_good_text);
        TextView statPerfectText = (TextView) layout.findViewById(R.id.stat_perfect_text);
        String badIssuesStr = String.format(Constants.PERCENTS_FLOAT_FORMAT, road.getStatBadIssues());
        String normalIssuesStr = String.format(Constants.PERCENTS_FLOAT_FORMAT, road.getStatNormalIssues());
        String goodIssuesStr = String.format(Constants.PERCENTS_FLOAT_FORMAT, road.getStatGoodIssues());
        String perfectIssuesStr = String.format(Constants.PERCENTS_FLOAT_FORMAT, road.getStatPerfectIssues());
        statBadText.setText(badIssuesStr);
        statNormalText.setText(normalIssuesStr);
        statGoodText.setText(goodIssuesStr);
        statPerfectText.setText(perfectIssuesStr);
    }

    @Override
    public void changeCursor(Cursor cursor) {
        super.changeCursor(cursor);
    }
}