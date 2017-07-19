package com.softteco.roadlabpro.sqlite.model;

import android.text.TextUtils;

import com.softteco.roadlabpro.R;
import com.softteco.roadlabpro.RAApplication;

/**
 * Created by Vadim Alenin on 3/26/2015.
 */
public class IssuesModel extends BaseModel {

    private TypeIssues typeIssues;

    private double latitude;
    private double longitude;

    private long date;
    private long time;

    private boolean uploaded;
    private boolean own;

    private String notes;
    private String uniqueId;

    private String[] images = {"", "", ""};

    public TypeIssues getTypeIssues() {
        return typeIssues;
    }

    public void setTypeIssues(final TypeIssues issues) {
        this.typeIssues = issues;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(final double lat) {
        this.latitude = lat;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(final double lon) {
        this.longitude = lon;
    }

    public long getDate() {
        return date;
    }

    public void setDate(final long currDate) {
        this.date = currDate;
    }

    public long getTime() {
        return time;
    }

    public void setTime(final long currTime) {
        this.time = currTime;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(final String note) {
        this.notes = note;
    }

    public String[] getImages() {
        return images;
    }

    public void setImages(final String[] arrImage) {
        this.images = arrImage;
    }

    public boolean isUploaded() {
        return uploaded;
    }

    public void setUploaded(boolean uploaded) {
        this.uploaded = uploaded;
    }

    public boolean isOwn() {
        return own;
    }

    public void setOwn(boolean own) {
        this.own = own;
    }

    public String getUniqueId() {
        return uniqueId;
    }

    public void setUniqueId(String uniqueId) {
        this.uniqueId = uniqueId;
    }

    public enum TypeIssues {
        BROKEN(0), ACCIDENT(1), PIT_ON_THE_ROAD(2), LUKE(3), RAILS(4), PIT_IN_THE_YARD(5), OTHER(6);

        private int id;

        TypeIssues(int id) {
            this.id = id;
        }

        public int getId() {
            return id;
        }

        public static boolean isBelongs(final String value) {
            return (!TextUtils.isEmpty(value)
                    && (TextUtils.equals(value, BROKEN.name())
                    || TextUtils.equals(value, ACCIDENT.name())
                    || TextUtils.equals(value, PIT_ON_THE_ROAD.name())
                    || TextUtils.equals(value, LUKE.name())
                    || TextUtils.equals(value, RAILS.name())
                    || TextUtils.equals(value, PIT_IN_THE_YARD.name())
                    || TextUtils.equals(value, OTHER.name())));
        }

        @Override
        public String toString() {
            final String[] arr = RAApplication.getInstance().getApplicationContext().getResources().
                    getStringArray(R.array.fr_report_spinner_items_road_condition);
            return arr[id].toUpperCase();
        }
    }

    @Override
    public boolean equals(Object o) {
        final IssuesModel castedObj = (IssuesModel) o;
        return uniqueId.equals(castedObj.uniqueId);
    }
}
