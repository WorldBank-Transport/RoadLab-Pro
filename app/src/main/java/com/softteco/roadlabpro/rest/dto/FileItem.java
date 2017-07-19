package com.softteco.roadlabpro.rest.dto;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class FileItem implements Serializable {

    @SerializedName("id")
    private String id;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
