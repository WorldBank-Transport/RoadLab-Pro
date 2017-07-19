package com.softteco.roadlabpro.fragment;

import android.view.View;

import com.google.android.gms.maps.model.LatLng;
import com.softteco.roadlabpro.R;
import com.softteco.roadlabpro.RAApplication;
import com.softteco.roadlabpro.algorithm.RoadQuality;
import com.softteco.roadlabpro.sqlite.MeasurementsDataHelper;
import com.softteco.roadlabpro.sqlite.model.MeasurementItem;
import com.softteco.roadlabpro.sqlite.model.ProcessedDataModel;
import com.softteco.roadlabpro.util.ActivityUtil;
import com.softteco.roadlabpro.util.ScheduleTimer;
import com.softteco.roadlabpro.util.UIUtils;

import java.util.ArrayList;
import java.util.List;

public class StartMeasurementMapFragment extends BaseStartMeasurementMapFragment
    implements ScheduleTimer.TimerTaskListener, SummaryFragment.OnDeviceFixedListener {

    protected void initUI() {
        timer = new ScheduleTimer(REFRESH_INTERVAL);
        timer.setOnTimerTaskListener(this);
        init();
    }

    @Override
    protected void setManualModeScreen(final boolean enabled, boolean isAutoMode) {
        enableMap(enabled);
        setGoToLocation(false);
        setRefreshMyLocation(false);
        if (enabled) {
            int blackColor = getAppResouces().getColor(android.R.color.black);
            distanceValueText.setTextColor(blackColor);
            speedValueText.setTextColor(blackColor);
            itemsCntValueText.setTextColor(blackColor);
            intervalsCntValueText.setTextColor(blackColor);
            bumpValueText.setTextColor(getAppResouces().getColor(R.color.bumps_color));
            UIUtils.getTintedDrawable(bumpValueText.getBackground(), getAppResouces().getColor(R.color.bumps_color));
        } else {
            deviceIndicatorAngleBtn.setVisibility(View.GONE);
            gpsIndicatorBtn.setVisibility(View.GONE);
            setRoughnessIndicator(roadIntervalValue, RoadQuality.NONE);
            int greyColor = getAppResouces().getColor(android.R.color.secondary_text_light_nodisable);
            distanceValueText.setTextColor(greyColor);
            speedValueText.setTextColor(greyColor);
            itemsCntValueText.setTextColor(greyColor);
            intervalsCntValueText.setTextColor(greyColor);
            bumpValueText.setTextColor(greyColor);
            UIUtils.getTintedDrawable(bumpValueText.getBackground(), greyColor);
            if (!isAutoMode) {
                bumpValueText.setText(DASH_TEXT);
                distanceValueText.setText(DASH_TEXT);
                speedValueText.setText(DASH_TEXT);
                itemsCntValueText.setText(DASH_TEXT);
                intervalsCntValueText.setText(DASH_TEXT);
                String intervalStr = RoadQuality.NONE.toString();
                roadIntervalValue.setText(intervalStr);
            }
            clearMap();
        }
    }

    protected void initMapData() {
        boolean isRecordEnabled = isRecordEnabled();
        enableMap(isRecordEnabled);
    }

    @Override
    protected void refreshData(final boolean refreshGraph) {
        if (!isAdded() || !isRecordEnabled()) {
            return;
        }
        long measurementId = RAApplication.getInstance().getCurrentMeasurementId();
        ProcessedDataModel processedData =
        MeasurementsDataHelper.getInstance().getLastInterval(measurementId);
        long bumpsCount = MeasurementsDataHelper.getInstance().getBumpsCount(measurementId);
        final int bumps = (int) bumpsCount;
        final RoadQuality quality = processedData != null ? processedData.getCategory() : RoadQuality.NONE;
        List<MeasurementItem> localMeasurementDataList = null;
        if (refreshGraph) {
            localMeasurementDataList =
            MeasurementsDataHelper.getInstance().getMeasurementData(measurementId, false);
        }
        final List<MeasurementItem> measurementDataList = localMeasurementDataList;
        ActivityUtil.runOnMainThread(new Runnable() {
            @Override
            public void run() {
                if (isAdded() && isRecordEnabled()) {
                    bumpValueText.setText(String.valueOf(bumps));
                    String intervalStr = quality.toString();
                    roadIntervalValue.setText(intervalStr);
                    if (refreshGraph) {
                        refreshMap(measurementDataList);
                        setRoughnessIndicator(roadIntervalValue, quality);
                        setManualModeScreen(true);
                    } else {
                        clearMap();
                        refreshMyLocation();
                    }
                }
            }
        });
    }

    private void refreshMap(List<MeasurementItem> items) {
        if (getMap() != null) {
            if (items != null) {
                boolean hasNewMarkers = false;
                List<LatLng> locations = new ArrayList<>();
                for (MeasurementItem item : items) {
                    locations.add(new LatLng(item.getLatitude(), item.getLongitude()));
                    if (!getMapHelper().isMarkerItemAlreadyAdded(item)) {
                        getMapHelper().addMarker(getContext(), getMap(), item, true);
                        hasNewMarkers = true;
                    }
                }
                if (hasNewMarkers) {
                    LatLng loc = getMyLocation();
                    if (loc != null) {
                        locations.add(loc);
                    }
                    fitMap(locations, true, getPadding());
                    refreshMyLocation();
                } else {
                    locations.clear();
                }
            }
        }
    }

    @Override
    public void onDeviceFixed(boolean isDeviceFixed) {
    }

    @Override
    protected void initGraph(final boolean isEmpty) {
    }

    @Override
    public int getLayoutFragmentResources() {
        return R.layout.fragment_all_summary_content;
    }
}
