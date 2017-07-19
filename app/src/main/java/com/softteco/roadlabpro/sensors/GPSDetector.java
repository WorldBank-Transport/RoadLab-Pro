package com.softteco.roadlabpro.sensors;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

import com.softteco.roadlabpro.BuildConfig;
import com.softteco.roadlabpro.RAApplication;
import com.softteco.roadlabpro.util.Constants;
import com.softteco.roadlabpro.util.ConvertQuantityUtil;
import com.softteco.roadlabpro.util.ExecutorServiceManager;
import com.softteco.roadlabpro.util.PreferencesUtil;
import com.softteco.roadlabpro.util.ScheduleTimer;
import com.softteco.roadlabpro.util.TimeUtil;
import com.softteco.roadlabpro.util.ValidateUtil;

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class GPSDetector implements GpsStatus.Listener, LocationListener, ScheduleTimer.TimerTaskListener {

    public static final String TAG = GPSDetector.class.getName();

    private static final int CHECK_DATA_TIMER_VALUE = 5000;
    private static final long DURATION_TO_FIX_LOST_MS = 10000;
    private static final long MINIMUM_UPDATE_TIME = 0;
    private static final float MINIMUM_UPDATE_DISTANCE = 0.0f;
    private static final long MAX_DISTANCE = 200;

    private float minSpeed;
    private float maxSpeed;
    private float intervalLength = Constants.INTERVAL_LENGTH;

    private boolean gpsEnabled;
    private boolean isGPSEnabled;
    private boolean isNetworkEnabled;
    private boolean gpsFix;

    private int accuracy = -1;
    private long timestamp = 0;

    private volatile double distanceInterval;
    private volatile float recentSpeed;

    private volatile double prevLatitude = 0;
    private volatile double prevLongitude = 0;
    private volatile double prevAltitude = 0;

    private Location lastLocation;
    private ExecutorServiceManager executor;
    private LocationManager locationManager;
    private Set<GpsChangedListener> gpsChangedListeners;
    private Set<GpsMapListener> gpsMapListeners;
    private Set<GpsIntervalsCheckListener> gpsIntervalsCheckListener;
    private BroadcastReceiver receiver;
    private ScheduleTimer timer;

    @Override
    public void onTimer() {
        if (BuildConfig.FAKE) {
            Location testlocation;
            if (lastLocation != null) {
                testlocation = lastLocation;
            } else {
                testlocation = new Location("gps");
                testlocation.setLatitude(Constants.LATITUDE_BELARUS);
                testlocation.setLongitude(Constants.LONGITUDE_BELARUS);
            }
            gpsEnabled = true;
            gpsFix = true;
            testlocation.setAccuracy(20);
            onLocationChanged(testlocation);
        }
    }

    public interface GpsMapListener {
        void onGpsMapListener(Location location);
    }

    public interface GpsChangedListener {
        void onGpsChangedListener(Location location);
    }

    public interface GpsIntervalsCheckListener {
        void gpsListenerOnIntervalChanged(double currentDistance, boolean correctData);
    }

    public GPSDetector() {
        if (gpsMapListeners == null) {
            gpsMapListeners = Collections.synchronizedSet(new HashSet());
        }
        if (gpsIntervalsCheckListener == null) {
            gpsIntervalsCheckListener = Collections.synchronizedSet(new HashSet());
        }
        if (gpsChangedListeners == null) {
            gpsChangedListeners = Collections.synchronizedSet(new HashSet());
        }
    }

    private Context getContext() {
        return RAApplication.getInstance();
    }

    private void getSettings(final Context context) {
        minSpeed = PreferencesUtil.getInstance(context).getSettingsValue(PreferencesUtil.SETTINGS.MIN_SPEED);
        maxSpeed = PreferencesUtil.getInstance(context).getSettingsValue(PreferencesUtil.SETTINGS.MAX_SPEED);
    }

    public double getLatitude() {
        return prevLatitude;
    }

    public double getLongitude() {
        return prevLongitude;
    }

    public double getAltitude() {
        return prevAltitude;
    }

    public synchronized Location getLocation() {
        return lastLocation;
    }

    public void setGpsMapListener(final GpsMapListener listener) {
        if (executor != null) {
            executor.runOperation(new Runnable() {
                @Override
                public void run() {
                    setGpsMapListenerTask(listener);
                }
            });
        }
    }

    public void clearGpsMapListener() {
        if (executor != null) {
            executor.runOperation(new Runnable() {
                @Override
                public void run() {
                    clearGpsMapListenerTask();
                }
            });
        }
    }

    public void removeGpsMapListener(final GpsMapListener listener) {
        if (executor != null) {
            executor.runOperation(new Runnable() {
                @Override
                public void run() {
                    removeGpsMapListenerTask(listener);
                }
            });
        }
    }

    private void setGpsMapListenerTask(final GpsMapListener listener) {
        if (gpsMapListeners == null) {
            gpsMapListeners = Collections.synchronizedSet(new HashSet());
        }
        if (listener != null) {
            gpsMapListeners.add(listener);
        }
    }

    private void clearGpsMapListenerTask() {
        if (gpsMapListeners != null) {
            gpsMapListeners.clear();
        }
    }

    private void removeGpsMapListenerTask(final GpsMapListener listener) {
        if (gpsMapListeners != null) {
            Iterator i = gpsMapListeners.iterator();
            while (i.hasNext()) {
                GpsMapListener l = (GpsMapListener) i.next();
                if (l.equals(listener)) {
                    i.remove();
                }
            }
        }
    }

    public void setGpsIntervalsCheckListener(final GpsIntervalsCheckListener listener) {
        if (gpsIntervalsCheckListener == null) {
            gpsIntervalsCheckListener = Collections.synchronizedSet(new HashSet());
        }
        if (listener != null) {
            gpsIntervalsCheckListener.add(listener);
        }
    }

    public void clearGpsIntervalsCheckListener() {
        if (gpsIntervalsCheckListener != null) {
            gpsIntervalsCheckListener.clear();
        }
    }

    public void removeGpsIntervalsCheckListener(final GpsIntervalsCheckListener listener) {
        if (gpsIntervalsCheckListener != null) {
            Iterator i = gpsIntervalsCheckListener.iterator();
            while (i.hasNext()) {
                GpsIntervalsCheckListener l = (GpsIntervalsCheckListener) i.next();
                if (l.equals(listener)) {
                    i.remove();
                }
            }
        }
    }

    public void setGpsChangedListener(final GpsChangedListener listener) {
        if (listener != null) {
            gpsChangedListeners.add(listener);
        }
    }

    public void clearGpsChangedListener() {
        if (gpsChangedListeners != null) {
            gpsChangedListeners.clear();
        }
    }

    public void removeGpsChangedListener(final GpsChangedListener listener) {
        if (gpsChangedListeners != null) {
            Iterator i = gpsChangedListeners.iterator();
            while (i.hasNext()) {
                GpsChangedListener l = (GpsChangedListener) i.next();
                if (l.equals(listener)) {
                    i.remove();
                }
            }
        }
    }

    public void onLocationChanged(final Location location) {
        if (location != null) {
// enable fake speed for tests:
            if (BuildConfig.FAKE) {
                location.setSpeed(20);
            }
            lastLocation = location;
            sendGpsMapEvent(location);
            timestamp = location.getTime();
            accuracy = (int) location.getAccuracy();
            Log.d(TAG, "onLocationChanged location: lat="
                    + location.getLatitude() + ", lon=" + location.getLongitude()
                    + ", accuracy: " + accuracy + ", state: " + getState().name());
            checkInterval(location);
        }
    }

    private void checkInterval(Location location) {
        if (location == null) {
            return;
        }
        if (!isValidState()) {
            distanceInterval = 0;
            recentSpeed = 0;
            return;
        }
        recentSpeed = location.getSpeed();
        if (!isValidSpeed()) {
            return;
        }
        double distance = 0;
        if (prevLatitude != location.getLatitude()
         || prevLongitude != location.getLongitude()) {
            distance = calcDistance(location);
        }
// enable fake speed for tests:
        if (BuildConfig.FAKE) {
            distance += 20;
        }
        prevLatitude = location.getLatitude();
        prevLongitude = location.getLongitude();
        prevAltitude = location.getAltitude();
        if (distance < intervalLength) {
            distanceInterval += distance;
        } else {
            sendOnIntervalChanged(distanceInterval, false);
            distanceInterval = 0;
            return;
        }
        if (distanceInterval >= intervalLength) {
            boolean correctData = distanceInterval < MAX_DISTANCE;
            sendOnIntervalChanged(distanceInterval, correctData);
            distanceInterval = 0;
        }
    }

    private void sendGpsMapEvent(final Location location) {
        if (executor != null) {
            executor.runOperation(new Runnable() {
                @Override
                public void run() {
                    sendGpsMapEventTask(location);
                }
            });
        }
    }

    private void sendGpsMapEventTask(final Location location) {
        if (gpsMapListeners != null) {
            for (GpsMapListener l : gpsMapListeners) {
                l.onGpsMapListener(location);
            }
        }
    }

    private void sendOnIntervalChanged(final double currentDistance, boolean correctData) {
        Log.d(TAG, "sendOnIntervalChanged : distance= " + currentDistance);
        if (gpsIntervalsCheckListener != null) {
            for (GpsIntervalsCheckListener l : gpsIntervalsCheckListener) {
                l.gpsListenerOnIntervalChanged(currentDistance, correctData);
            }
        }
    }

    public boolean isValidSpeed() {
        float speed = getSpeed();
        return isValidSpeed(speed);
    }

    public boolean isValidSpeed(float speed) {
        return speed >= minSpeed && speed <= maxSpeed;
    }

    public boolean isValidState() {
        return State.EXCELLENT.equals(getState())
                || State.GOOD.equals(getState())
                || State.POOR.equals(getState());
    }

    public float getSpeed() {
        float speedKmPerHour = 0;
        if (isValidState()) {
            speedKmPerHour = ConvertQuantityUtil.metersPerSecondsToKillometrPerHours(recentSpeed);
        }
        return speedKmPerHour;
    }

    private double calcDistance(Location location) {
        float distance = 0;
        float[] distanceArr = new float[2];
        if (location != null) {
            Location.distanceBetween(
                    prevLatitude,
                    prevLongitude,
                    location.getLatitude(),
                    location.getLongitude(), distanceArr);
            if (ValidateUtil.isValidNumber(distanceArr[0])) {
                distance = distanceArr[0];
            }
        }
        Log.d(TAG, "calcDistance : distance= " + distance);
        return distance;
    }

    public double getDistance() {
        return distanceInterval;
    }

    public void init() {
        if (BuildConfig.FAKE) {
            timer = new ScheduleTimer(CHECK_DATA_TIMER_VALUE);
            timer.setOnTimerTaskListener(this);
            timer.start();
        }
        timestamp = TimeUtil.getCurrentTimeMillis();
        executor = new ExecutorServiceManager();
        locationManager = (LocationManager) getContext().getSystemService(Context.LOCATION_SERVICE);
        locationManager.addGpsStatusListener(this);
        getSettings(getContext());
        start();
        registerProvidersChangedReceiver(getContext());
    }

    private void registerProvidersChangedReceiver(Context broadcastContext) {
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                Log.i(TAG, "registerProvidersChangedReceiver, action: " + action);
                if (LocationManager.PROVIDERS_CHANGED_ACTION.equals(action)) {
                    start();
                }
            }
        };
        if (receiver != null && broadcastContext != null) {
            IntentFilter filter = new IntentFilter(LocationManager.PROVIDERS_CHANGED_ACTION);
            broadcastContext.registerReceiver(receiver, filter);
        }
    }

    private void unregisterProvidersChangedReceiver(Context broadcastContext) {
        if (receiver != null && broadcastContext != null) {
            broadcastContext.unregisterReceiver(receiver);
            receiver = null;
        }
    }

    public boolean start() {
        timestamp = TimeUtil.getCurrentTimeMillis();
        if (locationManager != null) {
            lastLocation = checkLocationProviders();
            return true;
        }
        return false;
    }

    public void reset() {
        prevLatitude = 0;
        prevLongitude = 0;
        prevAltitude = 0;
        distanceInterval = 0;
        recentSpeed = 0;
    }

    private void stop() {
        if (locationManager != null) {
            locationManager.removeUpdates(this);
        }
    }

    public void destroy() {
        stop();
        if (locationManager != null) {
            locationManager.removeGpsStatusListener(this);
        }
        if (BuildConfig.FAKE) {
            if (timer != null) {
                timer.stop();
            }
        }
        locationManager = null;
        if (executor != null) {
            executor.shutdown();
        }
        clearGpsChangedListener();
        clearGpsMapListener();
        clearGpsIntervalsCheckListener();
        unregisterProvidersChangedReceiver(getContext());
    }

    public Location checkLocationProviders() {
        Location location = null;
        try {
            // getting GPS status
            isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

            // getting network status
            isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

            if (!isGPSEnabled && !isNetworkEnabled) {
                // no network provider is enabled
                //Toast.makeText(context, "No location providers are available", Toast.LENGTH_LONG).show();
            } else {
                // if GPS Enabled get lat/long using GPS Services
                if (isGPSEnabled) {
                    locationManager.requestLocationUpdates(
                            LocationManager.GPS_PROVIDER,
                            MINIMUM_UPDATE_TIME, MINIMUM_UPDATE_DISTANCE, this);
                    if (locationManager != null) {
                        location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                    }
                }
                if (isNetworkEnabled) {
                    locationManager.requestLocationUpdates(
                            LocationManager.NETWORK_PROVIDER,
                            MINIMUM_UPDATE_TIME, MINIMUM_UPDATE_DISTANCE, this);
                    if (location == null && locationManager != null) {
                        location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return location;
    }

    public Location getLastKnownLocation() {
        if (locationManager != null) {
            return locationManager.getLastKnownLocation("gps");
        }
        return null;
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        Log.d(TAG, "onStatusChanged :: " + provider + ", " + status);
    }

    @Override
    public void onProviderEnabled(String provider) {
        Log.d(TAG, "onProviderEnabled :: " + provider);
    }

    @Override
    public void onProviderDisabled(String provider) {
        Log.d(TAG, "onProviderDisabled :: " + provider);
    }

    @Override
    public void onGpsStatusChanged(int event) {

        switch (event) {
            case GpsStatus.GPS_EVENT_FIRST_FIX:
                gpsEnabled = true;
                gpsFix = true;
                break;
            case GpsStatus.GPS_EVENT_SATELLITE_STATUS:
                gpsEnabled = true;
                gpsFix = System.currentTimeMillis() - timestamp < DURATION_TO_FIX_LOST_MS;
                break;
            case GpsStatus.GPS_EVENT_STARTED:
                gpsEnabled = true;
                gpsFix = false;
                break;
            case GpsStatus.GPS_EVENT_STOPPED:
                gpsEnabled = false;
                gpsFix = false;
                break;
            default:
                return;
        }
        //Log.d(TAG, "onGpsStatusChanged :: " + event);
    }

    public int getAccuracyValue() {
        State state = getState();
        int accuracy = 0;
        switch (state) {
            case EXCELLENT:
                accuracy = 0;
                break;
            case GOOD:
                accuracy = 1;
                break;
            default:
                accuracy = 2;
                break;
        }
        return accuracy;
    }

    public State getState() {
        if (!gpsEnabled) {
            return State.DISABLED;
        }
        if (accuracy <= 10) {
            return State.EXCELLENT;
        }
        if (accuracy <= 30) {
            return State.GOOD;
        }
        if (accuracy <= 100) {
            return State.POOR;
        }
        if (!gpsFix) {
            return State.WAITING_FIX;
        }
        return State.UNUSABLE;
    }

    public enum State {
        DISABLED, WAITING_FIX, EXCELLENT, GOOD, POOR, UNUSABLE
    }
}
