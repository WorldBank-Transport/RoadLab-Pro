package com.softteco.roadlabpro.algorithm;

import com.softteco.roadlabpro.sqlite.model.RecordDetailsModel;

import java.util.List;

/**
 * Created by ppp on 15.05.2015.
 */
public class DeviceStateDetector {

    private static final int VECTOR_LENGTH = 3;
    private static final int VECTOR_X = 0;
    private static final int VECTOR_Y = 1;
    private static final int VECTOR_Z = 2;

    public static final int DEVICE_STATE_VERTICAL   = 0;
    public static final int DEVICE_STATE_HORIZONTAL = 1;

    public static final float STATE_DETECTION_THRESHOLD = RoadConditionDetection.G * 0.7f;

    private volatile int deviceState = -1;

    public int getDeviceState() {
        return deviceState;
    }

    public void clear() {
        deviceState = -1;
    }

    /**
     * Calculates device Horizontal/Vertical state
     * @param array - array of gravity vector values
     * @return calculated device state
     */
    public int calcState(float[] array) {
        int state = -1;
        if (array != null && array.length == VECTOR_LENGTH) {
            float gx = array[VECTOR_X];
            float gy = array[VECTOR_Y];
            float gz = array[VECTOR_Z];
            if (Math.abs(gx) >= STATE_DETECTION_THRESHOLD
             || Math.abs(gy) >= STATE_DETECTION_THRESHOLD) {
                state = DEVICE_STATE_VERTICAL;
            } else  if (Math.abs(gz) >= STATE_DETECTION_THRESHOLD) {
                state = DEVICE_STATE_HORIZONTAL;
            }
        }
        deviceState = state;
        return state;
    }

    /**
     * Calculates device Horizontal/Vertical state
     * @param r - Object contains Accelerometer data values
     * @return calculated device state
     */
    public int calcState(RecordDetailsModel r) {
        return calcState(new float[]{
                r.getAccelerometerGravityX(),
                r.getAccelerometerGravityY(),
                r.getAccelerometerGravityZ()});
    }

    /**
     * Calculates device Horizontal/Vertical state
     * @param records - List of objects contains Accelerometer data values
     * @return calculated device state
     */
    public int calcState(List<RecordDetailsModel> records) {
        int fixedState = 0;
        int nonFixedState = 0;
        int state = DEVICE_STATE_HORIZONTAL;
        int curState = -1;
        for (RecordDetailsModel r : records) {
            curState = calcState(r);
            if (curState == DEVICE_STATE_HORIZONTAL) {
                nonFixedState++;
            } else if (curState == DEVICE_STATE_VERTICAL) {
                fixedState++;
            }
        }
        if (fixedState > nonFixedState) {
            state = DEVICE_STATE_VERTICAL;
        } else if (nonFixedState > fixedState) {
            state = DEVICE_STATE_HORIZONTAL;
        }
        return state;
    }

    /**
     * Returns flag of device Horizontal/Vertical state
     * @return returns device Horizontal/Vertical state flag (false - horizontal, true - vertical)
     */
    public boolean isDeviceFixedState() {
        int deviceState = getDeviceState();
        if (deviceState == DeviceStateDetector.DEVICE_STATE_VERTICAL) {
            return true;
        } else if (deviceState == DeviceStateDetector.DEVICE_STATE_HORIZONTAL) {
            return false;
        }
        return false;
    }
}
