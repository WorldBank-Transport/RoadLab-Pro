package com.softteco.roadlabpro.sqlite.model;

import java.io.Serializable;

/**
 * Created by Vadim Alenin on 4/6/2015.
 */
public class RecordDetailsModel implements Serializable {

    public static final String TAG = RecordDetailsModel.class.getName();

    private long id;
    private long recordId;
    private int roadId;
    private long measurementId;
    private long timeStamp;
    private long time;
    private double latitude;
    private double longitude;
    private double altitude;
    private double curLatitude;
    private double curLongitude;
    private double curAltitude;

    private double distance;
    private float averageSpeed;
    private float accelerometerX;
    private float accelerometerY;
    private float accelerometerZ;
    private float accelerometerLinearX;
    private float accelerometerLinearY;
    private float accelerometerLinearZ;
    private float accelerometerGravityX;
    private float accelerometerGravityY;
    private float accelerometerGravityZ;
    private double rotationAngle;
    private int gpsAccuracy; // 0 - GOOD, 1 - FAIR, 2 - BAD
    private int intervalNumber;

    public RecordDetailsModel() {
    }

    public long getId() {
        return id;
    }

    public void setId(final long id) {
        this.id = id;
    }

    public long getRecordId() {
        return recordId;
    }

    public void setRecordId(final long recordId) {
        this.recordId = recordId;
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(final long timeStamp) {
        this.timeStamp = timeStamp;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(final double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(final double longitude) {
        this.longitude = longitude;
    }

    public void setAltitude(double altitude) {
        this.altitude = altitude;
    }

    public double getAltitude() {
        return altitude;
    }

    public double getCurLatitude() {
        return curLatitude;
    }

    public void setCurLatitude(final double curLatitude) {
        this.curLatitude = curLatitude;
    }

    public double getCurLongitude() {
        return curLongitude;
    }

    public void setCurLongitude(final double curLongitude) {
        this.curLongitude = curLongitude;
    }

    public void setCurAltitude(double curAltitude) {
        this.curAltitude = curAltitude;
    }

    public double getCurAltitude() {
        return curAltitude;
    }

    public int getGpsAccuracy() {
        return gpsAccuracy;
    }

    public int getIntervalNumber() {
        return intervalNumber;
    }

    public void setGpsAccuracy(final int gpsAccuracy) {
        this.gpsAccuracy = gpsAccuracy;
    }

    public void setIntervalNumber(final int intervalNumber) {
        this.intervalNumber = intervalNumber;
    }

    public float getAccelerometerX() {
        return accelerometerX;
    }

    public void setAccelerometerX(final float accelerometerX) {
        this.accelerometerX = accelerometerX;
    }

    public float getAccelerometerY() {
        return accelerometerY;
    }

    public void setAccelerometerY(final float accelerometerY) {
        this.accelerometerY = accelerometerY;
    }

    public float getAccelerometerZ() {
        return accelerometerZ;
    }

    public void setAccelerometerZ(final float accelerometerZ) {
        this.accelerometerZ = accelerometerZ;
    }

    public float getAccelerometerLinearX() {
        return accelerometerLinearX;
    }

    public void setAccelerometerLinearX(final float accelerometerLinearX) {
        this.accelerometerLinearX = accelerometerLinearX;
    }

    public float getAccelerometerLinearY() {
        return accelerometerLinearY;
    }

    public void setAccelerometerLinearY(final float accelerometerLinearY) {
        this.accelerometerLinearY = accelerometerLinearY;
    }

    public float getAccelerometerLinearZ() {
        return accelerometerLinearZ;
    }

    public void setAccelerometerLinearZ(final float accelerometerLinearZ) {
        this.accelerometerLinearZ = accelerometerLinearZ;
    }

    public float getAccelerometerGravityX() {
        return accelerometerGravityX;
    }

    public void setAccelerometerGravityX(final float accelerometerGravityX) {
        this.accelerometerGravityX = accelerometerGravityX;
    }

    public float getAccelerometerGravityY() {
        return accelerometerGravityY;
    }

    public void setAccelerometerGravityY(final float accelerometerGravityY) {
        this.accelerometerGravityY = accelerometerGravityY;
    }

    public float getAccelerometerGravityZ() {
        return accelerometerGravityZ;
    }

    public void setAccelerometerGravityZ(final float accelerometerGravityZ) {
        this.accelerometerGravityZ = accelerometerGravityZ;
    }

    public long getTime() {
        return time;
    }

    public void setTime(final long time) {
        this.time = time;
    }

    public float getAverageSpeed() {
        return averageSpeed;
    }

    public void setAverageSpeed(final float averageSpeed) {
        this.averageSpeed = averageSpeed;
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(final double distance) {
        this.distance = distance;
    }
    public double getRotationAngle() {
        return rotationAngle;
    }

    public void setRotationAngle(final double rotationAngle) {
        this.rotationAngle = rotationAngle;
    }

    public int getRoadId() {
        return roadId;
    }

    public void setRoadId(int roadId) {
        this.roadId = roadId;
    }

    public long getMeasurementId() {
        return measurementId;
    }

    public void setMeasurementId(long measurementId) {
        this.measurementId = measurementId;
    }
}
