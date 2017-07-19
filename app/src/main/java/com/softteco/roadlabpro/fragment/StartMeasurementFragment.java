package com.softteco.roadlabpro.fragment;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.androidplot.ui.AnchorPosition;
import com.androidplot.ui.XLayoutStyle;
import com.androidplot.ui.YLayoutStyle;
import com.androidplot.util.PixelUtils;
import com.androidplot.xy.BoundaryMode;
import com.androidplot.xy.SimpleXYSeries;
import com.androidplot.xy.XYPlot;
import com.softteco.roadlabpro.R;
import com.softteco.roadlabpro.RAApplication;
import com.softteco.roadlabpro.algorithm.RoadQuality;
import com.softteco.roadlabpro.sensors.IntervalsRecordHelper;
import com.softteco.roadlabpro.sqlite.MeasurementsDataHelper;
import com.softteco.roadlabpro.sqlite.dao.ProcessedDataDAO;
import com.softteco.roadlabpro.sqlite.model.FolderModel;
import com.softteco.roadlabpro.sqlite.model.ProcessedDataModel;
import com.softteco.roadlabpro.sqlite.model.RoadModel;
import com.softteco.roadlabpro.ui.SaveMeasurementSelectionTypes;
import com.softteco.roadlabpro.ui.SaveMeasurementToDialog;
import com.softteco.roadlabpro.ui.SelectProjectDialog;
import com.softteco.roadlabpro.ui.SettingsDialog;
import com.softteco.roadlabpro.ui.SplineLineAndPointFormatter;
import com.softteco.roadlabpro.util.ActivityUtil;
import com.softteco.roadlabpro.util.Constants;
import com.softteco.roadlabpro.util.PreferencesUtil;
import com.softteco.roadlabpro.util.ScheduleTimer;
import com.softteco.roadlabpro.util.UIUtils;

import java.text.DecimalFormat;
import java.text.FieldPosition;
import java.text.NumberFormat;
import java.text.ParsePosition;
import java.util.ArrayList;
import java.util.List;

public class StartMeasurementFragment extends BaseStartMeasurementFragment
    implements View.OnClickListener, ScheduleTimer.TimerTaskListener, SummaryFragment.OnDeviceFixedListener {

    protected TextView distanceValueText;
    protected TextView speedText;
    protected TextView speedValueText;
    protected TextView bumpValueText;
    protected TextView badText;
    protected TextView badQualityText;
    protected TextView normalText;
    protected TextView normalQualityText;
    protected TextView goodText;
    protected TextView goodQualityText;
    protected TextView roadIntervalValue;
    protected TextView roadIntervalText;
    protected TextView perfectText;
    protected TextView perfectQualityText;
    protected TextView recordStatusValue;
    protected TextView itemsCntValueText;
    protected TextView intervalsCntValueText;
    protected ImageView deviceIndicatorAngleBtn;
    protected ImageView gpsIndicatorBtn;
    private Button startMeasurementsBtn;
    private XYPlot xyPlot;
    SimpleXYSeries series = new SimpleXYSeries(null, SimpleXYSeries.ArrayFormat.Y_VALS_ONLY, "");
    protected ScheduleTimer timer;
    protected ScheduleTimer refreshGraphTimer;
    protected ProcessedDataDAO processedDao;
    private boolean isDeviceFixed = true;

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        xyPlot = (XYPlot) view.findViewById(R.id.accelerationGraph);
        badText = (TextView) view.findViewById(R.id.badText);
        normalText = (TextView) view.findViewById(R.id.normalText);
        goodText = (TextView) view.findViewById(R.id.goodText);
        perfectText = (TextView) view.findViewById(R.id.perfectText);
        distanceValueText = (TextView) view.findViewById(R.id.distanceValueText);
        speedText = (TextView) view.findViewById(R.id.speedText);
        speedValueText = (TextView) view.findViewById(R.id.speedValueText);
        bumpValueText = (TextView) view.findViewById(R.id.bumpValueText);
        badQualityText = (TextView) view.findViewById(R.id.badQualityText);
        normalQualityText = (TextView) view.findViewById(R.id.normalQualityText);
        goodQualityText = (TextView) view.findViewById(R.id.goodQualityText);
        perfectQualityText = (TextView) view.findViewById(R.id.perfectQualityText);
        deviceIndicatorAngleBtn = (ImageView) view.findViewById(R.id.wrong_angle_icon);
        gpsIndicatorBtn = (ImageView) view.findViewById(R.id.wrong_gps_icon);
        startMeasurementsBtn = (Button) view.findViewById(R.id.startMeasurementsBtn);
        roadIntervalValue = (TextView) view.findViewById(R.id.roadIntervalValue);
        roadIntervalText = (TextView) view.findViewById(R.id.roadIntervalText);
        recordStatusValue = (TextView) view.findViewById(R.id.recordStatusValue);
        itemsCntValueText = (TextView) view.findViewById(R.id.itemsCntValueText);
        intervalsCntValueText = (TextView) view.findViewById(R.id.intervalsCntValueText);
        initUI();
    }

    protected void initUI() {
        processedDao = new ProcessedDataDAO(getActivity());
        timer = new ScheduleTimer(REFRESH_INTERVAL);
        timer.setOnTimerTaskListener(this);
        refreshGraphTimer = new ScheduleTimer(REFRESH_GRAPH_INTERVAL);
        refreshGraphTimer.setOnTimerTaskListener(new ScheduleTimer.TimerTaskListener() {
            @Override
            public void onTimer() {
                if (isAdded()) {
                    List<Double> avList = null;
                    if (getIntervalsRecordHelper() != null) {
                        avList = getIntervalsRecordHelper().getRecentLinearAccValues();
                    }
                    refreshAccelerationGraphUi(avList);
                }
            }
        });
        init();
    }

    protected void initGraph(final boolean isEmpty) {
        int greyColor = getAppResouces().getColor(android.R.color.secondary_text_light_nodisable);
        xyPlot.getGraphWidget().getDomainGridLinePaint().setColor(greyColor);
        xyPlot.getGraphWidget().getRangeSubGridLinePaint().setColor(greyColor);
        xyPlot.getGraphWidget().getRangeGridLinePaint().setColor(greyColor);
        xyPlot.getGraphWidget().getDomainSubGridLinePaint().setColor(greyColor);
        xyPlot.getBackgroundPaint().setColor(Color.WHITE);
        xyPlot.setPlotMargins(0, 0, 0, 0);
        xyPlot.getGraphWidget().getGridBackgroundPaint().setColor(Color.WHITE);
        xyPlot.getGraphWidget().getBackgroundPaint().setColor(Color.WHITE);
        xyPlot.getGraphWidget().getCursorLabelBackgroundPaint().setColor(greyColor);
        xyPlot.getGraphWidget().getDomainOriginLabelPaint().setColor(Color.WHITE);
        xyPlot.getGraphWidget().getRangeOriginLabelPaint().setColor(greyColor);
        xyPlot.getGraphWidget().getDomainOriginLinePaint().setColor(greyColor);
        xyPlot.getGraphWidget().getRangeOriginLinePaint().setColor(greyColor);
        xyPlot.getGraphWidget().getDomainLabelPaint().setColor(greyColor);
        xyPlot.getGraphWidget().getRangeLabelPaint().setColor(greyColor);
        xyPlot.getDomainLabelWidget().getLabelPaint().setColor(greyColor);
        xyPlot.getRangeLabelWidget().getLabelPaint().setColor(greyColor);
        xyPlot.getDomainLabelWidget().position(0, XLayoutStyle.ABSOLUTE_FROM_CENTER,
                0, YLayoutStyle.RELATIVE_TO_BOTTOM, AnchorPosition.BOTTOM_MIDDLE);
        xyPlot.getTitleWidget().getLabelPaint().setColor(Color.TRANSPARENT);
        xyPlot.getTitleWidget().setVisible(false);
        xyPlot.getLegendWidget().setVisible(false);

        float domainLabelWidth = getAppResouces().getDimensionPixelSize(R.dimen.domainLabelWidth);
        float rangeLabelWidth = getAppResouces().getDimensionPixelSize(R.dimen.rangeLabelWidth);
        float rangeLabelVerticalOffset = getAppResouces().getDimensionPixelSize(R.dimen.rangeLabelVerticalOffset);

        float domainLabelMarginLeft = getAppResouces().getDimensionPixelSize(R.dimen.domainLabelMarginLeft);
        float domainLabelMarginRight = getAppResouces().getDimensionPixelSize(R.dimen.domainLabelMarginRight);
        float rangeLabelMargin = getAppResouces().getDimensionPixelSize(R.dimen.rangeLabelMargin);
        float gridPaddingMargin = getAppResouces().getDimensionPixelSize(R.dimen.gridPaddingMargin);
        float gridPaddingMarginRight = getAppResouces().getDimensionPixelSize(R.dimen.gridPaddingMarginRight);

        xyPlot.getDomainLabelWidget().setMargins(domainLabelMarginLeft, 0, domainLabelMarginRight, 0);
        xyPlot.getRangeLabelWidget().setMargins(rangeLabelMargin, 0, rangeLabelMargin, 0);
        xyPlot.getGraphWidget().setGridPadding(gridPaddingMargin, gridPaddingMargin, gridPaddingMarginRight, gridPaddingMargin);
        xyPlot.getGraphWidget().setDomainLabelWidth(domainLabelWidth);
        xyPlot.getGraphWidget().setRangeLabelWidth(rangeLabelWidth);
        xyPlot.getGraphWidget().setDomainLabelHorizontalOffset(0);
        xyPlot.getGraphWidget().setRangeLabelVerticalOffset(rangeLabelVerticalOffset);

        xyPlot.getDomainLabelWidget().pack();
        xyPlot.getRangeLabelWidget().pack();
        final String secStr = getContext().getString(R.string.seconds);
        final DecimalFormat domainFmt = new DecimalFormat("#.#");
        xyPlot.setDomainValueFormat(new NumberFormat() {
            @Override
            public StringBuffer format(double d, StringBuffer sb, FieldPosition fp) {
                return sb.append(domainFmt.format(d / 50f)).append(secStr);
            }
            @Override
            public StringBuffer format(long l, StringBuffer stringBuffer, FieldPosition fieldPosition) {
                return null;
            }
            @Override
            public Number parse(String s, ParsePosition parsePosition) {
                return null;
            }
        });
        xyPlot.setRangeValueFormat(new DecimalFormat("#.#G"));
        xyPlot.setDomainBoundaries(0, 100, BoundaryMode.FIXED);
        xyPlot.setDomainStepValue(2);
        xyPlot.setRangeStepValue(11);
        xyPlot.setRangeBoundaries(-0.5f, 0.5f, BoundaryMode.FIXED);
        DashPathEffect dashFx = new DashPathEffect(new float[] {PixelUtils.dpToPix(3), PixelUtils.dpToPix(3)}, 0);
        xyPlot.getGraphWidget().getDomainGridLinePaint().setPathEffect(dashFx);
        xyPlot.getGraphWidget().getRangeGridLinePaint().setPathEffect(dashFx);
        setGraphData(null);
    }

    protected void setGraphData(List<Double> data) {
        if (!isAdded()) {
            return;
        }
        xyPlot.clear();
        int blueGraph = getAppResouces().getColor(R.color.blue_about);
        SplineLineAndPointFormatter formatter = new SplineLineAndPointFormatter(blueGraph, Color.TRANSPARENT, null);
        formatter.getLinePaint().setStrokeJoin(Paint.Join.ROUND);
        formatter.getLinePaint().setStrokeWidth(4);
        formatter.getLinePaint().setAntiAlias(true);
        if (data != null) {
            series.setModel(data, SimpleXYSeries.ArrayFormat.Y_VALS_ONLY);
        } else {
            series.setModel(new ArrayList<Number>(), SimpleXYSeries.ArrayFormat.Y_VALS_ONLY);
        }
        xyPlot.addSeries(series, formatter);
        xyPlot.calculateMinMaxVals();
        xyPlot.redraw();
    }

    @Override
    public void onDeviceFixed(boolean isDeviceFixed) {
        this.isDeviceFixed = isDeviceFixed;
        if (isAdded()) {
            int deviceState = getDeviceState();
            boolean isRecordEnabled = isRecordEnabled();
            setDeviceIndicatorAngleBtn(deviceIndicatorAngleBtn, isRecordEnabled, isDeviceFixed, deviceState);
            setDataRecordingStatus(isDeviceFixed, isRecordEnabled);
        }
    }

    public void init() {
        float speed = getGpsDetector().getSpeed();
        setGpsData(speed);
        if (bumpValueText != null) {
            bumpValueText.setText(DASH_TEXT);
        }
        refreshPercentValues(0, 0, 0, 0);
        boolean isAutoSync = isAutoMode();
        boolean isRecordEnabled = isRecordEnabled();
        setDeviceIndicatorBtn(isRecordEnabled, isAutoSync);
        initTimers(isAutoSync, isRecordEnabled);
        setManualModeScreen(isRecordEnabled, isAutoSync);
        setDataRecordingStatus(isDeviceFixed, isRecordEnabled);
        initGraph(true);
    }

    protected void initTimers(boolean isAutoSync, boolean isRecordEnabled) {
        if (isAutoSync || isRecordEnabled) {
            runTimers(true);
        }
    }

    protected void runTimers(final boolean run) {
        if (timer != null) {
            if (run) {
                timer.start();
            } else {
                timer.stop();
            }
        }
        if (refreshGraphTimer != null) {
            if (run) {
                refreshGraphTimer.start();
            } else {
                refreshGraphTimer.stop();
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        runTimers(false);
        hideInfoDialog();
    }

    private void refreshAccelerationGraphUi(final List<Double> avList) {
        ActivityUtil.runOnMainThread(new Runnable() {
            @Override
            public void run() {
                boolean isRecordEnabled = isRecordEnabled();
                refreshAccelerationGraph(isRecordEnabled, avList);
            }
        });
    }

    private void refreshAccelerationGraph(boolean isRecordEnabled, List<Double> avList) {
        if (isRecordEnabled) {
            setGraphData(avList);
        } else {
            setGraphData(null);
        }
    }

    private void refreshSensorsAction(final boolean isRecordEnabled) {
        final int deviceState = getDeviceState();
        //refreshVerticalAcceleration(isRecordEnabled);
        setDeviceIndicatorAngleBtn(deviceIndicatorAngleBtn, isRecordEnabled, isDeviceFixed, deviceState);
    }

    protected void setDeviceIndicatorBtn(final boolean isRecordEnabled, final boolean isAutoSync) {
        setControlVisibility(startMeasurementsBtn, !isAutoSync);
        if (!isAutoSync) {
            setManualModeText(isRecordEnabled);
            startMeasurementsBtn.setOnClickListener(this);
        }
    }

    private void setManualModeText(boolean isRecordEnabled) {
        if (isRecordEnabled) {
            startMeasurementsBtn.setText(R.string.summary_manual_stop);
        } else {
            startMeasurementsBtn.setText(R.string.summary_manual_start);
        }
    }

    protected void setManualModeScreen(final boolean enabled) {
        setManualModeScreen(enabled, false);
    }

    protected void setDataRecordingStatus(boolean isDeviceFixed, boolean isRecordEnabled) {
        if (!isAdded()) {
            return;
        }
        final boolean validSpeed = getGpsDetector().isValidSpeed();
        final boolean gpsRightState = isValidGps();
        if (recordStatusValue == null) {
            return;
        }
        if (!isDeviceFixed || !gpsRightState || !validSpeed || !isRecordEnabled) {
            recordStatusValue.setTextColor(getAppResouces().getColor(android.R.color.holo_red_dark));
        } else {
            recordStatusValue.setTextColor(getAppResouces().getColor(android.R.color.holo_green_dark));
        }
        if(!isRecordEnabled) {
            recordStatusValue.setText(R.string.summary_data_collection_status_off_text);
        } else if (!isDeviceFixed) {
            recordStatusValue.setText(R.string.summary_device_not_fixed_text);
        } else if (!gpsRightState) {
            recordStatusValue.setText(R.string.summary_no_gps_text);
        } else if(!validSpeed) {
            double minSpeed = PreferencesUtil.getInstance(getContext()).getSettingsValue(PreferencesUtil.SETTINGS.MIN_SPEED);
            recordStatusValue.setText(getAppResouces().getString(R.string.summary_low_speed_text, minSpeed));
        } else if(isRecordEnabled) {
            recordStatusValue.setText(R.string.summary_data_collection_status_on_text);
        } else {
            recordStatusValue.setText("");
        }
    }

    protected void setManualModeScreen(final boolean enabled, boolean isAutoMode) {
        if (!isAdded()) {
            return;
        }
        if (enabled) {
            int blackColor = getAppResouces().getColor(android.R.color.black);
            badText.setTextColor(blackColor);
            normalText.setTextColor(blackColor);
            goodText.setTextColor(blackColor);
            perfectText.setTextColor(blackColor);
            distanceValueText.setTextColor(blackColor);
            speedValueText.setTextColor(blackColor);
            itemsCntValueText.setTextColor(blackColor);
            intervalsCntValueText.setTextColor(blackColor);
            bumpValueText.setTextColor(getAppResouces().getColor(R.color.bumps_color));
            UIUtils.getTintedDrawable(bumpValueText.getBackground(), getAppResouces().getColor(R.color.bumps_color));
            badQualityText.setTextColor(getAppResouces().getColor(R.color.chart_bad_roads_color));
            normalQualityText.setTextColor(getAppResouces().getColor(R.color.chart_normal_roads_color));
            goodQualityText.setTextColor(getAppResouces().getColor(R.color.chart_good_roads_color));
            perfectQualityText.setTextColor(getAppResouces().getColor(R.color.chart_perfect_roads_color));
        } else {
            deviceIndicatorAngleBtn.setVisibility(View.GONE);
            gpsIndicatorBtn.setVisibility(View.GONE);
            setRoughnessIndicator(roadIntervalValue, RoadQuality.NONE);
            int greyColor = getAppResouces().getColor(android.R.color.secondary_text_light_nodisable);
            badText.setTextColor(greyColor);
            normalText.setTextColor(greyColor);
            goodText.setTextColor(greyColor);
            perfectText.setTextColor(greyColor);
            distanceValueText.setTextColor(greyColor);
            speedValueText.setTextColor(greyColor);
            badQualityText.setTextColor(greyColor);
            normalQualityText.setTextColor(greyColor);
            goodQualityText.setTextColor(greyColor);
            perfectQualityText.setTextColor(greyColor);
            itemsCntValueText.setTextColor(greyColor);
            intervalsCntValueText.setTextColor(greyColor);
            bumpValueText.setTextColor(greyColor);
            UIUtils.getTintedDrawable(bumpValueText.getBackground(), greyColor);
            if (!isAutoMode) {
                bumpValueText.setText(DASH_TEXT);
                distanceValueText.setText(DASH_TEXT);
                speedValueText.setText(DASH_TEXT);
                badQualityText.setText(DASH_TEXT);
                normalQualityText.setText(DASH_TEXT);
                goodQualityText.setText(DASH_TEXT);
                perfectQualityText.setText(DASH_TEXT);
                itemsCntValueText.setText(DASH_TEXT);
                intervalsCntValueText.setText(DASH_TEXT);
                String intervalStr = RoadQuality.NONE.toString();
                roadIntervalValue.setText(intervalStr);
            }
            setGraphData(null);
        }
    }

    @Override
    public void onClick(final View v) {
        switch (v.getId()) {
            case R.id.wrong_angle_icon:
                showInfoDialog(Constants.DIALOG_TYPE_DEVICE_WARNING);
                break;
            case R.id.wrong_gps_icon:
                showInfoDialog(Constants.DIALOG_TYPE_GPS_WARNING);
                break;
            case R.id.startMeasurementsBtn:
                startMeasurementsAction();
                break;
            default:
                break;
        }
    }

    private void startMeasurementsAction() {
        if (getIntervalsRecordHelper() != null) {
            getIntervalsRecordHelper().turn(new IntervalsRecordHelper.OnRecordEventListener() {
                @Override
                public void onRecordStarted(boolean started) {
                    reinitUI();
                    if (!started) {
                        checkCurrentMeasurement();
                    }
                }
            });
        }
    }

    private void reinitUI() {
        boolean isRecordEnabled = isRecordEnabled();
        runTimers(isRecordEnabled);
        setDataRecordingStatus(isDeviceFixed, isRecordEnabled);
        boolean recordStarted = RAApplication.getInstance().isRecordStarted();
        setManualModeScreen(recordStarted);
        setManualModeText(recordStarted);
    }

    private void checkCurrentMeasurement() {
        MeasurementsDataHelper.getInstance().checkCurrentMeasurement(
        new MeasurementsDataHelper.MeasurementsDataLoaderListener<Boolean>() {
            @Override
            public void onDataLoaded(Boolean emptyMeasurement) {
                boolean recordStarted = RAApplication.getInstance().isRecordStarted();
                if (!recordStarted && !emptyMeasurement) {
                    new SaveMeasurementToDialog(getContext(), currentTitle, new SettingsDialog.SettingsDialogListener() {
                        @Override
                        public void onRequestClose(int typeId) {
                            onSaveMeasurementToDialogClosed(typeId);
                        }
                    }).show();
                }
            }
        });
    }

    private void onSaveMeasurementToDialogClosed(int typeId) {
        SaveMeasurementSelectionTypes type = SaveMeasurementSelectionTypes.values()[typeId];
        switch(type) {
            case CURRENT_PROJECT:
                break;
            case SELECT:
                openSelectProjectDlg();
                break;
            case SELECT_LATER:
                moveToDefaultProject();
                break;
        }
    }

    private void moveToDefaultProject() {
        new AsyncTask <Object, Object, Object>() {
            @Override
            protected Object doInBackground(Object... params) {
                long measurementId = RAApplication.getInstance().getCurrentMeasurementId();
                //MeasurementsDataHelper.getInstance().getMeasurement(measurementId);
                FolderModel defProj = MeasurementsDataHelper.getInstance().getDefaultProject();
                RoadModel defRoad = MeasurementsDataHelper.getInstance().getDefaultRoad();
                long projectId = -1;
                long roadId = -1;
                if (defProj == null) {
                    projectId = MeasurementsDataHelper.getInstance().createDefaultProject(true, false, false, true);
                } else {
                    projectId = defProj.getId();
                }
                if(defRoad == null) {
                    roadId = MeasurementsDataHelper.getInstance().createDefaultRoad(projectId, true, false, true);
                } else {
                    roadId = defRoad.getId();
                }
                MeasurementsDataHelper.getInstance().moveMeasurementToSync(measurementId, projectId, roadId);
                return null;
            }
            @Override
            protected void onPostExecute(Object o) {
                Toast.makeText(getContext(),
                getString(R.string.measurement_saved_text), Toast.LENGTH_LONG).show();
            }
        }.execute();
    }

    private void openSelectProjectDlg() {
        new SelectProjectDialog(getMainActivity(), new SelectProjectDialog.ProjectSelectDialogListener() {
            @Override
            public void onProjectSelected(long folderId, long roadId) {
                moveMeasurementTo(folderId, roadId);
            }
        }).show();
    }

    private void moveMeasurementTo(long folderId, long roadId) {
        long curMeasurementId = RAApplication.getInstance().getCurrentMeasurementId();
//        RAApplication.getInstance().setCurrentFolderId(folderId);
//        RAApplication.getInstance().setCurrentRoadId(roadId);
//        PreferencesUtil.getInstance().setCurrentFolderId(folderId);
//        PreferencesUtil.getInstance().setCurrentRoadId(roadId);
        MeasurementsDataHelper.getInstance().moveMeasurementTo(curMeasurementId, folderId, roadId,
                new MeasurementsDataHelper.MeasurementsDataLoaderListener() {
                    @Override
                    public void onDataLoaded(Object data) {
                        Toast.makeText(getContext(),
                        getString(R.string.measurement_saved_text), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void refreshGpsStateIndicator(final boolean isRecordEnabled, boolean gpsRightState) {
        setGpsIndicatorBtn(gpsIndicatorBtn, isRecordEnabled, gpsRightState);
    }

    protected boolean isValidGps() {
        final boolean gpsRightState = getGpsDetector() != null && getGpsDetector().isValidState();
        return gpsRightState;
    }

    protected void refreshPercentValues(final float badPercent, final float normalPercent,
                                        final float goodPercent, final float perfectPercent) {
        badQualityText.setText(String.format(Constants.PERCENTS_FLOAT_FORMAT, badPercent));
        normalQualityText.setText(String.format(Constants.PERCENTS_FLOAT_FORMAT, normalPercent));
        goodQualityText.setText(String.format(Constants.PERCENTS_FLOAT_FORMAT, goodPercent));
        perfectQualityText.setText(String.format(Constants.PERCENTS_FLOAT_FORMAT, perfectPercent));
    }

    protected Cursor getDataItemsCursor() {
        //long session = PreferencesUtil.getInstance(RAApplication.getInstance()).getDataRecordSession();
        long measurementId = RAApplication.getInstance().getCurrentMeasurementId();
        Cursor c = processedDao.getProcessedDataByMeasurementIdCursor(measurementId);
        return c;
    }

    protected ProcessedDataModel getDataFromCursor(Cursor c) {
        return processedDao.cursorToRecord(c);
    }

    protected void refreshData(final boolean refreshGraph) {
        if (!isAdded() || !isRecordEnabled()) {
            return;
        }
        Cursor c = getDataItemsCursor();
        int badIssues = 0;
        int normalIssues = 0;
        int goodIssues = 0;
        int perfectIssues = 0;
        int count = 0;
        int bumpsCount = 0;

        ProcessedDataModel processedData = null;
        boolean hasData = true;

        if (c.getCount() > 0) {
            c.moveToFirst();
            while (hasData) {
                processedData = getDataFromCursor(c);
                RoadQuality quality = processedData.getCategory();
                bumpsCount += processedData.getBumps();
                switch (quality) {
                    case POOR:
                        badIssues++;
                        break;
                    case FAIR:
                        normalIssues++;
                        break;
                    case GOOD:
                        goodIssues++;
                        break;
                    case EXCELLENT:
                        perfectIssues++;
                        break;
                    default:
                        break;
                }
                count++;
                hasData = c.moveToNext();
            }
        }
        c.close();
        final float badPercent = Math.round((float) badIssues / ((float) count * 0.01f));
        final float normalPercent = Math.round((float) normalIssues / ((float) count * 0.01f));
        final float goodPercent = Math.round((float) goodIssues / ((float) count * 0.01f));
        final float perfectPercent = Math.round((float) perfectIssues / ((float) count * 0.01f));
        final int bumps = bumpsCount;
        final RoadQuality quality = processedData != null ? processedData.getCategory() : RoadQuality.NONE;
        ActivityUtil.runOnMainThread(new Runnable() {
            @Override
            public void run() {
                if (isAdded() && isRecordEnabled()) {
                    bumpValueText.setText(String.valueOf(bumps));
                    String intervalStr = quality.toString();
                    roadIntervalValue.setText(intervalStr);
                    refreshPercentValues(badPercent, normalPercent, goodPercent, perfectPercent);
                    if (refreshGraph) {
                        setRoughnessIndicator(roadIntervalValue, quality);
                        setManualModeScreen(true);
                    }
                }
            }
        });
    }

    @Override
    public int getLayoutFragmentResources() {
        return R.layout.fragment_summary_current;
    }

    private void setGpsData(final float speed) {
        if (speedValueText != null) {
            speedValueText.setText(String.format(getString(R.string.speed_format), speed));
        }
    }

    protected void setRoughnessIndicator(View image, RoadQuality quality) {
        int stateColor = getAppResouces().getColor(android.R.color.secondary_text_light_nodisable);
        if (quality != null) {
            stateColor = quality.getRoadConditionColor(RAApplication.getInstance());
        }
        UIUtils.getTintedDrawable(image.getBackground(), stateColor);
    }

    @Override
    public void onTimer() {
        final boolean recordEnabled = isRecordEnabled();
        final boolean autoMode = isAutoMode();
        refreshData(recordEnabled);
        ActivityUtil.runOnMainThread(new Runnable() {
            @Override
            public void run() {
                if (isAdded()) {
                    refreshSensorsData(recordEnabled);
                    setManualModeScreen(recordEnabled, autoMode);
                    setDataRecordingStatus(isDeviceFixed, recordEnabled);
                }
            }
        });
    }

    protected void refreshSensorsData(final boolean isRecordEnabled) {
        if (!isAdded()) {
            return;
        }
        refreshSensorsAction(isRecordEnabled);
        final boolean gpsRightState = isValidGps();
        refreshGpsInfo(isRecordEnabled, gpsRightState);
        refreshDistance();
    }

    protected void refreshDistance() {
        double distance = getIntervalsRecordHelper().getCurrentDistance();
        distanceValueText.setText(String.format(getString(R.string.distance_format), distance));
    }

    private void refreshGpsInfo(final boolean isRecordEnabled, final boolean gpsRightState) {
        refreshGpsStateIndicator(isRecordEnabled, gpsRightState);
        refreshGpsSpeed();
    }

    private void refreshGpsSpeed() {
        float speed = getGpsDetector().getSpeed();
        setGpsData(speed);
    }

    @Override
    public int getMenuFragmentResources() {
        return R.menu.abc_menu_create_tag;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_bar_create_tag: {
                createTag();
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    private void createTag() {
        getMainActivity().openNewTagFragment(true);
    }
}
