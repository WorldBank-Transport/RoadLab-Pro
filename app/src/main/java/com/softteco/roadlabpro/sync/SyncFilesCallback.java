package com.softteco.roadlabpro.sync;

import java.util.List;

public interface SyncFilesCallback {
    void onUploadComplete(List<String> result);
    void onError(Exception e);
}
