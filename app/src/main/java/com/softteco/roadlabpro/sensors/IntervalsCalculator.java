package com.softteco.roadlabpro.sensors;

import android.util.Log;

import com.softteco.roadlabpro.RAApplication;
import com.softteco.roadlabpro.algorithm.RoadConditionDetection;
import com.softteco.roadlabpro.sqlite.dao.BumpDAO;
import com.softteco.roadlabpro.sqlite.dao.ProcessedDataDAO;
import com.softteco.roadlabpro.sqlite.model.BumpModel;
import com.softteco.roadlabpro.sqlite.model.ProcessedDataModel;
import com.softteco.roadlabpro.sqlite.model.RecordDetailsModel;
import com.softteco.roadlabpro.util.ExecutorServiceManager;
import com.softteco.roadlabpro.util.PreferencesUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ppp on 06.05.2015.
 */
public class IntervalsCalculator {

    public static final String TAG = IntervalsCalculator.class.getName();
    private static final int RECORD_DETAILS_BUF_MAX_LENGTH = 50000;

    private ProcessedDataDAO processedDataDAO;
    private BumpDAO bumpDao;

    private RoadConditionDetection roadConditionDetector;
    private ExecutorServiceManager serviceManager;
    private List<RecordDetailsModel> cacheBuf;
    private volatile boolean syncProcessing = false;
    private OnIntervalCalculatedListener intervalCalculatedListener;

    public interface OnIntervalCalculatedListener {
        void onIntervalCalculated(double distance, ProcessedDataModel data);
    }

    public void setOnIntervalCalculatedListener(OnIntervalCalculatedListener listener) {
        this.intervalCalculatedListener = listener;
    }

    public IntervalsCalculator() {
        serviceManager = new ExecutorServiceManager();
        roadConditionDetector = new RoadConditionDetection();
        processedDataDAO = new ProcessedDataDAO(RAApplication.getInstance());
        bumpDao = new BumpDAO(RAApplication.getInstance());
    }

    public void init() {
        if (roadConditionDetector != null) {
            roadConditionDetector.init();
        }
        clearCache();
    }

    public void clearCache() {
        if (cacheBuf != null && !syncProcessing) {
            syncProcessing = true;
            Log.e(TAG, "clearCache() processing");
            cacheBuf.clear();
            syncProcessing = false;
        }
    }

    public void putToCache(RecordDetailsModel record) {
        if (syncProcessing) {
            Log.e(TAG, "putToCache(): syncProcessing = true");
            return;
        }
        if (cacheBuf == null) {
            Log.e(TAG, "putToCache(): cacheBuf = null");
            cacheBuf = new ArrayList<RecordDetailsModel>();
        }
        if (isBufferOverflow()) {
            Log.e(TAG, "Data buffer overflow: no space to record");
            return;
        }
        //TODO
        record.setMeasurementId(RAApplication.getInstance().getCurrentMeasurementId());
        cacheBuf.add(record);
    }

    public void checkInterval(final double distance) {
        final List<RecordDetailsModel> data = getIntervalData();

        //LogUtils.logInFile("IntervalsCalculator", "checkInterval() :: data.size == " + data.size());

        serviceManager.runOperation(new Runnable() {
            @Override
            public void run() {
                ProcessedDataModel processedData = processIntervalData(data, distance);
                if (processedData != null) {
                    processedDataDAO.putProcessedData(processedData);
                    putBumps();
                    if (intervalCalculatedListener != null) {
                        intervalCalculatedListener.onIntervalCalculated(distance, processedData);
                    }
                }
            }
        });
    }

    private List<RecordDetailsModel> getIntervalData() {
        List<RecordDetailsModel> tmpCacheBuf = null;
        //long startTime = System.nanoTime();
        if (cacheBuf != null && cacheBuf.size() > 0) {
            Log.d(TAG, "processIntervalData, cacheBuf SIZE: " + cacheBuf.size());
            syncProcessing = true;
            tmpCacheBuf = new ArrayList<>(cacheBuf);
            Log.e(TAG, "cacheBuf: clearing");
            cacheBuf.clear();
            syncProcessing = false;
        }
        //long endTime = System.nanoTime() - startTime;
        //Log.i(TAG, "getIntervalData, copy arrays time: " + endTime);
        return tmpCacheBuf;
    }

    private ProcessedDataModel processIntervalData(List<RecordDetailsModel> data, double distance) {
        ProcessedDataModel record = null;
        //startTime = System.nanoTime();
        if (data != null) {
            roadConditionDetector.reset();
            record = roadConditionDetector.calculate(data, distance);
            if (record != null) {
                long session = PreferencesUtil.getInstance(RAApplication.getInstance()).getDataRecordSession();
                record.setSession(session);
                long folderId = RAApplication.getInstance().getCurrentFolderId();
                long roadId = RAApplication.getInstance().getCurrentRoadId();
                long measurementId = RAApplication.getInstance().getCurrentMeasurementId();
                PreferencesUtil.SUSPENSION_TYPES suspensionType = PreferencesUtil.getInstance().getSuspensionType();
                record.setFolderId(folderId);
                record.setRoadId(roadId);
                record.setMeasurementId(measurementId);
                if (suspensionType != null) {
                    record.setSuspension(suspensionType.ordinal());
                }
            }
        }
        //endTime = System.nanoTime() - startTime;
        //Log.i(TAG, "processIntervalData, calculation time: " + endTime);
        return record;
    }

    private void putBumps() {
        List<BumpModel> bumps = roadConditionDetector.getDetectedBumps();
        if (bumps != null && bumps.size() > 0) {
            long folderId = RAApplication.getInstance().getCurrentFolderId();
            long roadId = RAApplication.getInstance().getCurrentRoadId();
            long measurementId = RAApplication.getInstance().getCurrentMeasurementId();
            bumpDao.putBumpList(bumps, folderId, roadId, measurementId);
        }
    }

    public boolean isBufferOverflow() {
        return cacheBuf != null &&
        cacheBuf.size() >= RECORD_DETAILS_BUF_MAX_LENGTH;
    }

    public void destroy() {
        if (serviceManager != null) {
            serviceManager.shutdown();
        }
        intervalCalculatedListener = null;
    }
}
