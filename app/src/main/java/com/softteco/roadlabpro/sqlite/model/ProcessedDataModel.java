package com.softteco.roadlabpro.sqlite.model;

import com.softteco.roadlabpro.algorithm.RoadQuality;
import com.softteco.roadlabpro.util.DateUtil;
import com.softteco.roadlabpro.util.StringUtil;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

/**
 * Created by ppp on 14.04.2015.
 */
public class ProcessedDataModel extends BaseModel implements MeasurementItem {

    private long recordId;
    private long time;
    private float speed;
    private int bumps;
    private long session;
    private float av;
    private RoadQuality category;
    private float stdDeviation;
    private double[] coordsStart;
    private double[] coordsEnd;
    private boolean uploaded;
    private boolean pending;
    private boolean fixed;
    private float iri;
    private double distance;
    private int itemsCount;
    private long folderId;
    private long roadId;
    private long measurementId;
    private int suspension;

    public int getSuspension() {
        return suspension;
    }

    public void setSuspension(int suspension) {
        this.suspension = suspension;
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public int getItemsCount() {
        return itemsCount;
    }

    public void setItemsCount(int itemsCount) {
        this.itemsCount = itemsCount;
    }

    public long getSession() {
        return session;
    }

    public void setSession(long session) {
        this.session = session;
    }

    public boolean isUploaded() {
        return uploaded;
    }

    public void setUploaded(final boolean uploaded) {
        this.uploaded = uploaded;
    }

    public void setRecordId(final long recordId) {
        this.recordId = recordId;
    }

    public long getRecordId() {
        return recordId;
    }

    public double[] getCoordsStart() {
        return coordsStart;
    }

    public double[] getCoordsEnd() {
        return coordsEnd;
    }

    public boolean isPending() {
        return pending;
    }

    public void setPending(boolean pending) {
        this.pending = pending;
    }

    public void setCoordsStart(final double latitude, final double longitude) {
        setCoordsStart(latitude, longitude, 0);
    }

    public void setCoordsEnd(final double latitude, final double longitude) {
        setCoordsEnd(latitude, longitude, 0);
    }

    public void setCoordsStart(final double latitude, final double longitude, final double altitude) {
        if (coordsStart == null) {
            coordsStart = new double[3];
        }
        this.coordsStart[0] = latitude;
        this.coordsStart[1] = longitude;
        this.coordsStart[2] = altitude;
    }

    public void setCoordsEnd(final double latitude, final double longitude, final double altitude) {
        if (coordsEnd == null) {
            coordsEnd = new double[3];
        }
        this.coordsEnd[0] = latitude;
        this.coordsEnd[1] = longitude;
        this.coordsEnd[2] = altitude;
    }

    public float getVerticalAcceleration() {
        return av;
    }

    public void setVerticalAccleration(final float acceleration) {
        this.av = acceleration;
    }

    public float getStdDeviation() {
        return stdDeviation;
    }

    public void setStdDeviation(final float stdDeviation) {
        this.stdDeviation = stdDeviation;
    }

    @Override
    public long getTime() {
        return time;
    }

    public float getSpeed() {
        return speed;
    }

    public int getBumps() {
        return bumps;
    }

    public RoadQuality getCategory() {
        return category;
    }

    public void setTime(final long time) {
        this.time = time;
    }

    public void setSpeed(final float speed) {
        this.speed = speed;
    }

    public void setCategory(final RoadQuality category) {
        this.category = category;
    }

    public void setBumps(final int bumps) {
        this.bumps = bumps;
    }

    public float getIri() {
        return iri;
    }

    public void setIri(float iri) {
        this.iri = iri;
    }

    public void setFixed(boolean fixed) {
        this.fixed = fixed;
    }

    public boolean isFixed() {
        return fixed;
    }

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

    public long getMeasurementId() {
        return measurementId;
    }

    public void setMeasurementId(long measurementId) {
        this.measurementId = measurementId;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        return getId() == ((ProcessedDataModel)o).getId();
    }

    @Override
    public int hashCode() {
        return (int) getId();
    }

    public JSONObject getRoadIntervalAsJSONObject() {
        JSONObject object = new JSONObject();
        try {
            object.put("coords", StringUtil.getStringFromCoords(getCoordsStart()));
            object.put("coords_end", StringUtil.getStringFromCoords(getCoordsEnd()));
            object.put("stddev", getStdDeviation());
            object.put("time", DateUtil.format(new Date(getTime()), DateUtil.Format.SERVER_DATE));
            object.put("speed", getSpeed());
            object.put("category", getCategory().getId());
            object.put("is_fixed", isFixed());
            object.put("iri",getIri());
            object.put("distance",getDistance());
        } catch (JSONException e) {

        }
        return object;
    }

    @Override
    public double getLatitude() {
        if (getCoordsStart() != null && getCoordsStart().length >= 2) {
            return coordsStart[0];
        }
        return 0;
    }

    @Override
    public double getLongitude() {
        if (getCoordsStart() != null && getCoordsStart().length >= 2) {
            return coordsStart[1];
        }
        return 0;
    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    public String getDescription() {
        return String.format("%2.0f m - %2.0f km/h", distance, speed);
    }

    @Override
    public MeasurementItemType getType() {
        return MeasurementItemType.INTERVAl;
    }
}
