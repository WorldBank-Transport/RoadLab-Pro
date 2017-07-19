package com.softteco.roadlabpro.sqlite.model;

public interface MeasurementItem {
    long getId();
    long getTime();
    double getLatitude();
    double getLongitude();
    String getName();
    String getDescription();
    float getIri();
    MeasurementItemType getType();
}
