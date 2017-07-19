package com.softteco.roadlabpro.sensors.data;

import com.softteco.roadlabpro.util.ConvertQuantityUtil;

public class AccelerometerDataEntry {

    private float accelerometerX;
    private float accelerometerY;
    private float accelerometerZ;

    private float accelerometerLinearX;
    private float accelerometerLinearY;
    private float accelerometerLinearZ;

    private float accelerometerGravityX;
    private float accelerometerGravityY;
    private float accelerometerGravityZ;

    private float speedBefore;
    private float speedAfter;
    private float distance;
    private float acceleration;
    private double rotationAngle = 0;

    private boolean deviceIsNotFixed = false;

    public AccelerometerDataEntry() {
        this(0, 0, 0);
    }

    public AccelerometerDataEntry(final float accelerometerX, final float accelerometerY, final float accelerometerZ) {
        this.accelerometerZ = accelerometerZ;
        this.accelerometerY = accelerometerY;
        this.accelerometerX = accelerometerX;
    }

    public void setAccelerometerY(float accelerometerY) {
        this.accelerometerY = accelerometerY;
    }

    public void setAccelerometerZ(float accelerometerZ) {
        this.accelerometerZ = accelerometerZ;
    }

    public void setAccelerometerX(float accelerometerX) {

        this.accelerometerX = accelerometerX;
    }

    public float getAccelerometerX() {
        return accelerometerX;
    }

    public float getAccelerometerZ() {
        return accelerometerZ;
    }

    public float getAccelerometerY() {

        return accelerometerY;
    }

    public float getAccelerometerLinearX() {
        return accelerometerLinearX;
    }

    public void setAccelerometerLinearX(float accelerometerLinearX) {
        this.accelerometerLinearX = accelerometerLinearX;
    }

    public float getAccelerometerLinearY() {
        return accelerometerLinearY;
    }

    public void setAccelerometerLinearY(float accelerometerLinearY) {
        this.accelerometerLinearY = accelerometerLinearY;
    }

    public float getAccelerometerLinearZ() {
        return accelerometerLinearZ;
    }

    public void setAccelerometerLinearZ(float accelerometerLinearZ) {
        this.accelerometerLinearZ = accelerometerLinearZ;
    }

    public float getAccelerometerGravityX() {
        return accelerometerGravityX;
    }

    public void setAccelerometerGravityX(float accelerometerGravityX) {
        this.accelerometerGravityX = accelerometerGravityX;
    }

    public float getAccelerometerGravityY() {
        return accelerometerGravityY;
    }

    public void setAccelerometerGravityY(float accelerometerGravityY) {
        this.accelerometerGravityY = accelerometerGravityY;
    }

    public float getAccelerometerGravityZ() {
        return accelerometerGravityZ;
    }

    public void setAccelerometerGravityZ(float accelerometerGravityZ) {
        this.accelerometerGravityZ = accelerometerGravityZ;
    }

    public void calc(long interval){
        acceleration = (float) Math.sqrt(accelerometerLinearX * accelerometerLinearX
                + accelerometerLinearY * accelerometerLinearY
                + accelerometerLinearZ * accelerometerLinearZ);
        float t = ((float) interval / 1000f);
        speedAfter = speedBefore + (acceleration * t);
        distance = speedBefore*t + acceleration*t*t/2;

    }

    public float getSpeedAfter() {
        return ConvertQuantityUtil.metersPerSecondsToKillometrPerHours(speedAfter);
    }

    public float getDistance() {
        return distance;
    }

    public double getRotationAngle() {
        return rotationAngle;
    }

    public void setRotationAngle(double rotationAngle) {
        this.rotationAngle = rotationAngle;
    }

    public boolean isDeviceNotFixed() {
        return deviceIsNotFixed;
    }

    public void setDeviceNotFixed(boolean deviceIsNotFixed) {
        this.deviceIsNotFixed = deviceIsNotFixed;
    }

    @Override
    public String toString() {
        return "AccelerometerDataEntry(" + accelerometerX + ", " + accelerometerY + ", " + accelerometerZ + ")";
    }
}