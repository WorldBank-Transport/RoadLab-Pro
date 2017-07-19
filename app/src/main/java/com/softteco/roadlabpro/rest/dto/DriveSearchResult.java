package com.softteco.roadlabpro.rest.dto;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class DriveSearchResult extends FileItem {

    @SerializedName("files")
    private List<FileSearchItem> files;

    public List<FileSearchItem> getFiles() {
        return files;
    }

    public FileSearchItem getFirst() {
        if (files != null && files.size() > 0) {
            return files.get(0);
        }
        return null;
    }
}
