package com.softteco.roadlabpro.sensors.software;

/**
 * Created by ppp on 13.04.2015.
 */
public class GravitySoftwareSensor extends SoftwareSensor {

    //data of previous calculated gravity vector
    private float[] gVector = new float[3];

    //data of previous calculated linear acceleration vector
    private float[] linearAcceleration = new float[3];

    // alpha is calculated as t / (t + dT)
    private float alpha = 0.9f;
    private float alpha1 = 0.1f;

    @Override
    public float[] calculate() {
        gVector[0] = (alpha1 * getValues()[0] + alpha * gVector[0]);
        gVector[1] = (alpha1 * getValues()[1] + alpha * gVector[1]);
        gVector[2] = (alpha1 * getValues()[2] + alpha * gVector[2]);

        linearAcceleration[0] = getValues()[0] - gVector[0];
        linearAcceleration[1] = getValues()[1] - gVector[1];
        linearAcceleration[2] = getValues()[2] - gVector[2];

        return gVector;
    }

    public float[] getLinearAcceleration() {
        return linearAcceleration;
    }
}
