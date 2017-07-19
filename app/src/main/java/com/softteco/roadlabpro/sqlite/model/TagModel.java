package com.softteco.roadlabpro.sqlite.model;

import android.content.Context;
import android.text.TextUtils;

import com.softteco.roadlabpro.R;
import com.softteco.roadlabpro.RAApplication;
import com.softteco.roadlabpro.util.TimeUtil;

import java.text.SimpleDateFormat;
import java.util.Date;

public class TagModel extends BaseModel implements MeasurementItem {

    private double latitude;
    private double longitude;
    private double altitude;
    private String name;
    private String description;
    private float speed;
    private long time;
    private long date;
    private boolean uploaded;
    private boolean pending;
    private long folderId;
    private long roadId;
    private long measurementId;
    private String[] images = {"", "", ""};
    private String audioFile;
    private RoadCondition roadCondition;
    private String notes;
    private boolean single;
    private float iri;

    public String getAudioFile() {
        return audioFile;
    }

    public void setAudioFile(String audioFile) {
        this.audioFile = audioFile;
    }

    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
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

    public void setMeasurementId(long measurementId) {
        this.measurementId = measurementId;
    }

    public long getMeasurementId() {
        return measurementId;
    }

    @Override
    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    @Override
    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getAltitude() {
        return altitude;
    }

    public void setAltitude(double altitude) {
        this.altitude = altitude;
    }

    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getDescription() {
        if (TextUtils.isEmpty(description)) {
            return getNotes();
        }
        return description;
    }

    @Override
    public float getIri() {
        return iri;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setSpeed(float speed) {
        this.speed = speed;
    }

    public float getSpeed() {
        return speed;
    }

    public void setTime(long time) {
        this.time = time;
    }

    @Override
    public long getTime() {
        return time;
    }

    public boolean isUploaded() {
        return uploaded;
    }

    public boolean isPending() {
        return pending;
    }

    public void setPending(boolean pending) {
        this.pending = pending;
    }

    public void setUploaded(boolean uploaded) {
        this.uploaded = uploaded;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String[] getImages() {
        return images;
    }

    public void setImages(String[] images) {
        this.images = images;
    }

    public boolean isSingle() {
        return single;
    }

    public void setSingle(boolean single) {
        this.single = single;
    }

    public void setIri(float iri) {
        this.iri = iri;
    }

    @Override
    public MeasurementItemType getType() {
        return MeasurementItemType.TAG;
    }

    public RoadCondition getRoadCondition() {
        return roadCondition;
    }

    public void setRoadCondition(RoadCondition roadCondition) {
        this.roadCondition = roadCondition;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        return getId() == ((TagModel)o).getId();
    }

    @Override
    public int hashCode() {
        return (int) getId();
    }

    public enum RoadCondition {
        GOOD(0), FAIR(1), POOR(2), BAD(3), NONE(4);
        private int id;

        RoadCondition(int id) {
            this.id = id;
        }

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public static RoadCondition getRoadQualityById(int id) {
            for (RoadCondition r : values()) {
                if (r.getId() == id) {
                    return r;
                }
            }
            return RoadCondition.NONE;
        }

        public static RoadCondition getRoadConditionByIri(double iri) {
            if (iri >= 0) {
                if (iri <= 2) {
                    return GOOD;
                } else if (iri <= 4) {
                    return FAIR;
                } else if (iri <= 6) {
                    return POOR;
                } else {
                    return BAD;
                }
            } else {
                return NONE;
            }
        }

        public static String getName(RoadCondition condition) {
            String[] conditionStrs = RAApplication.getInstance().getResources().
            getStringArray(R.array.road_condition_values);
            if (conditionStrs != null) {
                switch (condition) {
                    case GOOD:
                        if (conditionStrs.length >= 1) {
                            return conditionStrs[0];
                        }
                        break;
                    case FAIR:
                        if (conditionStrs.length >= 2) {
                            return conditionStrs[1];
                        }
                        break;
                    case POOR:
                        if (conditionStrs.length >= 3) {
                            return conditionStrs[2];
                        }
                        break;
                    case BAD:
                        if (conditionStrs.length >= 4) {
                            return conditionStrs[3];
                        }
                        break;
                    case NONE:
                        if (conditionStrs.length >= 5) {
                            return conditionStrs[4];
                        }
                        break;
                }
            }
            return "";
        }

        @Override
        public String toString() {
            return this.name();
        }
    }

    public int getRoadConditionColor(Context context) {
        int color = context.getResources().getColor(R.color.chart_none_roads_color);
        if (roadCondition != null) {
            switch (roadCondition) {
                case GOOD:
                    color = context.getResources().getColor(R.color.chart_perfect_roads_color);
                    break;
                case FAIR:
                    color = context.getResources().getColor(R.color.chart_good_roads_color);
                    break;
                case POOR:
                    color = context.getResources().getColor(R.color.chart_normal_roads_color);
                    break;
                case BAD:
                    color = context.getResources().getColor(R.color.chart_bad_roads_color);
                    break;
            }
        }
        return color;
    }

    public String getName(Context context) {
        long dateMillis = getDate();
        Date date = new Date(dateMillis);
        String dateStr = new SimpleDateFormat(TimeUtil.DATE_SIMPLE_FILENAME_FORMAT).format(date);
        return context.getString(R.string.fr_settings_db_template_for_export_tag, String.valueOf(getId()), dateStr);
    }
}
