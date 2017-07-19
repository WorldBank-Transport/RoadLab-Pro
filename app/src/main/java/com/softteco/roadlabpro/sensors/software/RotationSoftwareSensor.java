package com.softteco.roadlabpro.sensors.software;

/**
 * Created by ppp on 13.04.2015.
 */
public class RotationSoftwareSensor extends SoftwareSensor {

    private static final float NS2S = 1.0f / 1000000000.0f;
    private final float[] deltaRotationVector = new float[4];
    public static final float EPSILON = 0.000000001f;

    private long prevTimestamp;
    private long currTimestamp;

    public void setTimestamp(long timestamp) {
        this.currTimestamp = timestamp;
    }

    @Override
    public float[] calculate() {
        // This timestep's delta rotation to be multiplied by the current rotation
        // after computing it from the gyro sample data.
        if (prevTimestamp != 0) {
            final float dT = (currTimestamp - prevTimestamp) * NS2S;
            // Axis of the rotation sample, not normalized yet.
            float axisX = getValues()[0];
            float axisY = getValues()[1];
            float axisZ = getValues()[2];

            // Calculate the angular speed of the sample
            float omegaMagnitude = (float) Math.sqrt(axisX*axisX + axisY*axisY + axisZ*axisZ);

            // Normalize the rotation vector if it's big enough to get the axis
            if (omegaMagnitude > EPSILON) {
                axisX /= omegaMagnitude;
                axisY /= omegaMagnitude;
                axisZ /= omegaMagnitude;
            }
            // Integrate around this axis with the angular speed by the timestep
            // in order to get a delta rotation from this sample over the timestep
            // We will convert this axis-angle representation of the delta rotation
            // into a quaternion before turning it into the rotation matrix.
            float thetaOverTwo = omegaMagnitude * dT / 2.0f;
            float sinThetaOverTwo = (float) Math.sin(thetaOverTwo);
            float cosThetaOverTwo = (float) Math.cos(thetaOverTwo);
            deltaRotationVector[0] = sinThetaOverTwo * axisX;
            deltaRotationVector[1] = sinThetaOverTwo * axisY;
            deltaRotationVector[2] = sinThetaOverTwo * axisZ;
            deltaRotationVector[3] = cosThetaOverTwo;
        }
        prevTimestamp = currTimestamp;
        return deltaRotationVector;
    }
}
