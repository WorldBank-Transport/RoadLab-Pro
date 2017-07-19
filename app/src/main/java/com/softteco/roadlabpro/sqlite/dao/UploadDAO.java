package com.softteco.roadlabpro.sqlite.dao;

import com.softteco.roadlabpro.sqlite.model.BaseModel;

import java.util.List;

/**
 * Created by Aleksey on 22.04.2015.
 */
public interface UploadDAO {
    List<? extends BaseModel> getDataForUpload(String query);

    List<? extends BaseModel> getDataForUpload();

    boolean updateUploadedDB(List<? extends BaseModel> items, boolean uploaded);

    boolean updatePendingDB(List<? extends BaseModel> items, boolean pending);

    boolean updateUploadedPendingDB(List<? extends BaseModel> items, boolean uploaded, boolean pending);

    boolean putDB(List<? extends BaseModel> items);
}
