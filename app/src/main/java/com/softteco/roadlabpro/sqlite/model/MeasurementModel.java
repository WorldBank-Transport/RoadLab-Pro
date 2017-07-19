package com.softteco.roadlabpro.sqlite.model;

import java.util.Date;

public class MeasurementModel extends BaseModel {

    private long time;
    private long date;
    private long roadId;
    private long intervalsNumber;
    private double overallDistance;
    private double pathDistance;
    private double avgIRI;
    private boolean uploaded;
    private boolean pending;

    public MeasurementModel(){}

    public MeasurementModel(long roadId){
        this.roadId = roadId;
        this.time = this.date = new Date().getTime();
    }

    public double getPathDistance() {
        return pathDistance;
    }

    public void setPathDistance(double pathDistance) {
        this.pathDistance = pathDistance;
    }

    public double getAvgIRI() {
        return avgIRI;
    }

    public void setAvgIRI(double avgIRI) {
        this.avgIRI = avgIRI;
    }

    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public long getRoadId() {
        return roadId;
    }

    public void setRoadId(long roadId) {
        this.roadId = roadId;
    }

    public double getOverallDistance() {
        return overallDistance;
    }

    public void setOverallDistance(double overallDistance) {
        this.overallDistance = overallDistance;
    }

    public long getIntervalsNumber() {
        return intervalsNumber;
    }

    public void setIntervalsNumber(long intervalsNumber) {
        this.intervalsNumber = intervalsNumber;
    }

    public boolean isUploaded() {
        return uploaded;
    }

    public void setUploaded(boolean uploaded) {
        this.uploaded = uploaded;
    }

    public boolean isPending() {
        return pending;
    }

    public void setPending(boolean pending) {
        this.pending = pending;
    }

}
