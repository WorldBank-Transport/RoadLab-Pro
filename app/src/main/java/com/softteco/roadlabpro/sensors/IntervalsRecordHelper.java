package com.softteco.roadlabpro.sensors;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.softteco.roadlabpro.RAApplication;
import com.softteco.roadlabpro.algorithm.RoadConditionDetection;
import com.softteco.roadlabpro.sensors.data.AccelerometerDataEntry;
import com.softteco.roadlabpro.sensors.data.SensorData;
import com.softteco.roadlabpro.sqlite.MeasurementsDataHelper;
import com.softteco.roadlabpro.sqlite.dao.MeasurementDAO;
import com.softteco.roadlabpro.sqlite.model.MeasurementModel;
import com.softteco.roadlabpro.sqlite.model.ProcessedDataModel;
import com.softteco.roadlabpro.sqlite.model.RecordDetailsModel;
import com.softteco.roadlabpro.util.PreferencesUtil;
import com.softteco.roadlabpro.util.ScheduleTimer;

import java.util.Arrays;
import java.util.List;

/**
 * Created by ppp on 06.05.2015.
 */
public class IntervalsRecordHelper extends DataRecordHelper implements GPSDetector.GpsIntervalsCheckListener, IntervalsCalculator.OnIntervalCalculatedListener, ScheduleTimer.TimerTaskListener {
    public static final String TAG = IntervalsRecordHelper.class.getName();
    private static final int CHECK_SPEED_INTERVAL = 3000;
    private static final int MAX_ARRAY_SIZE = 100;
    private IntervalsCalculator intervalsCalculator;
    private IntervalsCalculator.OnIntervalCalculatedListener listener;
    private boolean autoSync;
    private volatile boolean recordEnabled = false;
    private volatile int currentIdx = 0;
    private Double[] recentLinearAccValues;
    private ScheduleTimer checkSpeedTimer;
    private MeasurementDAO measurementDAO;
    private GeoTagTracker tagTracker;

    public interface OnRecordEventListener {
        void onRecordStarted(boolean started);
    }

    @Override
    public void init(final Activity activity) {
        super.init(activity);
        if (tagTracker != null) {
            tagTracker.destroy();
        }
        tagTracker = new GeoTagTracker();
        measurementDAO = new MeasurementDAO(RAApplication.getInstance());
        checkSpeedTimer = new ScheduleTimer(CHECK_SPEED_INTERVAL);
        checkSpeedTimer.setOnTimerTaskListener(this);
        checkSpeedTimer.start();
        reinit(activity);
    }

    public void reinit(final Context context) {
        reinit(context, true);
    }

    public void reinit(final Context context, boolean clearDistance) {
        if (intervalsCalculator == null) {
            intervalsCalculator = new IntervalsCalculator();
        }
        if (intervalsCalculator != null) {
            intervalsCalculator.setOnIntervalCalculatedListener(this);
            intervalsCalculator.init();
        }
        if (clearDistance) {
            clearCurrentDistance();
        }
        checkSettings(context);
        stop();
    }

    public void setOnIntervalCalculatedListener(final IntervalsCalculator.OnIntervalCalculatedListener listener) {
        this.listener = listener;
    }

    @Override
    public void onIntervalCalculated(double distance, ProcessedDataModel data) {
        Log.d(TAG, "onIntervalCalculated: distance= " + distance + ", data stddev: " + data.getStdDeviation() + ", data bumps: " + data.getBumps());
        if (data != null) {
            incCurrentDistance(distance);
            incOverallDistance(distance);
            if (listener != null) {
                listener.onIntervalCalculated(distance, data);
            }
        }
    }

    private void checkSettings(final Context context) {
        autoSync = PreferencesUtil.getInstance(context).autoRoughnessDetectionEnabled();
    }

    public List<Double> getRecentLinearAccValues() {
        if (recentLinearAccValues != null) {
            return Arrays.asList(recentLinearAccValues);
        }
        return null;
    }

    private void putLinearAccValue(RecordDetailsModel record) {
        if (record == null) {
            return;
        }
        float av = RoadConditionDetection.getAv(record) / RoadConditionDetection.G;
        if (recentLinearAccValues == null) {
            recentLinearAccValues = new Double[MAX_ARRAY_SIZE];
        }
        if (currentIdx == MAX_ARRAY_SIZE) {
            for (int i = 0; i < recentLinearAccValues.length - 1; i++ ) {
                recentLinearAccValues[i] = recentLinearAccValues[i+1];
            }
            recentLinearAccValues[MAX_ARRAY_SIZE - 1] = (double) av;

        } else {
            recentLinearAccValues[currentIdx] = (double) av;
            currentIdx++;
        }
    }

    protected void processOnSensorChanged(final SensorData data) {
        processSensorsData(data);
    }

    @Override
    protected void writeData(final boolean isTmp) {
        writeRecordToBuf(isTmp);
    }

    @Override
    protected void writeRecordToBuf(final boolean isTmp) {
        RecordDetailsModel record = populateRecord();
        if (record == null) {
//            Log.i(TAG, "writeRecordToBuf: record = null");
            return;
        }
        putLinearAccValue(record);
        if (isTmp) {
            getDataKeeper().putTmpItem(record);
        } else {
            RecordDetailsModel avgRecord = getDataKeeper().getAvgValue();
            if (avgRecord != null) {
                intervalsCalculator.putToCache(avgRecord);
            } else {
                intervalsCalculator.putToCache(record);
            }
        }
    }

    public void process(double distance) {
        if (intervalsCalculator != null) {
            intervalsCalculator.checkInterval(distance);
        }
    }

    private void clearCache() {
        if (intervalsCalculator != null) {
            intervalsCalculator.clearCache();
        }
    }

    public float getCurrentAv() {
        if (getSensorGravity() != null
         && getSensorGravity().getLastAccelerometerDataEntry() != null) {
            AccelerometerDataEntry data = getSensorGravity().getLastAccelerometerDataEntry();
            return RoadConditionDetection.getAv(data);
        }
        return 0;
    }

    @Override
    public void gpsListenerOnIntervalChanged(double distance, boolean correctData) {
        if (correctData && isRecordEnabled()) {
            process(distance);
        }
        if (!correctData) {
            clearCache();
        }
    }

    public boolean isRecordEnabled() {
        return recordEnabled;
    }

    public void turn() {
        turn(null);
    }

    public void turn(OnRecordEventListener listener) {
        if (RAApplication.getInstance().isRecordStarted()) {
            stop();
            RAApplication.getInstance().setRecordStarted(false);
            if (listener != null) {
                listener.onRecordStarted(false);
            }
        } else {
            RAApplication.getInstance().setRecordStarted(true);
            prepareStart(listener);
        }
    }

    public void prepareStart(final OnRecordEventListener listener) {
        new AsyncTask<Object, Object, Boolean>() {
            @Override
            protected Boolean doInBackground(Object... params) {
                boolean recordStarted = RAApplication.getInstance().isRecordStarted();
                if (recordStarted) {
                    createMeasurementItem();
                }
                return recordStarted;
            }
            @Override
            protected void onPostExecute(Boolean recordStarted) {
                if (recordStarted) {
                    reinit(RAApplication.getInstance(), true);
                    start(true);
                }
                if (listener != null) {
                    listener.onRecordStarted(recordStarted);
                }
            }
        }.execute();
    }

    private boolean checkSpeed() {
        GPSDetector detector = getGpsDetector();
        if (detector != null) {
            return detector.isValidSpeed();
        }
        return false;
    }

    private void enableSensors(final boolean enable) {
        GravityAccelerometerDetector detector = getSensorGravity();
        if (detector != null) {
            if (enable) {
                detector.start();
                recordEnabled = true;
            } else {
                detector.stop();
                recordEnabled = false;
            }
        }
    }

    private void checkSensors() {
        if (!autoSync) {
            return;
        }
        enableSensors(checkSpeed());
    }

    @Override
    public void onTimer() {
        checkSensors();
    }

    public synchronized void start() {
        start(false);
    }

    public synchronized void start(final boolean manual) {
        checkSettings(RAApplication.getInstance());
        if (autoSync || manual) {
            recordEnabled = true;
            if (!autoSync || manual) {
                enableSensors(true);
            } else if(autoSync) {
                checkSensors();
            }
        }
        if (tagTracker == null) {
            tagTracker = new GeoTagTracker();
        }
        tagTracker.start();
    }

    private void createMeasurementItem() {
        if (measurementDAO == null) {
            measurementDAO = new MeasurementDAO(RAApplication.getInstance());
        }
        checkCurrentProjectSelection();
        MeasurementModel measurement = new MeasurementModel(RAApplication.getInstance().getCurrentRoadId());
        long measurementId = measurementDAO.putMeasurement(measurement);
        RAApplication.getInstance().setCurrentMeasurementId(measurementId);
        Log.i(TAG, "created MeasurementItem");
    }

    private void checkCurrentProjectSelection() {
        long projects = MeasurementsDataHelper.getInstance().getFoldersCount();
        long folderId = RAApplication.getInstance().getCurrentFolderId();
        if (projects < 1) {
            folderId = MeasurementsDataHelper.getInstance().createDefaultProject(true);
        }
        long roads = MeasurementsDataHelper.getInstance().getRoadsCount(folderId);
        if (roads < 1) {
            MeasurementsDataHelper.getInstance().createDefaultRoad(folderId, true);
        }
    }

    public synchronized void stop() {
        recordEnabled = false;
        enableSensors(false);
        clear();
        if (tagTracker != null) {
            tagTracker.stop();
        }
    }

    public void clear() {
        currentIdx = 0;
        if (recentLinearAccValues != null) {
            for (int i = 0; i < recentLinearAccValues.length; i++) {
                recentLinearAccValues[i] = null;
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        listener = null;
        if (intervalsCalculator != null) {
            intervalsCalculator.destroy();
        }
        if (checkSpeedTimer != null) {
            checkSpeedTimer.stop();
        }
        if (tagTracker != null) {
            tagTracker.destroy();
        }
    }
}
