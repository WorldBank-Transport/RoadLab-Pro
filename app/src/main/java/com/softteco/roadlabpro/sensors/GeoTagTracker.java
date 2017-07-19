package com.softteco.roadlabpro.sensors;

import android.location.Location;
import android.util.Log;

import com.softteco.roadlabpro.RAApplication;
import com.softteco.roadlabpro.sqlite.dao.GeoTagDAO;
import com.softteco.roadlabpro.sqlite.model.GeoTagModel;
import com.softteco.roadlabpro.util.ScheduleTimer;
import com.softteco.roadlabpro.util.ValidateUtil;

import java.util.Calendar;
import java.util.Date;

public class GeoTagTracker implements ScheduleTimer.TimerTaskListener {

    public static final String TAG = GeoTagTracker.class.getName();

    private static final long TRACK_TIME = 5000;
    private static final long MIN_DISTANCE = 10;

    private volatile double curLatitude = -1;
    private volatile double curLongitude = -1;
    private volatile double curAltitude = -1;
    private volatile double curSpeed = -1;
    private volatile double curDistance = 0;
    private volatile boolean started = false;

    private ScheduleTimer trackingTimer;

    private void GeoTagTracker() {
    }

    private synchronized GPSDetector getGPSDetector() {
        return RAApplication.getInstance().getGpsDetector();
    }

    private boolean checkIsDataAvailableForRecord() {
        GPSDetector gpsDetector = getGPSDetector();
        if (gpsDetector == null ||
           !gpsDetector.isValidSpeed() || !gpsDetector.isValidState()) {
            return false;
        }
        Location loc = gpsDetector.getLocation();
        if (loc == null) {
            return false;
        }
        double latitude = loc.getLatitude();
        double longitude = loc.getLongitude();
        double altitude = loc.getAltitude();
        curSpeed = loc.getSpeed();
        double distance = 0;
        if (latitude >= 0 && longitude >= 0) {
            distance = calcDistance(latitude, longitude);
            if (curLatitude != latitude && curLongitude != longitude) {
                curLatitude = latitude;
                curLongitude = longitude;
                curAltitude = altitude;
                //Log.d(TAG, "curLatitude= " + curLatitude + ", curLongitude= "
                //+ curLongitude + ", distance= " + distance);
            }
        }
        if (distance >= MIN_DISTANCE) {
            curDistance = distance;
            Log.d(TAG, "data available for record, distance= " + distance);
            return true;
        }
        return false;
    }

    private double calcDistance(double latitude, double longitude) {
        float distance = 0;
        float[] distanceArr = new float[2];
        if (curLatitude >= 0 && curLongitude >= 0) {
            Location.distanceBetween(
            curLatitude, curLongitude,
            latitude, longitude, distanceArr);
        }
        if (ValidateUtil.isValidNumber(distanceArr[0])) {
            distance = distanceArr[0];
        }
        return distance;
    }

    public void start() {
        if (started) {
            return;
        }
        if (trackingTimer == null) {
            trackingTimer = new ScheduleTimer(TRACK_TIME);
            trackingTimer.setOnTimerTaskListener(this);
        }
        if (trackingTimer != null) {
            trackingTimer.start();
        }
        Log.d(TAG, "started");
        started = true;
    }

    public void stop() {
        if (trackingTimer != null) {
            trackingTimer.stop();
        }
        Log.d(TAG, "stopped");
        started = false;
    }

    public void destroy() {
        Log.d(TAG, "destroyed");
        stop();
        curLatitude = -1;
        curLongitude = -1;
        curAltitude = -1;
        trackingTimer = null;
    }

    @Override
    public void onTimer() {
       if (checkIsDataAvailableForRecord()) {
           Log.d(TAG, "record geo tag: lat: " + curLatitude + ", lon: " + curLongitude);
           writeTagToDb();
       }
    }

    private void writeTagToDb() {
        GeoTagDAO dao = new GeoTagDAO(RAApplication.getInstance());//MeasurementsDataHelper.getInstance().getGeoTagDao();
        GeoTagModel tag = new GeoTagModel();
        long folderId = RAApplication.getInstance().getCurrentFolderId();
        long roadId = RAApplication.getInstance().getCurrentRoadId();
        long measurementId = RAApplication.getInstance().getCurrentMeasurementId();
        final Calendar nowDate = Calendar.getInstance();
        long time = nowDate.getTimeInMillis();
        tag.setTime(time);
        tag.setFolderId(folderId);
        tag.setRoadId(roadId);
        tag.setMeasurementId(measurementId);
        tag.setLatitude(curLatitude);
        tag.setLongitude(curLongitude);
        tag.setAltitude(curAltitude);
        tag.setSpeed(curSpeed);
        tag.setDistance(curDistance);
        dao.putGeoTag(tag);
    }
}
