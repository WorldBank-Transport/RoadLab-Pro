package com.softteco.roadlabpro.sqlite.model;

public class GeoTagModel extends BaseModel implements MeasurementItem {

    private double latitude;
    private double longitude;
    private double altitude;
    private double distance;
    private double speed;
    private long time;
    private boolean uploaded;
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

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public boolean isUploaded() {
        return uploaded;
    }

    public void setUploaded(final boolean uploaded) {
        this.uploaded = uploaded;
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
        return String.format("lat:%2.8f, lon: %2.8f, %2.0f km/h", speed, latitude, longitude);
    }

    @Override
    public float getIri() {
        return 0;
    }

    @Override
    public MeasurementItemType getType() {
        return MeasurementItemType.GEO_TAG;
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

    public double getSpeed() {
        return speed;
    }

    public void setSpeed(final double speed) {
        this.speed = speed;
    }

    @Override
    public long getTime() {
        return time;
    }

    public void setTime(final long time) {
        this.time = time;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        return getId() == ((GeoTagModel)o).getId();
    }

    @Override
    public int hashCode() {
        return (int) getId();
    }
}
