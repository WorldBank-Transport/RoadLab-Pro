package com.softteco.roadlabpro.sqlite.model;

import android.content.Context;

import com.softteco.roadlabpro.R;
import com.softteco.roadlabpro.util.DateUtil;
import com.softteco.roadlabpro.util.StringUtil;
import com.softteco.roadlabpro.util.TimeUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Aleksey on 17.04.2015.
 */
public class BumpModel extends BaseModel implements MeasurementItem {

    private double latitude;
    private double longitude;
    private double altitude;
    private float accelerationX;
    private float accelerationY;
    private float accelerationZ;
    private float speed;
    private long time;
    private boolean uploaded;
    private boolean pending;
    private long recordId;
    private boolean fixed;
    private long folderId;
    private long roadId;
    private long measurementId;

    public long getFolderId() {
        return folderId;
    }

    public void setFolderId(long folderId) {
        this.folderId = folderId;
    }

    public long getRoadId() {
        return roadId;
    }

    public void setRoadId(long roadId) {
        this.roadId = roadId;
    }

    public void setMeasurementId(long measurementId) {
        this.measurementId = measurementId;
    }

    public long getMeasurementId() {
        return measurementId;
    }

    public boolean isPending() {
        return pending;
    }

    public void setPending(boolean pending) {
        this.pending = pending;
    }

    public boolean isUploaded() {
        return uploaded;
    }

    public void setUploaded(final boolean uploaded) {
        this.uploaded = uploaded;
    }

    public long getRecordId() {
        return recordId;
    }

    public void setRecordId(final long recordId) {
        this.recordId = recordId;
    }

    public void setLatitude(final double latitude) {
        this.latitude = latitude;
    }

    @Override
    public double getLatitude() {
        return latitude;
    }

    @Override
    public double getLongitude() {
        return longitude;
    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    public String getDescription() {
        return String.format("%2.0f km/h", speed);
    }

    @Override
    public float getIri() {
        return 0;
    }

    @Override
    public MeasurementItemType getType() {
        return MeasurementItemType.BUMP;
    }

    public void setLongitude(final double longitude) {
        this.longitude = longitude;
    }

    public double getAltitude() {
        return altitude;
    }

    public void setAltitude(final double altitude) {
        this.altitude = altitude;
    }

    public float getAccelerationX() {
        return accelerationX;
    }

    public void setAccelerationX(final float accelerationX) {
        this.accelerationX = accelerationX;
    }

    public float getAccelerationY() {
        return accelerationY;
    }

    public void setAccelerationY(final float accelerationY) {
        this.accelerationY = accelerationY;
    }

    public float getAccelerationZ() {
        return accelerationZ;
    }

    public void setAccelerationZ(final float accelerationZ) {
        this.accelerationZ = accelerationZ;
    }

    public float getSpeed() {
        return speed;
    }

    public void setSpeed(final float speed) {
        this.speed = speed;
    }

    @Override
    public long getTime() {
        return time;
    }

    public void setTime(final long time) {
        this.time = time;
    }

    public boolean isFixed() {
        return fixed;
    }

    public void setFixed(boolean fixed) {
        this.fixed = fixed;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        return getId() == ((BumpModel)o).getId();
    }

    @Override
    public int hashCode() {
        return (int) getId();
    }

    public JSONObject getBumpAsJSONObject() {
        JSONObject object = new JSONObject();
        try {
            object.put("coords", StringUtil.getStringFromCoords(new double[]{getLatitude(), getLongitude(), getAltitude()}));
            object.put("acc", StringUtil.getStringFromCoords(new double[]{getAccelerationX(), getAccelerationY(), getAccelerationZ()}));
            object.put("time", DateUtil.format(new Date(getTime()), DateUtil.Format.SERVER_DATE));
            object.put("speed", getSpeed());
            object.put("is_fixed", isFixed());
        } catch (JSONException e) {

        }
        return object;
    }
}
