package com.softteco.roadlabpro.sensors.data;

/**
 * Created by ppp on 13.04.2015.
 */
public class SensorData {

    public int type;
    public float[] values;

    public SensorData(int type, float[] values) {
        this.type = type;
        this.values = values;
    }
}
