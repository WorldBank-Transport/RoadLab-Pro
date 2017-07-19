package com.softteco.roadlabpro.sensors;

import android.app.Activity;
import android.hardware.Sensor;
import android.util.Log;

import com.softteco.roadlabpro.RAApplication;
import com.softteco.roadlabpro.sensors.data.AccelerometerDataEntry;
import com.softteco.roadlabpro.sensors.data.SensorData;
import com.softteco.roadlabpro.sqlite.model.RecordDetailsModel;
import com.softteco.roadlabpro.sqlite.model.RecordModel;
import com.softteco.roadlabpro.util.Constants;
import com.softteco.roadlabpro.util.ExecutorServiceManager;
import com.softteco.roadlabpro.util.PreferencesUtil;

import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by ppp on 07.04.2015.
 */
public class DataRecordHelper implements GravityAccelerometerDetector.SensorsDetectorListener {
    private static final String TAG = DataRecordHelper.class.getSimpleName();
    private static final long HOLD_TIME = 1000;
    private static final long AVAILABILITY_CHECK_MIN_TIME = 100;
    private static final long AVAILABILITY_CHECK_TIMEOUT = 1000;
    private Activity activity;
    private volatile boolean recordStarted;
    private GravityAccelerometerDetector sensorGravity;
    private Set<DataRecordChangedListener> dataRecordChangedListeners;
    private ExecutorServiceManager executor;

    private volatile double currentDistance = 0;

    private Date recordStartTime;
    private long startTimeInterval;

    private DataRecordsKeeper dataKeeper;
    private RecordModel record;

    private long lastCheckAvailability = 0;
    private volatile boolean linearAccelerationNewData = false;
    private volatile boolean gravityNewData = false;
    private volatile boolean accelerometerNewData = false;

    public interface DataRecordChangedListener {
        void onAccelerometerChangedListener(SensorData data, AccelerometerDataEntry entry);
    }

    public void init(Activity activity) {
        this.activity = activity;
        executor = new ExecutorServiceManager();
        dataKeeper = new DataRecordsKeeper(RAApplication.getInstance());
        detectorInitialize();
        initListeners();
    }

    public void clearCurrentDistance() {
        currentDistance = 0;
    }

    public double getCurrentDistance() {
        return currentDistance;
    }

    public void incCurrentDistance(double distance) {
        this.currentDistance += distance;
    }

    public void incOverallDistance(double distance) {
        float overallDistance = PreferencesUtil.getInstance(RAApplication.getInstance()).getOverallDistance();
        overallDistance += distance;
        PreferencesUtil.getInstance(RAApplication.getInstance()).setOverallDistance(overallDistance);
    }

    public void setDataRecordChangedListener(DataRecordChangedListener listener) {
        if (dataRecordChangedListeners == null) {
            dataRecordChangedListeners = new HashSet();
        }
        dataRecordChangedListeners.add(listener);
    }

    public void clearDataRecordChangedListener() {
        if (dataRecordChangedListeners != null) {
            dataRecordChangedListeners.clear();
        }
    }

    public void removeDataRecordChangedListener(DataRecordChangedListener listener) {
        if (dataRecordChangedListeners != null) {
            dataRecordChangedListeners.remove(listener);
        }
    }

    private void initListeners() {
        sensorGravity.setOnSensorChangedListener(this);
    }

    private void detectorInitialize() {
        sensorGravity = new GravityAccelerometerDetector(RAApplication.getInstance());
    }

    public GPSDetector getGpsDetector() {
        return RAApplication.getInstance().getGpsDetector();
    }

    protected void setStartTime() {
        setRecordStartTime(Calendar.getInstance().getTime());
        setStartTimeInterval(System.currentTimeMillis());
    }

    public GravityAccelerometerDetector getSensorGravity() {
        return sensorGravity;
    }

    public Date getRecordStartTime() {
        return recordStartTime;
    }

    public void setRecordStartTime(Date recordStartTime) {
        this.recordStartTime = recordStartTime;
    }

    public long getStartTimeInterval() {
        return startTimeInterval;
    }

    public void setStartTimeInterval(long startTimeInterval) {
        this.startTimeInterval = startTimeInterval;
    }

    public Activity getActivity() {
        return activity;
    }

    public void onDestroy() {
        stop();
        activity = null;
        recordStarted = false;
        startTimeInterval = 0;
        recordStartTime = null;
        if (executor != null) {
            executor.shutdown();
        }
        if (dataKeeper != null) {
            dataKeeper.clean();
        }
        if (sensorGravity != null) {
            sensorGravity.destroy();
        }
    }

    public boolean isRecordStarted() {
        return recordStarted;
    }

    public void addRecord() {
        dataKeeper.putCurrentRecord(record);
    }

    public synchronized void start() {
        if (!recordStarted) {
            Log.i(TAG, "start data recording");
            recordStarted = true;
            setStartTime();
            addRecord();
            sensorGravity.start();
        }
    }

    public synchronized void stop() {
        if (recordStarted) {
            Log.i(TAG, "stop data recording");
            recordStarted = false;
            dataKeeper.syncToDB(true);
            sensorGravity.stop();
            getGpsDetector().reset();
        }
    }

    public RecordModel getRecord() {
        return record;
    }

    public void setRecord(RecordModel record) {
        this.record = record;
    }

    public DataRecordsKeeper getDataKeeper() {
        return dataKeeper;
    }

    private boolean isNewDataAvailable() {
        long currTime = System.currentTimeMillis();
        long interval = currTime - lastCheckAvailability;
        if (linearAccelerationNewData && gravityNewData && accelerometerNewData
            || interval >= AVAILABILITY_CHECK_TIMEOUT) {
            lastCheckAvailability = currTime;
            linearAccelerationNewData = false;
            gravityNewData = false;
            accelerometerNewData = false;
            return true;
        }
        return false;
    }

    @Override
    public void onSensorChanged(SensorData data) {
        processOnSensorChanged(data);
    }

    protected void processSensorsData(SensorData data) {
        switch (data.type) {
            case Sensor.TYPE_ACCELEROMETER:
                accelerometerNewData = true;
                break;
            case Sensor.TYPE_GRAVITY:
                gravityNewData = true;
                break;
            case Sensor.TYPE_LINEAR_ACCELERATION:
                linearAccelerationNewData = true;
                break;
        }
        boolean isTmp = isNewDataAvailable();
        if (dataRecordChangedListeners != null) {
            for (DataRecordChangedListener l : dataRecordChangedListeners) {
                if (l != null) {
                    l.onAccelerometerChangedListener(data, sensorGravity.getLastAccelerometerDataEntry());
                }
            }
        }
        writeData(isTmp);
    }

    protected void processOnSensorChanged(final SensorData data) {
        if (executor != null) {
            executor.runOperation(new Runnable() {
                @Override
                public void run() {
                    processSensorsData(data);
                }
            });
        }
    }

    protected void writeRecordToBuf(boolean isTmp) {
        RecordDetailsModel record = populateRecord();
        if (record == null) {
            return;
        }
        if (isTmp) {
            dataKeeper.putTmpItem(record);
        } else {
            RecordDetailsModel avgRecord = dataKeeper.getAvgValue();
            if (avgRecord != null) {
                dataKeeper.putItem(avgRecord);
            } else {
                dataKeeper.putItem(record);
            }
        }
    }

    protected RecordDetailsModel populateRecord() {
        GPSDetector gpsDetector = getGpsDetector();
        boolean isValidSpeed = gpsDetector.isValidSpeed();
        long time = System.currentTimeMillis() - startTimeInterval;
        if (time < HOLD_TIME || !gpsDetector.isValidState() || !isValidSpeed) {
//            Log.i(TAG, "populateRecord: time < HOLD_TIME || !gpsDetector.isValidState() || !isValidSpeed");
            return null;
        }
        long recordId = 0;
        if (dataKeeper.getCurrentRecord() != null) {
            recordId = dataKeeper.getCurrentRecord().getId();
        }
        AccelerometerDataEntry entry = sensorGravity.getLastAccelerometerDataEntry();
        RecordDetailsModel record = new RecordDetailsModel();
        record.setId(recordId);
        record.setMeasurementId(RAApplication.getInstance().getCurrentMeasurementId());
        record.setTime(time);
        long startTime = 0;
        if (getRecordStartTime() != null){
            startTime = getRecordStartTime().getTime();
        }
        record.setTimeStamp(startTime);
        int intervalNumber = (int)((float) getCurrentDistance() / Constants.INTERVAL_LENGTH);
        record.setIntervalNumber(intervalNumber);
        record.setDistance(getCurrentDistance());
        record.setLatitude(gpsDetector.getLatitude());
        record.setLongitude(gpsDetector.getLongitude());
        record.setAltitude(gpsDetector.getAltitude());
        record.setGpsAccuracy(gpsDetector.getAccuracyValue());
        record.setAverageSpeed(gpsDetector.getSpeed());
        if (entry != null) {
            record.setAccelerometerX(entry.getAccelerometerX());
            record.setAccelerometerY(entry.getAccelerometerY());
            record.setAccelerometerZ(entry.getAccelerometerZ());
            record.setAccelerometerLinearX(entry.getAccelerometerLinearX());
            record.setAccelerometerLinearY(entry.getAccelerometerLinearY());
            record.setAccelerometerLinearZ(entry.getAccelerometerLinearZ());
            record.setAccelerometerGravityX(entry.getAccelerometerGravityX());
            record.setAccelerometerGravityY(entry.getAccelerometerGravityY());
            record.setAccelerometerGravityZ(entry.getAccelerometerGravityZ());
            record.setRotationAngle(entry.getRotationAngle());
        }
        return record;
    }

    public int getDeviceState() {
        if (sensorGravity != null) {
            return sensorGravity.getDeviceState();
        }
        return -1;
    }

    public boolean isValidRotationAngle() {
        boolean isValidAngle = false;
        if (sensorGravity != null) {
            isValidAngle = sensorGravity.isValidRotationAngle();
        }
        return isValidAngle;
    }

    protected void writeData(boolean isTmp) {
        writeRecordToBuf(isTmp);
        if (!isTmp) {
            dataKeeper.syncToDB();
        }
    }
}
