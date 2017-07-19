package com.softteco.roadlabpro.rest.dto;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class UpdateDriveFile {

    @SerializedName("parents")
    private List<DriveFolder> parents;
    @SerializedName("title")
    private String title;
    @SerializedName("mimeType")
    private String mimeType;

    public List<DriveFolder> getParents() {
        return parents;
    }

    public void setParents(List<DriveFolder> parents) {
        this.parents = parents;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }
}
