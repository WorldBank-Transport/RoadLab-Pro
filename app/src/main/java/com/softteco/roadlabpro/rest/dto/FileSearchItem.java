package com.softteco.roadlabpro.rest.dto;

import com.google.gson.annotations.SerializedName;
import com.softteco.roadlabpro.sync.google.GoogleAPIHelper;

public class FileSearchItem extends FileItem {

    @SerializedName("kind")
    private String kind;

    @SerializedName("name")
    private String name;

    @SerializedName("mimeType")
    private String mimeType;

    public String getKind() {
        return kind;
    }

    public void setKind(String kind) {
        this.kind = kind;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public boolean isDirectory() {
        return mimeType != null
        && GoogleAPIHelper.GOOGLE_DRIVE_FOLDER_MIME.equals(mimeType);
    }
}
