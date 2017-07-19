package com.softteco.roadlabpro.rest.dto;

import com.google.gson.annotations.SerializedName;

public class AccountData {
    @SerializedName("kind")
    private String kind;
    @SerializedName("name")
    private String name;
    @SerializedName("user")
    private UserInfo user;

    public UserInfo getUser() {
        return user;
    }

    public String getName() {
        return name;
    }
}
