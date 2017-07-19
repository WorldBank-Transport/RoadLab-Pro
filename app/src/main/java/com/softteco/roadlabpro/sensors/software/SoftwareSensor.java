package com.softteco.roadlabpro.sensors.software;

/**
 * Created by ppp on 13.04.2015.
 */
public class SoftwareSensor {

    private float[] values;

    public void setValues(float[] values) {
        this.values = values;
    }

    public float[] getValues() {
        return values;
    }

    public float[] calculate() {
        return null;
    }
}
