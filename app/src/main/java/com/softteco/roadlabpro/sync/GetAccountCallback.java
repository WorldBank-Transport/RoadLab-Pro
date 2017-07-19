package com.softteco.roadlabpro.sync;

import com.softteco.roadlabpro.users.User;

public interface GetAccountCallback {
    void onComplete(SyncDataType type, User result);
    void onError(Exception e);
}