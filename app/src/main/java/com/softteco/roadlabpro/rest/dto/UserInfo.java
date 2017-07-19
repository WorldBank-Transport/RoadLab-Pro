package com.softteco.roadlabpro.rest.dto;

import com.google.gson.annotations.SerializedName;

public class UserInfo {

    @SerializedName("kind")
    private String kind;
    @SerializedName("displayName")
    private String displayName;
    @SerializedName("picture")
    private Picture picture;
    @SerializedName("isAuthenticatedUser")
    private boolean authenticatedUser;
    @SerializedName("permissionId")
    private String permissionId;
    @SerializedName("emailAddress")
    private String emailAddress;

    public String getKind() {
        return kind;
    }

    public String getDisplayName() {
        return displayName;
    }

    public Picture getPicture() {
        return picture;
    }

    public String getPermissionId() {
        return permissionId;
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public boolean isAuthenticatedUser() {
        return authenticatedUser;
    }
}