package com.softteco.roadlabpro.rest.dto;

import com.google.gson.annotations.SerializedName;

public class Picture {
    @SerializedName("url")
    private String url;

    public String getUrl() {
        return url;
    }
}
