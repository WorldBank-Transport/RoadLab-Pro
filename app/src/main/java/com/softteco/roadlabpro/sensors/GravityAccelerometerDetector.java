package com.softteco.roadlabpro.sensors;

import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

import com.softteco.roadlabpro.RAApplication;
import com.softteco.roadlabpro.algorithm.DeviceStateDetector;
import com.softteco.roadlabpro.sensors.data.AccelerometerDataEntry;
import com.softteco.roadlabpro.sensors.data.SensorData;
import com.softteco.roadlabpro.sensors.software.GravitySoftwareSensor;
import com.softteco.roadlabpro.sensors.software.RotationSoftwareSensor;
import com.softteco.roadlabpro.util.Constants;
import com.softteco.roadlabpro.util.PreferencesUtil;

import org.acra.ACRA;

import java.util.List;

public class GravityAccelerometerDetector implements SensorEventListener {

    public static final String TAG = GravityAccelerometerDetector.class.getName();
    public static final int ROTATION_EVENTS_COUNT = 5;
    public static final int ROTATE_ANALYZE_INTERVAL = 5000;

    public static final int DATA_X = 0;
    public static final int DATA_Y = 1;
    public static final int DATA_Z = 2;

    private DeviceStateDetector deviceStateDetector;
    private SensorManager sensorManager;
//    private ExecutorServiceManager executor;

    private SensorsDetectorListener sensorListener;

    private long previousDataDetectionTime;
    private long firstRotationDetectionTime;

    private boolean linearAccelerationAvailable;
    private boolean gravityAvailable;

    private GravitySoftwareSensor softGravitySensor;
    private RotationSoftwareSensor softRotationSensor;

    private float deltaAngle = 15;
    private long rotateDeltaTime = 1000;
    private int rotateEventCount = 0;
    private int noRotateEventCount = 0;
    private volatile boolean eventsEnabled = false;

    public interface SensorsDetectorListener {
        void onSensorChanged(SensorData data);
    }

    private AccelerometerDataEntry testLastAccelerometerDataEntry = new AccelerometerDataEntry();
    private AccelerometerDataEntry testPreviousAccelerometerDataEntry = new AccelerometerDataEntry();

    public GravityAccelerometerDetector(Context context) {
        if (context == null) {
            throw new IllegalArgumentException("Context must not be null");
        }
        deviceStateDetector = new DeviceStateDetector();
//        executor = new ExecutorServiceManager();
        sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
//        deltaAngle = PreferencesUtil.getInstance(context).getSettingsValue(PreferencesUtil.SETTINGS.DELTA_ANGLE);
        rotateDeltaTime = (long) (PreferencesUtil.getInstance(context).getSettingsValue(PreferencesUtil.SETTINGS.DELTA_TIME) * 1000f);
    }

    public boolean start() {
        if (eventsEnabled) {
            return false;
        }
        Log.d(TAG, "start");
        if (sensorManager != null) {
            testLastAccelerometerDataEntry = new AccelerometerDataEntry();
            testPreviousAccelerometerDataEntry = new AccelerometerDataEntry();
            gravityAvailable = checkSensorAvailability(sensorManager, Sensor.TYPE_GRAVITY);
            linearAccelerationAvailable = checkSensorAvailability(sensorManager, Sensor.TYPE_LINEAR_ACCELERATION);

            if (gravityAvailable) {
                sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY), SensorManager.SENSOR_DELAY_NORMAL);
            } else {
                softGravitySensor = new GravitySoftwareSensor();
            }
            if (linearAccelerationAvailable) {
                sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION), SensorManager.SENSOR_DELAY_NORMAL);
            } else {
                softGravitySensor = new GravitySoftwareSensor();
            }
            sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL);
            eventsEnabled = true;
            sendOnShakeDetectionBroadcast(false);
            return true;
        }
        return false;
    }

    private boolean checkSensorAvailability(SensorManager sensorManager, int type) {
        List<Sensor> sensors = sensorManager.getSensorList(type);
        return !(sensors == null || sensors.size() == 0);
    }

    private void sendSensorUnavailableReport(int type) {
        switch(type) {
            case Sensor.TYPE_GYROSCOPE:
            ACRA.getErrorReporter().handleSilentException(new Throwable("no sensors are found for type TYPE_GYROSCOPE"));
            break;
            //default:
            //ACRA.getErrorReporter().handleSilentException(new Throwable("test error send"));
            //break;
        }
    }

    public void stop() {
        if (eventsEnabled && sensorManager != null) {
            sensorManager.unregisterListener(this);
        }
        clearDeviceState();
        eventsEnabled = false;
        Log.d(TAG, "stop");
    }

    public void destroy() {
        stop();
        sensorManager = null;
//        if (executor != null) {
//            executor.shutdown();
//        }
        Log.d(TAG, "destroy");
    }

    public void setOnSensorChangedListener(SensorsDetectorListener listener) {
        this.sensorListener = listener;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        Log.d(TAG, "onAccuracyChanged");
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        switch (event.sensor.getType()) {
            case Sensor.TYPE_ACCELEROMETER:
                testLastAccelerometerDataEntry.setAccelerometerX(event.values[DATA_X]);
                testLastAccelerometerDataEntry.setAccelerometerY(event.values[DATA_Y]);
                testLastAccelerometerDataEntry.setAccelerometerZ(event.values[DATA_Z]);
                if (!gravityAvailable || !linearAccelerationAvailable) {
                    calcSoftwareGravityVector(event);
                    calcRotationAngle();
                    float[] gravityVector = new float[] {
                        testLastAccelerometerDataEntry.getAccelerometerGravityX(),
                        testLastAccelerometerDataEntry.getAccelerometerGravityY(),
                        testLastAccelerometerDataEntry.getAccelerometerGravityZ() };
                    deviceStateDetector.calcState(gravityVector);
                    processLinearAcceleration();
                }
                break;
            case Sensor.TYPE_LINEAR_ACCELERATION:
                testLastAccelerometerDataEntry.setAccelerometerLinearX(event.values[DATA_X]);
                testLastAccelerometerDataEntry.setAccelerometerLinearY(event.values[DATA_Y]);
                testLastAccelerometerDataEntry.setAccelerometerLinearZ(event.values[DATA_Z]);
                processLinearAcceleration();
                break;
            case Sensor.TYPE_GRAVITY:
                testLastAccelerometerDataEntry.setAccelerometerGravityX(event.values[DATA_X]);
                testLastAccelerometerDataEntry.setAccelerometerGravityY(event.values[DATA_Y]);
                testLastAccelerometerDataEntry.setAccelerometerGravityZ(event.values[DATA_Z]);
                deviceStateDetector.calcState(event.values);
                calcRotationAngle();
                break;
        }
        if (sensorListener != null) {
            SensorData data = new SensorData(event.sensor.getType(), event.values);
            sensorListener.onSensorChanged(data);
        }
    }

//    private void calcDeviceStateProcess(final float[] values) {
//        final float[] stateValues = new float[3];
//        stateValues[DATA_X] = values[DATA_X];
//        stateValues[DATA_Y] = values[DATA_Y];
//        stateValues[DATA_Z] = values[DATA_Z];
//        if (executor != null) {
//            executor.runOperation(new Runnable() {
//                @Override
//                public void run() {
//                    deviceStateDetector.calcState(stateValues);
//                }
//            });
//        }
//    }
//
//    private void calcRotationAngleProcess() {
//        if (executor != null) {
//            executor.runOperation(new Runnable() {
//                @Override
//                public void run() {
//                    calcRotationAngle();
//                }
//            });
//        }
//    }

    private void clearDeviceState() {
        if (deviceStateDetector != null) {
            deviceStateDetector.clear();
        }
    }

    public int getDeviceState() {
        if (deviceStateDetector != null) {
            return deviceStateDetector.getDeviceState();
        }
        return -1;
    }

    private void processLinearAcceleration() {
        long currentTimeMillis = System.currentTimeMillis();
        long interval = currentTimeMillis - previousDataDetectionTime;
        //Log.e("inerval", " " + interval);
        //   if (interval > 500) {
        testLastAccelerometerDataEntry.calc(currentTimeMillis - previousDataDetectionTime);

        // }
    }

    private void calcSoftwareGravityVector(SensorEvent event) {
        softGravitySensor.setValues(event.values);
        float[] gravityValues = softGravitySensor.calculate();
        testLastAccelerometerDataEntry.setAccelerometerGravityX(gravityValues[DATA_X]);
        testLastAccelerometerDataEntry.setAccelerometerGravityY(gravityValues[DATA_Y]);
        testLastAccelerometerDataEntry.setAccelerometerGravityZ(gravityValues[DATA_Z]);
        if (sensorListener != null) {
            SensorData data = new SensorData(Sensor.TYPE_GRAVITY, gravityValues);
            sensorListener.onSensorChanged(data);
        }
        if (!linearAccelerationAvailable) {
            float[] linearAccelerationValues = softGravitySensor.getLinearAcceleration();
            calcSoftwareLinearAccelerationVector(linearAccelerationValues);
        }
    }

    private void calcSoftwareLinearAccelerationVector(float[] linearAccelerationValues) {
        if (linearAccelerationValues != null && testLastAccelerometerDataEntry != null) {
            testLastAccelerometerDataEntry.setAccelerometerLinearX(linearAccelerationValues[DATA_X]);
            testLastAccelerometerDataEntry.setAccelerometerLinearY(linearAccelerationValues[DATA_Y]);
            testLastAccelerometerDataEntry.setAccelerometerLinearZ(linearAccelerationValues[DATA_Z]);
        }
        if (sensorListener != null && linearAccelerationValues!= null) {
            SensorData data = new SensorData(Sensor.TYPE_LINEAR_ACCELERATION, linearAccelerationValues);
            sensorListener.onSensorChanged(data);
        }
        processLinearAcceleration();
    }

    private void calcRotationAngle() {
        long currentTimeMillis = System.currentTimeMillis();
        long interval = currentTimeMillis - previousDataDetectionTime;
        if (interval >= rotateDeltaTime) {
            final double Gx0 = testPreviousAccelerometerDataEntry.getAccelerometerGravityX();
            final double Gy0 = testPreviousAccelerometerDataEntry.getAccelerometerGravityY();
            final double Gz0 = testPreviousAccelerometerDataEntry.getAccelerometerGravityZ();
            final double Gx = testLastAccelerometerDataEntry.getAccelerometerGravityX();
            final double Gy = testLastAccelerometerDataEntry.getAccelerometerGravityY();
            final double Gz = testLastAccelerometerDataEntry.getAccelerometerGravityZ();
            final double G0 = Math.sqrt(Gx0 * Gx0 + Gy0 * Gy0 + Gz0 * Gz0);
            final double G = Math.sqrt(Gx * Gx + Gy * Gy + Gz * Gz);
            final double cos = (Gx0 * Gx + Gy0 * Gy + Gz0*Gz) / (G0 * G);
            final double angle = Math.toDegrees(Math.acos(cos));
            final boolean condition = !( Math.abs(angle) <= 1 ) || ( Math.abs(testPreviousAccelerometerDataEntry.getRotationAngle()) > 1 );

            testLastAccelerometerDataEntry.setRotationAngle(angle);
            testPreviousAccelerometerDataEntry.setAccelerometerGravityX((float) Gx);
            testPreviousAccelerometerDataEntry.setAccelerometerGravityY((float) Gy);
            testPreviousAccelerometerDataEntry.setAccelerometerGravityZ((float) Gz);

            if (!( Math.abs(angle) <= deltaAngle)
                    || ( Math.abs(testPreviousAccelerometerDataEntry.getRotationAngle()) > deltaAngle )) {
                testPreviousAccelerometerDataEntry.setRotationAngle(angle);
                if (rotateEventCount == 0) {
                    firstRotationDetectionTime = currentTimeMillis;
                }

                long rotationInterval = currentTimeMillis - firstRotationDetectionTime;

                Log.e(" ", "rotationInterval: " + rotationInterval + " rotateEventCount: " + rotateEventCount);
                rotateEventCount++;
                if (rotationInterval >= ROTATE_ANALYZE_INTERVAL) {
                    if (rotateEventCount >= ROTATION_EVENTS_COUNT) {
                        testLastAccelerometerDataEntry.setDeviceNotFixed(true);
                        sendOnShakeDetectionBroadcast(true);
                        rotateEventCount = 0;
                    }
                }
            } else {
                if (testLastAccelerometerDataEntry.isDeviceNotFixed()) {
                    noRotateEventCount++;
                    if (noRotateEventCount == ROTATION_EVENTS_COUNT) {
                        testLastAccelerometerDataEntry.setDeviceNotFixed(false);
                        sendOnShakeDetectionBroadcast(false);
                        noRotateEventCount = 0;
                    }
                }
            }
            previousDataDetectionTime = currentTimeMillis;
        }
    }

    private void sendOnShakeDetectionBroadcast(boolean shakeDetected) {
        if (shakeDetected) {
            RAApplication.getInstance().sendBroadcast(new Intent(Constants.ACTION_DEVICE_NOT_FIXED));
        } else {
            RAApplication.getInstance().sendBroadcast(new Intent(Constants.ACTION_DEVICE_FIXED));
        }
    }

    public AccelerometerDataEntry getLastAccelerometerDataEntry() {
        return testLastAccelerometerDataEntry;
    }

    public void setTestLastAccelerometerDataEntry(AccelerometerDataEntry testLastAccelerometerDataEntry) {
        this.testLastAccelerometerDataEntry = testLastAccelerometerDataEntry;
    }

    public synchronized boolean isValidRotationAngle() {
        boolean validAngle = false;
        if (testLastAccelerometerDataEntry != null) {
            return !testLastAccelerometerDataEntry.isDeviceNotFixed();
        }
        return validAngle;
    }
}