package com.softteco.roadlabpro.sqlite.model;

import java.util.Date;

/**
 * Created by Aleksey on 17.04.2015.
 */
public class FolderModel extends BaseModel implements Cloneable {

    private long time;
    private long date;
    private String name;
    private boolean uploaded;
    private boolean pending;
    private boolean defaultProject;
    private long roads;
    private double averageIRI;
    private double averageSpeed;
    private double overallDistance;
    private double pathDistance;

    public FolderModel(){}

    public FolderModel(String name){
        this.name = name;
        this.time = this.date = new Date().getTime();
    }

    public double getPathDistance() {
        return pathDistance;
    }

    public void setPathDistance(double pathDistance) {
        this.pathDistance = pathDistance;
    }

    public double getAverageSpeed() {
        return averageSpeed;
    }

    public void setAverageSpeed(double averageSpeed) {
        this.averageSpeed = averageSpeed;
    }

    public double getAverageIRI() {
        return averageIRI;
    }

    public void setAverageIRI(double averageIRI) {
        this.averageIRI = averageIRI;
    }

    public boolean isDefaultProject() {
        return defaultProject;
    }

    public void setDefaultProject(boolean defaultProject) {
        this.defaultProject = defaultProject;
    }

    public double getOverallDistance() {
        return overallDistance;
    }

    public void setOverallDistance(double overallDistance) {
        this.overallDistance = overallDistance;
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getRoads() {
        return roads;
    }

    public void setRoads(long roads) {
        this.roads = roads;
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

    //    public JSONObject getFolderAsJSONObject() {
//        JSONObject object = new JSONObject();
//        try {
//            object.put("coords", StringUtil.getStringFromCoords(new double[]{getLatitude(), getLongitude(), getAltitude()}));
//            object.put("acc", StringUtil.getStringFromCoords(new double[]{getAccelerationX(), getAccelerationY(), getAccelerationZ()}));
//            object.put("time", DateUtil.format(new Date(getTime()), DateUtil.Format.SERVER_DATE));
//            object.put("speed", getSpeed());
//            object.put("is_fixed", isFixed());
//        } catch (JSONException e) {
//
//        }
//        return object;
//    }

    @Override
    public FolderModel clone() {
        try {
            return (FolderModel) super.clone();
        } catch (CloneNotSupportedException e) {
            return null;
        }
    }
}
