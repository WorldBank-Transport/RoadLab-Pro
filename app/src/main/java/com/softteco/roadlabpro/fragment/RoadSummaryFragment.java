package com.softteco.roadlabpro.fragment;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.Html;
import android.view.View;
import android.widget.TextView;

import com.softteco.roadlabpro.R;
import com.softteco.roadlabpro.menu.ScreenItems;
import com.softteco.roadlabpro.sqlite.MeasurementsDataHelper;
import com.softteco.roadlabpro.sqlite.model.FolderModel;
import com.softteco.roadlabpro.util.Constants;
import com.softteco.roadlabpro.util.DateUtil;
import com.softteco.roadlabpro.util.DistanceUtil;

import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Created by bogdan on 06.04.2016.
 */
public class RoadSummaryFragment extends AbstractWBFragment {

    private final String TAG = RoadSummaryFragment.class.getName();

    private FolderModel folder;

    private TextView projectDate;
    private TextView projectRoads;
    private TextView projectDistance;
    private TextView projectIri;
    private TextView projectSpeed;
    private TextView badQualityText;
    private TextView normalQualityText;
    private TextView goodQualityText;
    private TextView perfectQualityText;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        checkArgs();
    }

    private void checkArgs() {
        if (getArguments() != null && getArguments().getSerializable(RoadListFragment.ARG_ROAD_LIST) != null) {
            folder = (FolderModel) getArguments().getSerializable(RoadListFragment.ARG_ROAD_LIST);
        }
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        projectDate = (TextView) view.findViewById(R.id.project_time_value);
        projectRoads = (TextView) view.findViewById(R.id.project_roads_value);
        projectDistance = (TextView) view.findViewById(R.id.project_distance_value);
        projectIri = (TextView) view.findViewById(R.id.project_iri_value);
        projectSpeed = (TextView) view.findViewById(R.id.project_speed_value);
        badQualityText = (TextView) view.findViewById(R.id.badQualityText);
        normalQualityText = (TextView) view.findViewById(R.id.normalQualityText);
        goodQualityText = (TextView) view.findViewById(R.id.goodQualityText);
        perfectQualityText = (TextView) view.findViewById(R.id.perfectQualityText);
        fillUI();
    }

    private void fillUI() {
        if (folder == null) {
            return;
        }
        Date projDate = new Date(folder.getDate());
        String projDateStr = DateUtil.format(projDate, DateUtil.Format.DDMMMMYYYYHHMM);
        projectDate.setText(projDateStr);
        String roadsCount = String.valueOf(folder.getRoads());
        projectRoads.setText(roadsCount);
        double overallDistance = folder.getOverallDistance();
        double pathDistance = folder.getPathDistance();
        String projectDistanceStr = MeasurementsDataHelper.
               getDistanceStrHtml(getContext(), overallDistance, pathDistance);
        projectDistance.setText(Html.fromHtml(projectDistanceStr));
        double averageIri = folder.getAverageIRI();
        double averageSpeed = folder.getAverageSpeed();
        String averageIriStr = MeasurementsDataHelper.getInstance().getIRIStr(averageIri, false);
        String averageSpeedStr = getString(R.string.speed_format_str,
        String.format(Locale.US, "%2.0f", averageSpeed));
        projectIri.setText(averageIriStr);
        projectSpeed.setText(averageSpeedStr);
        refreshProjectStats();
    }

    protected void refreshPercentValues(Float[] stats) {
        badQualityText.setText(String.format(Constants.PERCENTS_FLOAT_FORMAT, stats[0]));
        normalQualityText.setText(String.format(Constants.PERCENTS_FLOAT_FORMAT, stats[1]));
        goodQualityText.setText(String.format(Constants.PERCENTS_FLOAT_FORMAT, stats[2]));
        perfectQualityText.setText(String.format(Constants.PERCENTS_FLOAT_FORMAT, stats[3]));
    }

    protected void refreshProjectStats() {
        new AsyncTask<Object, Object, Float[]>() {
            @Override
            protected Float[] doInBackground(Object... params) {
                float[] stats = MeasurementsDataHelper.getInstance().getRoughnessStatisticsPercent(folder.getId(), -1, -1);
                Float[] newStats = new Float[4];
                if (stats != null && stats.length >= 4) {
                    newStats[0] = stats[0];
                    newStats[1] = stats[1];
                    newStats[2] = stats[2];
                    newStats[3] = stats[3];
                }
                return newStats;
            }
            @Override
            protected void onPostExecute(Float[] stats) {
                refreshPercentValues(stats);
            }
        }.execute();
    }

    @Override
    public int getLayoutFragmentResources() {
        return R.layout.fragment_road_summary;
    }

    @Override
    public int getMenuFragmentResources() {
        return -1;
    }

    @Override
    public int getTypeFragment() {
        return ScreenItems.SCREEN_PROJECT_SUMMARY;
    }

    @Override
    public boolean isHomeIndicatorMenu() {
        return false;
    }
}
