package com.softteco.roadlabpro.sqlite.model;

import java.util.Date;

public class RoadModel extends BaseModel implements Cloneable {

    private long time;
    private long date;
    private long folderId;
    private String name;
    private long experiments;
    private double overallDistance;
    private double pathDistance;
    private double averageIRI;
    private double averageSpeed;
    private float[] issuesStats;
    private boolean uploaded;
    private boolean defaultRoad;
    private boolean pending;

    public RoadModel(){}

    public RoadModel(String name, long folderId){
        this.folderId = folderId;
        this.name = name;
        this.time = this.date = new Date().getTime();
    }

    public double getPathDistance() {
        return pathDistance;
    }

    public void setPathDistance(double pathDistance) {
        this.pathDistance = pathDistance;
    }

    public void setIssuesStats(float[] stats) {
        this.issuesStats = stats;
    }

    public float[] getIssuesStats() {
        return issuesStats;
    }

    public float getStatBadIssues() {
        if (issuesStats != null && issuesStats.length >= 1) {
            return issuesStats[0];
        }
        return 0;
    }

    public float getStatNormalIssues() {
        if (issuesStats != null && issuesStats.length >= 2) {
            return issuesStats[1];
        }
        return 0;
    }

    public float getStatGoodIssues() {
        if (issuesStats != null && issuesStats.length >= 3) {
            return issuesStats[2];
        }
        return 0;
    }

    public float getStatPerfectIssues() {
        if (issuesStats != null && issuesStats.length >= 2) {
            return issuesStats[3];
        }
        return 0;
    }

    public double getAverageIRI() {
        return averageIRI;
    }

    public void setAverageIRI(double averageIRI) {
        this.averageIRI = averageIRI;
    }

    public double getAverageSpeed() {
        return averageSpeed;
    }

    public void setAverageSpeed(double averageSpeed) {
        this.averageSpeed = averageSpeed;
    }

    public boolean isDefaultRoad() {
        return defaultRoad;
    }

    public void setDefaultRoad(boolean defaultRoad) {
        this.defaultRoad = defaultRoad;
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

    public long getFolderId() {
        return folderId;
    }

    public void setFolderId(long folderId) {
        this.folderId = folderId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getExperiments() {
        return experiments;
    }

    public void setExperiments(long experiments) {
        this.experiments = experiments;
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

    @Override
    public RoadModel clone() {
        try {
            return (RoadModel) super.clone();
        } catch (CloneNotSupportedException e) {
            return null;
        }
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
}
