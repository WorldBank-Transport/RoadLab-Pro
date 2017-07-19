package com.softteco.roadlabpro.rest.dto;

import com.google.gson.annotations.SerializedName;

public class DriveFile extends FileItem {

    @SerializedName("kind")
    private String kind;
    @SerializedName("title")
    private String title;
    @SerializedName("mimeType")
    private String mimeType;
    @SerializedName("createdDate")
    private String createdDate;
    @SerializedName("modifiedDate")
    private String modifiedDate;
    @SerializedName("downloadUrl")
    private String downloadUrl;

    public String getKind() {
        return kind;
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

    public String getCreatedDate() {
        return createdDate;
    }

    public String getModifiedDate() {
        return modifiedDate;
    }

    public String getDownloadUrl() {
        return downloadUrl;
    }
}
