package com.softteco.roadlabpro.sqlite.model;

/**
 * Created by Vadim Alenin on 4/6/2015.
 */
public class RecordModel extends BaseModel {

    public static final String TAG = RecordModel.class.getName();

    private String date;
    private String time;
    private int runNumber;
    private int roadId;
    private String roadSegmentId;
    private String vehicleId;
    private String speedDisplay;

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(final String deviceId) {
        this.deviceId = deviceId;
    }

    private String deviceId;

    public RecordModel() {
    }

    public int getRoadId() {
        return roadId;
    }

    public void setRoadId(int roadId) {
        this.roadId = roadId;
    }

    public int getRunNumber() {
        return runNumber;
    }

    public void setRunNumber(final int runNumber) {
        this.runNumber = runNumber;
    }

    public String getRoadSegmentId() {
        return roadSegmentId;
    }

    public void setRoadSegmentId(final String roadSegmentId) {
        this.roadSegmentId = roadSegmentId;
    }

    public String getVehicleId() {
        return vehicleId;
    }

    public void setVehicleId(final String vehicleId) {
        this.vehicleId = vehicleId;
    }

    public String getDate() {
        return date;
    }

    public void setDate(final String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(final String time) {
        this.time = time;
    }

    public String getSpeedDisplay() {
        return speedDisplay;
    }

    public void setSpeedDisplay(final String speedDisplay) {
        this.speedDisplay = speedDisplay;
    }

    public String getCommonInfo() {
        return
        "Record ID: " + getId() + " \n"
        + "Road id: " + roadId + " \n"
        + "Run number: " + runNumber + " \n"
        + "Road Segment ID: " + roadSegmentId  + " \n"
        + "Date: " + date + " \n"
        + "Time: " + time + " \n"
        + "Vehicle ID: " + vehicleId + " \n"
        + "Android ID: " + deviceId;
    }
}
