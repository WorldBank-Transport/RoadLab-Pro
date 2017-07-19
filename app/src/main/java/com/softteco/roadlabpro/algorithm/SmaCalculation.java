package com.softteco.roadlabpro.algorithm;

import android.content.Context;

import com.softteco.roadlabpro.util.PreferencesUtil;

/**
 * Created by ppp on 15.04.2015.
 */
public class SmaCalculation {

    public static final long CHECK_SMA_INTERVAL = 100; // value in milliseconds of SMA calculation period

    private long smaLastTime = 0;
    private long smaCurrentTime = 0;
    private float smaAv = 0;
    private float lastSmaAv;
    private int smaCount = 0;

    private long smaInterval = CHECK_SMA_INTERVAL;
    private float smaMinValue = 0.1f * RoadConditionDetection.G;

    public SmaCalculation(Context context) {
        smaMinValue = (PreferencesUtil.getInstance(context).
                getSettingsValue(PreferencesUtil.SETTINGS.FILTER_ACCELERATION) / 100f) * RoadConditionDetection.G;
    }

    /**
     * Manual set SMA calculation interval (by default value is 100ms)
     * @param context - context instance
     * @param checkSmaInterval - value of new interval
     */
    public void setCheckSmaInterval(Context context, long checkSmaInterval) {
        smaInterval = checkSmaInterval;
    }

    /**
     * Runs calculation of SMA value
     * @param forceCalc - force SMA calculation, timers will be ignored
     * @param time - time of accelerometer data measurement
     * @param value - value of linear acceleration
     * @return true if new value of SMA calculated
     */
    public boolean calculate(boolean forceCalc, long time, float value) {
        float absValue = Math.abs(value);
        if (!forceCalc && absValue >= smaMinValue) {
            smaAv += absValue;
            smaCount++;
        }
        return recalculateSMA(time, forceCalc);
    }

    /**
     * Calculates next SMA value
     * @param time - time of accelerometer data measurement
     * @param forceCalc - force SMA calculation, timers will be ignored
     * @return true if new value of SMA calculated
     */
    private boolean recalculateSMA(long time, boolean forceCalc) {
        smaCurrentTime = time;
        if (forceCalc || smaCurrentTime - smaLastTime >= smaInterval) {
            smaLastTime = smaCurrentTime;
            if (smaCount != 0) {
                lastSmaAv = (1.0f / (float) smaCount) * smaAv;
            }
            smaCount = 0;
            smaAv = 0;
            return true;
        }
        return false;
    }

    /**
     * Returns current value of SMA
     * @return current SMA value
     */
    public float getSMA() {
        return lastSmaAv;
    }
}
