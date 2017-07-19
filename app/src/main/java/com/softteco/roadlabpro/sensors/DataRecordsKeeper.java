package com.softteco.roadlabpro.sensors;

import android.content.Context;
import android.util.Log;

import com.softteco.roadlabpro.RAApplication;
import com.softteco.roadlabpro.sqlite.dao.RecordDAO;
import com.softteco.roadlabpro.sqlite.dao.RecordDetailsDAO;
import com.softteco.roadlabpro.sqlite.model.RecordDetailsModel;
import com.softteco.roadlabpro.sqlite.model.RecordModel;
import com.softteco.roadlabpro.util.ExecutorServiceManager;
import com.softteco.roadlabpro.util.ValidateUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ppp on 09.04.2015.
 */
public class DataRecordsKeeper {

    private static final String TAG = DataRecordsKeeper.class.getSimpleName();
    private static final int RECORD_DETAILS_BUF_MAX_LENGTH = 10000;
    private static final long REFRESH_TIME = 5000;

    private ExecutorServiceManager serviceManager;

    private RecordModel currentRecord;
    private List<RecordDetailsModel> recordDetailsBuf;
    private List<RecordDetailsModel> tmpRecordDetailsBuf;

    private RecordDAO recordDao;
    private RecordDetailsDAO recordDetailsDao;

    private volatile boolean syncProcessing = false;

    private long lastRefreshTime = 0;

    public DataRecordsKeeper(Context context) {
        recordDao = new RecordDAO(context);
        recordDetailsDao = new RecordDetailsDAO(context);
        serviceManager = new ExecutorServiceManager();
    }

    public List<RecordDetailsModel> getRecordDetailsById(int recordId) {
        return recordDetailsDao.getRecordDetailsOfRecord(recordId);
    }

    public RecordModel getCurrentRecord() {
        return currentRecord;
    }

    public void putCurrentRecord(RecordModel currentRecord) {
        this.currentRecord = currentRecord;
        long recordId = new RecordDAO(RAApplication.getInstance()).putRecord(currentRecord);
        if (recordId >= 0) {
            currentRecord.setId(recordId);
        }
    }

    public void putTmpItem(RecordDetailsModel record) {
        if (syncProcessing) {
            return;
        }
        if (tmpRecordDetailsBuf == null) {
            tmpRecordDetailsBuf = new ArrayList<RecordDetailsModel>();
        }
        if (tmpRecordDetailsBuf.size() == RECORD_DETAILS_BUF_MAX_LENGTH) {
            return;
        }
        tmpRecordDetailsBuf.add(record);
    }
    
    public RecordDetailsModel getAvgValue() {

        RecordDetailsModel avgItem = null;

        float distance = 0;
        float averageSpeed = 0;
        double rotationAmgle = 0;

        float accelerometerX = 0;
        float accelerometerY = 0;
        float accelerometerZ = 0;

        float accelerometerLinearX = 0;
        float accelerometerLinearY = 0;
        float accelerometerLinearZ = 0;

        float accelerometerGravityX = 0;
        float accelerometerGravityY = 0;
        float accelerometerGravityZ = 0;
        
        int count = 0;
        
        if (tmpRecordDetailsBuf != null && tmpRecordDetailsBuf.size() > 0) {
            avgItem = new RecordDetailsModel();
            for (RecordDetailsModel r : tmpRecordDetailsBuf) {
                avgItem.setTime(r.getTime());
                avgItem.setRecordId(r.getRecordId());
                avgItem.setTimeStamp(r.getTimeStamp());
                avgItem.setLongitude(r.getLongitude());
                avgItem.setLatitude(r.getLatitude());
                avgItem.setAltitude(r.getAltitude());
                avgItem.setCurLongitude(r.getCurLongitude());
                avgItem.setCurLatitude(r.getCurLatitude());
                avgItem.setCurAltitude(r.getCurAltitude());
                if (ValidateUtil.isValidNumber(r.getDistance())) {
                    distance += r.getDistance();
                }
                if (ValidateUtil.isValidNumber(r.getAverageSpeed())) {
                    averageSpeed += r.getAverageSpeed();
                }
                if (ValidateUtil.isValidNumber(r.getRotationAngle())) {
                    rotationAmgle = r.getRotationAngle();
                }
                if (ValidateUtil.isValidNumber(r.getAccelerometerX())) {
                    accelerometerX = r.getAccelerometerX();
                }
                if (ValidateUtil.isValidNumber(r.getAccelerometerY())) {
                    accelerometerY = r.getAccelerometerY();
                }
                if (ValidateUtil.isValidNumber(r.getAccelerometerZ())) {
                    accelerometerZ = r.getAccelerometerZ();
                }
                if (ValidateUtil.isValidNumber(r.getAccelerometerLinearX())) {
                    accelerometerLinearX = r.getAccelerometerLinearX();
                }
                if (ValidateUtil.isValidNumber(r.getAccelerometerLinearY())) {
                    accelerometerLinearY = r.getAccelerometerLinearY();
                }
                if (ValidateUtil.isValidNumber(r.getAccelerometerLinearZ())) {
                    accelerometerLinearZ = r.getAccelerometerLinearZ();
                }
                if (ValidateUtil.isValidNumber(r.getAccelerometerGravityX())) {
                    accelerometerGravityX = r.getAccelerometerGravityX();
                }
                if (ValidateUtil.isValidNumber(r.getAccelerometerGravityY())) {
                    accelerometerGravityY = r.getAccelerometerGravityY();
                }
                if (ValidateUtil.isValidNumber(r.getAccelerometerGravityZ())) {
                    accelerometerGravityZ = r.getAccelerometerGravityZ();
                }
                count++;
            }
            if (count > 0) {
                if (distance > 0) {
                    avgItem.setDistance(distance / (float) count);
                }
                if (averageSpeed > 0) {
                    avgItem.setAverageSpeed(averageSpeed / (float) count);
                }
                if (rotationAmgle != 0) {
                    avgItem.setRotationAngle(rotationAmgle /*/ (float) count*/);
                }
                if (accelerometerX != 0) {
                    avgItem.setAccelerometerX(accelerometerX/* / (float) count*/);
                }
                if (accelerometerY != 0) {
                    avgItem.setAccelerometerY(accelerometerY /* / (float) count*/);
                }
                if (accelerometerZ != 0) {
                    avgItem.setAccelerometerZ(accelerometerZ /* / (float) count*/);
                }
                if (accelerometerLinearX != 0) {
                    avgItem.setAccelerometerLinearX(accelerometerLinearX /* / (float) count*/);
                }
                if (accelerometerLinearY != 0) {
                    avgItem.setAccelerometerLinearY(accelerometerLinearY /* / (float) count*/);
                }
                if (accelerometerLinearZ != 0) {
                    avgItem.setAccelerometerLinearZ(accelerometerLinearZ /* / (float) count*/);
                }
                if (accelerometerGravityX != 0) {
                    avgItem.setAccelerometerGravityX(accelerometerGravityX /* / (float) count*/);
                }
                if (accelerometerGravityY != 0) {
                    avgItem.setAccelerometerGravityY(accelerometerGravityY /* / (float) count*/);
                }
                if (accelerometerGravityZ != 0) {
                    avgItem.setAccelerometerGravityZ(accelerometerGravityZ /* / (float) count*/);
                }
            }
            tmpRecordDetailsBuf.clear();
        }
        return avgItem;
    }

    public void putItem(RecordDetailsModel record) {
        if (syncProcessing) {
            return;
        }
        if (recordDetailsBuf == null) {
            recordDetailsBuf = new ArrayList<RecordDetailsModel>();
        }
        if (recordDetailsBuf.size() == RECORD_DETAILS_BUF_MAX_LENGTH) {
            return;
        }
        recordDetailsBuf.add(record);
    }

    public void syncToDB() {
        syncToDB(false);
    }

    public void syncToDB(boolean force) {
        long currentTime = System.currentTimeMillis();
        if (force || currentTime - lastRefreshTime >= REFRESH_TIME) {
            lastRefreshTime = currentTime;
            serviceManager.runOperation(new Runnable() {
                @Override
                public void run() {
                    syncDataRecords();
                }
            });
        }
    }

    private void syncDataRecords() {
        if (recordDetailsBuf == null) {
            syncProcessing = false;
            return;
        }
        syncProcessing = true;
        Log.i(TAG, "synchronize data to DB, Buf size: " + recordDetailsBuf.size());
        List<RecordDetailsModel> syncList = new ArrayList<RecordDetailsModel>(recordDetailsBuf);
        clearRecordsBuf();
        syncProcessing = false;
        recordDetailsDao.putRecordDetailsList(syncList);
        syncList.clear();
    }

    public void clearRecordsBuf() {
        syncProcessing = true;
        if (recordDetailsBuf != null) {
            recordDetailsBuf.clear();
        }
        syncProcessing = false;
    }

    public void clean() {
        if (serviceManager != null) {
            serviceManager.shutdown();
        }
    }
}
