package com.softteco.roadlabpro.algorithm;

import android.util.Log;

import com.softteco.roadlabpro.RAApplication;
import com.softteco.roadlabpro.sensors.data.AccelerometerDataEntry;
import com.softteco.roadlabpro.sqlite.model.BumpModel;
import com.softteco.roadlabpro.sqlite.model.ProcessedDataModel;
import com.softteco.roadlabpro.sqlite.model.RecordDetailsModel;
import com.softteco.roadlabpro.util.PreferencesUtil;
import com.softteco.roadlabpro.util.ValidateUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ppp on 14.04.2015.
 */
public class RoadConditionDetection {

    private static final String TAG = RoadConditionDetection.class.getSimpleName();

    public static final float G = 9.80665f;

    private static final float SPEED_1 = 30.0f;
    private static final float SPEED_2 = 50.0f;

    private static final float ANGLE_THRESHOLD = 15;

    public static float IRI_DEFAULT_VALUE = 1f;

    private static final float IRI_QUALITY_1 = 2;
    private static final float IRI_QUALITY_2 = 4;
    private static final float IRI_QUALITY_3 = 6;

    private float quality1 = IRI_QUALITY_1;
    private float quality2 = IRI_QUALITY_2;
    private float quality3 = IRI_QUALITY_3;

    private List<BumpModel> detectedBumps;

    private float bumpThreshold1 = 1.0f * G;
    private float bumpThreshold2 = 1.2f * G;

    private long timeInterval = 500;
    private long prevTime = 0;

    private DeviceStateDetector stateDetector;

    public RoadConditionDetection() {
        init();
    }

    /**
     * Resets buffers to start new calculations
     */
    public void reset() {
        prevTime = 0;
        if (detectedBumps != null) {
            detectedBumps.clear();
        }
    }

    /**
     * Init parameters of algorithms
     */
    public void init() {
        reset();
        stateDetector = new DeviceStateDetector();
        timeInterval = PreferencesUtil.getInstance().getTimeInterval().getTimeInterval();
        bumpThreshold1 = PreferencesUtil.getInstance().getLess50SpeedThresholds() * G;
        bumpThreshold2 = PreferencesUtil.getInstance().getMore50SpeedThresholds() * G;
    }

    /**
     * Gets list of detected bumps
     * @return list of detected bumps
     */
    public List<BumpModel> getDetectedBumps() {
        return detectedBumps;
    }

    /**
     * Adds object with detected bump data
     * @param r - accelerometer data values
     * @param isFixed - indicates device vertical/horizontal state (false - horizontal, true - vertical)
     */
    public void addBumpEvent(RecordDetailsModel r, boolean isFixed) {
        if (detectedBumps == null) {
            detectedBumps = new ArrayList<>();
        }
        BumpModel bump = new BumpModel();
        bump.setRecordId(r.getRecordId());
        bump.setAccelerationX(r.getAccelerometerX());
        bump.setAccelerationY(r.getAccelerometerY());
        bump.setAccelerationZ(r.getAccelerometerZ());
        bump.setTime(r.getTime());
        bump.setSpeed(r.getAverageSpeed());
        bump.setLatitude(r.getLatitude());
        bump.setLongitude(r.getLongitude());
        bump.setAltitude(r.getAltitude());
        bump.setFixed(isFixed);
        detectedBumps.add(bump);
    }

    /**
     * Calculates threshold value that use in bumps detection logic
     * @param speed - average speed value
     * @param isFixed - indicates device vertical/horizontal state (false - horizontal, true - vertical)
     * @return threshold value that using in bumps detection logic
     */
    public float getThreshold(float speed, boolean isFixed) {
        float threshold1 = bumpThreshold1;
        float threshold2 = bumpThreshold2;
        if (speed >= SPEED_1 && speed <= SPEED_2) {
            return threshold1;
        } else if (speed > SPEED_2) {
            return threshold2;
        }
        return threshold1;
    }

    /**
     * Calculates Standard deviation value of given records
     * @param records - records (storing accelerometer data) to calculate StandardDeviation value
     * @param angleThreshold - threshold value of rotation angle to filter values higher than this value
     * @return calculated value of Standard deviation
     */
    public static float getSTD(List<RecordDetailsModel> records, float angleThreshold) {
        float sumAvs = 0;
        float subAvs = 0;
        int count = 0;
        float std = 0;
        float meanAv = getMeanAv(records, angleThreshold);
        for (RecordDetailsModel r : records) { //Feng Guo: how data value are put into records: based on time interval or distance
                                               //data values adding to records for interval based on calculated distance by GPS
                                               //e.g. if passed distance >= 100 meters then algorithm calculations started with collected data from accelerometer)
            if (!isValidRotationAngle(r, angleThreshold)) {
                continue;
            }
            subAvs = getAv(r) - meanAv;
            sumAvs += subAvs * subAvs;
            count++;
        }
        std = (float) Math.sqrt(1.0f / (float) (count - 1) * sumAvs);
        return std;
    }

    /**
     * Validates value of rotation angle for accelerometer data point
     * @param r - Accelerometer values for one point
     * @param angleThreshold - threshold value of rotation angle to filter values that than this value
     * @return result of validation
     */
    private static boolean isValidRotationAngle(RecordDetailsModel r, float angleThreshold) {
        return Math.abs(r.getRotationAngle()) <= angleThreshold;
    }

    /**
     * Calculates vertical acceleration vector value
     * @param gx - X axis value of gravity vector
     * @param gy - Y axis value of gravity vector
     * @param gz - Z axis value of gravity vector
     * @param ax - X axis value of linear acceleration vector
     * @param ay - Y axis value of linear acceleration vector
     * @param az - Z axis value of linear acceleration vector
     * @return value of vertical acceleration vector
     */
    public static float getAv(float gx, float gy, float gz, float ax, float ay, float az) {
        float av = (ax*gx + ay*gy + az*gz) / G;
        return av;
    }

    /**
     * Calculates vertical acceleration vector value
     * @param data - object contains accelerometer data
     * @return value of vertical acceleration vector
     */
    public static float getAv(AccelerometerDataEntry data) {
        float gx = data.getAccelerometerGravityX();
        float gy = data.getAccelerometerGravityY();
        float gz = data.getAccelerometerGravityZ();
        float ax = data.getAccelerometerLinearX();
        float ay = data.getAccelerometerLinearY();
        float az = data.getAccelerometerLinearZ();
        return getAv(gx, gy, gz, ax, ay, az);
    }

    /**
     * Calculates vertical acceleration vector value
     * @param record - object contains accelerometer data
     * @return value of vertical acceleration vector
     */
    public static float getAv(RecordDetailsModel record) {
        float gx = record.getAccelerometerGravityX();
        float gy = record.getAccelerometerGravityY();
        float gz = record.getAccelerometerGravityZ();
        float ax = record.getAccelerometerLinearX();
        float ay = record.getAccelerometerLinearY();
        float az = record.getAccelerometerLinearZ();
        return getAv(gx, gy, gz, ax, ay, az);
    }

    /**
     * Calculates vertical acceleration vector value for given data points
     * @param records - list of objects that contains accelerometer data
     * @return value of vertical acceleration vector
     */
    public static float getMeanAv(List<RecordDetailsModel> records, float angleThreshold) {
        float avrAv = 0;
        int count = 0;
        for (RecordDetailsModel r : records) {
            if (!isValidRotationAngle(r, angleThreshold)) {
                continue;
            }
            avrAv += getAv(r);
            count++;
        }
        avrAv /= count;
        return avrAv;
    }

    /**
     * Runs bumps detection calculations
     * @param av - average value of vertical acceleration vector
     * @param speed - average speed value
     * @param isFixed - indicates device vertical/horizontal state (false - horizontal, true - vertical)
     * @return returns true if bump detected, otherwise false
     */
    public boolean detectBump(float av, float speed, boolean isFixed) {
        float threshold = getThreshold(speed, isFixed);
        Log.i(TAG, "detectBump: av: " + av + ", speed: " + speed + ", threshold: " + threshold + ", isFixed: " + isFixed);
        if (av >= threshold) {
            return true;
        }
        return false;
    }

    /**
     * Runs calculation process of interval road roughness
     * @param records - Data points to calculate interval roughness
     * @return results of algorithm calculations
     */
    public ProcessedDataModel calculate(List<RecordDetailsModel> records) {
        return calculate(records, -1);
    }

    /**
     * Runs calculation process of interval road roughness
     * @param records - Data points to calculate interval roughness
     * @param distance - interval distance
     * @return results of algorithm calculations
     */
    public ProcessedDataModel calculate(List<RecordDetailsModel> records, double distance) {
        int bumpsCount = 0;
        float speedAvg = 0;
        float meanAv = 0;
        float sumAv = 0;
        int avgAvCount = 0;
        int count = 0;
        if (records == null || records.size() == 0) {
            Log.e(TAG, "[ No data records to process ] algorithm calculation stopped");
            return null;
        }
        ProcessedDataModel processedData = new ProcessedDataModel();
        if (records.get(0) != null) {
            processedData.setTime(records.get(0).getTime() + records.get(0).getTimeStamp());
        }
        stateDetector.calcState(records);
        boolean isDeviceFixed = stateDetector.isDeviceFixedState();
        for (RecordDetailsModel r : records) {
            if (!isValidRotationAngle(r, ANGLE_THRESHOLD)) {
                Log.e(TAG, "[ Non valid RotationAngle detected ] Interval: "
                + r.getTime() + ", angle: "
                + r.getRotationAngle() + ", skip calculations for this value");
                continue;
            }
            final float av = getAv(r);
            sumAv += Math.abs(av);
            avgAvCount++;
            boolean isNextWindow = isNextWindow(r.getTime());
            boolean isEndOfList = count == records.size() - 1;
            if (isNextWindow || isEndOfList) {
                meanAv = sumAv / (float) avgAvCount;
                sumAv = 0;
                avgAvCount = 0;
                if (detectBump(meanAv, speedAvg / (float) count, isDeviceFixed)) {
                    bumpsCount++;
                    addBumpEvent(r, isDeviceFixed);
                    Log.i(TAG, "interval: " + processedData.getTime() + ", Mean Av: " + meanAv + ", bumpsCount: " + bumpsCount);
                }
            }
            speedAvg += r.getAverageSpeed();
            count++;
        }
        float stdDeviation = getSTD(records, ANGLE_THRESHOLD);
        if(!ValidateUtil.isValidNumber(stdDeviation) || stdDeviation <= 0) {
            Log.e(TAG, "[ Too low standard deviation value ] algorithm calculation stopped");
            return null;
        }
        float avgSpeed = speedAvg / (float) count;
        float iri = calculateIRI(stdDeviation, avgSpeed, isDeviceFixed);
        RoadQuality roadQuality = getRoadQuality(iri);

        Log.d(TAG, "stddev: " + stdDeviation + ", IRI: " + iri + ", records count: " + records.size());
        Log.d(TAG, "isDeviceFixed: " + isDeviceFixed);
        Log.d(TAG, "road Quality: " + roadQuality.name());

        double latStart = 0;
        double lonStart = 0;
        double altStart = 0;
        double latEnd = 0;
        double lonEnd = 0;
        double altEnd = 0;
        if (records.get(0) != null) {
            latStart = records.get(0).getLatitude();
            lonStart = records.get(0).getLongitude();
            altStart = records.get(0).getAltitude();
            if (latStart == 0 || lonStart == 0) {
                latStart = records.get(0).getCurLatitude();
                lonStart = records.get(0).getCurLongitude();

            }
            if (altStart == 0) {
                altStart = records.get(0).getCurAltitude();
            }
        }
        int lastId = records.size() - 1;
        if (records.get(lastId) != null) {
            latEnd = records.get(lastId).getLatitude();
            lonEnd = records.get(lastId).getLongitude();
            altEnd = records.get(lastId).getAltitude();
            if (latEnd == 0 || lonEnd == 0) {
                latEnd = records.get(lastId).getCurLatitude();
                lonEnd = records.get(lastId).getCurLongitude();
            }
            if (altEnd == 0) {
                altEnd = records.get(lastId).getCurAltitude();
            }
        }
        if (distance >= 0) {
            processedData.setDistance(distance);
        }
        processedData.setMeasurementId(records.get(0).getMeasurementId());
        processedData.setIri(iri);
        processedData.setFixed(isDeviceFixed);
        processedData.setStdDeviation(stdDeviation);
        processedData.setBumps(bumpsCount);
        processedData.setCoordsStart(latStart, lonStart, altStart);
        processedData.setCoordsEnd(latEnd, lonEnd, altEnd);
        processedData.setSpeed(avgSpeed);
        processedData.setCategory(roadQuality);
        processedData.setItemsCount(records.size());
        return processedData;
    }

    private boolean isNextWindow(long time) {
        if (time - prevTime >= timeInterval) {
            prevTime = time;
            return true;
        }
        return false;
    }

    /**
     * Does road quality assessment on given IRI value
     * @param iri - IRI value
     * @return road quality assessment
     */
    public RoadQuality getRoadQuality(float iri) {
        if (iri < quality1) {
            return RoadQuality.EXCELLENT;
        } else if (iri > quality1 && iri <= quality2) {
            return RoadQuality.GOOD;
        } else if (iri > quality2 && iri <= quality3) {
            return RoadQuality.FAIR;
        } else {
            return RoadQuality.POOR;
        }
    }

    /**
     * Calculates IRI value
     * @param std - standard deviation value
     * @param speed - average speed value
     * @param isFixed - indicates device vertical/horizontal state (false - horizontal, true - vertical)
     * @return calculated value of IRI
     */
    public float calculateIRI(float std, float speed, boolean isFixed) {
        float interceptConst = RAApplication.getInstance().getIriTable().getIntercept(isFixed, std);
        float sdConst = RAApplication.getInstance().getIriTable().getSd(isFixed, std);
        float speedConst = RAApplication.getInstance().getIriTable().getSpeed(isFixed, std);
        float sdConstPow2 = RAApplication.getInstance().getIriTable().getSdPow2(isFixed, std);
        float sdSpeedConst = RAApplication.getInstance().getIriTable().getSdSpeed(isFixed, std);
        float iri = interceptConst + sdConst*std + speedConst*speed + sdConstPow2*std*std + sdSpeedConst*std*speed;
        if (iri <= 1) {
            iri = IRI_DEFAULT_VALUE;
        }
        return iri;
    }
}
