package com.softteco.roadlabpro.sync.google;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;

import com.dropbox.core.v2.files.FileMetadata;
import com.softteco.roadlabpro.rest.dto.DriveFile;
import com.softteco.roadlabpro.rest.dto.FileItem;
import com.softteco.roadlabpro.sqlite.model.FolderModel;
import com.softteco.roadlabpro.sqlite.model.MeasurementItemType;
import com.softteco.roadlabpro.sqlite.model.MeasurementModel;
import com.softteco.roadlabpro.sqlite.model.RoadModel;
import com.softteco.roadlabpro.sqlite.model.TagModel;
import com.softteco.roadlabpro.sync.DataSyncMode;
import com.softteco.roadlabpro.sync.SyncDataManager;
import com.softteco.roadlabpro.sync.SyncFilesCallback;
import com.softteco.roadlabpro.util.ActivityUtil;
import com.softteco.roadlabpro.util.PreferencesUtil;

import java.io.File;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.List;

public class GoogleDriveManager extends SyncDataManager {

    private final String TAG = GoogleDriveManager.class.getName();

    private GoogleAPIHelper apiHelper;

    private FileItem rootDir;
    private DriveFile projectDir;
    private DriveFile roadDir;
    private DriveFile measurementDir;
    private DriveFile tagsDir;
    private DriveFile tagMediaDir;

    public GoogleDriveManager(Context context) {
        super(context);
        apiHelper = new GoogleAPIHelper(context);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        apiHelper.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onPause() {
        apiHelper.onPause();
    }

    @Override
    public void onResume() {
        apiHelper.onResume();
    }

    @Override
    public boolean authentication() {
        return hasToken();
    }

    @Override
    public boolean hasToken() {
        String accessToken = PreferencesUtil.getInstance().getGoogleAccessToken();
        return !TextUtils.isEmpty(accessToken);
    }

    @Override
    public void login(Context context, OnSyncDataListener<Boolean> callback) {
        apiHelper.login(callback);
    }

    @Override
    public void logout() {
        apiHelper.logout();
    }

    @Override
    public void deactivate() {
        apiHelper.deactivate();
    }

    @Override
    public boolean isFolderAlreadyExists(String folder) {
        return false;
    }

    @Override
    protected void checkDataDirs(String dropboxPath, FolderModel folder, RoadModel road, DataSyncMode mode) {
        rootDir = apiHelper.createFolderWithSubFoldersSync(dropboxPath, true);
        switch (mode) {
            case ROAD:
                checkProjectDir(folder);
                break;
        }
    }

    @Override
    protected void checkValidToken() {
        apiHelper.checkToken();
    }

    @Override
    protected void uploadProject(FolderModel folder, String dropboxPath) {
        checkProjectDir(folder);
        super.uploadProject(folder, dropboxPath);
    }

    private void checkProjectDir(FolderModel folder) {
        String projectDirName = folder.getName();
        String parentId = null;
        if (rootDir != null) {
            parentId = rootDir.getId();
        }
        projectDir = apiHelper.createFolderSync(parentId, projectDirName);
    }

    @Override
    protected void uploadRoad(FolderModel folder, RoadModel road, String dropboxPath) {
        String roadDirName = road.getName();
        String parentId = null;
        if (projectDir != null) {
            parentId = projectDir.getId();
        }
        roadDir = apiHelper.createFolderSync(parentId, roadDirName);
        super.uploadRoad(folder, road, dropboxPath);
    }

    @Override
    protected void uploadMeasurement(String dropboxPath, final FolderModel folder, final RoadModel road, final MeasurementModel measurement) {
        String measurementDirName = getMeasurementDirName(measurement);
        String parentId = null;
        if (roadDir != null) {
            parentId = roadDir.getId();
        }
        measurementDir = apiHelper.createFolderSync(parentId, measurementDirName);
        super.uploadMeasurement(dropboxPath, folder, road, measurement);
    }

    @Override
    protected void uploadTags(final List<TagModel> tagModels, String dropboxPath,
        final FolderModel folder, final RoadModel road) {
        String tagsDirName = TAGS_PATH_PREFIX;
        String parentId = null;
        if (roadDir != null) {
            parentId = roadDir.getId();
        }
        tagsDir = apiHelper.createFolderSync(parentId, tagsDirName);
        super.uploadTags(tagModels, dropboxPath, folder, road);
    }

    @Override
    protected void uploadTagMediaFiles(final TagModel tag, String roadName, String projectName,
        String dataDir, String dropboxProjectPath) {
        String tagMediaDirName = getTagMediaDirName(tag, projectName, roadName);
        String parentId = null;
        if (tagsDir != null) {
            parentId = tagsDir.getId();
        }
        tagMediaDir = apiHelper.createFolderSync(parentId, tagMediaDirName);
        super.uploadTagMediaFiles(tag, roadName, projectName, dataDir, dropboxProjectPath);
    }

    @Override
    protected void runUploadFiles(String[] filePaths, String dropboxProjectPath, final SyncFilesCallback callback) {
        Exception e = null;
        final List<String> uploadedFiles = new ArrayList<>();
        MeasurementItemType type = null;
        File file = null;
        Object data = null;
        for (String filePath : filePaths) {
            if (TextUtils.isEmpty(filePath)) {
                continue;
            }
            file = new File(filePath);
            type = getFileType(file.getName());
            data = runUploadFiles(filePath, dropboxProjectPath, type, null);
            if (data != null) {
                if (data instanceof Exception) {
                    e = (Exception) data;
                } else if(data instanceof DriveFile) {
                    DriveFile fileData = (DriveFile) data;
                    if (fileData.getId() != null) {
                        uploadedFiles.add(fileData.getId());
                    }
                }
            }
        }
        final Exception finalE = e;
        ActivityUtil.runOnMainThread(new Runnable() {
            @Override
            public void run() {
                if (callback != null) {
                    if (finalE != null) {
                        callback.onError(finalE);
                    } else {
                        callback.onUploadComplete(uploadedFiles);
                    }
                }
            }
        });
    }

    @Override
    protected Object runUploadFiles(String filePath, String dropboxProjectPath,
        MeasurementItemType type, final SyncFilesCallback callback) {

        String parentId = getParentDirId(type);
        final Object result = apiHelper.createFileSync(parentId, filePath);
        DriveFile localFileData = null;
        if (result == null || result instanceof Exception) {
            ActivityUtil.runOnMainThread(new Runnable() {
                @Override
                public void run() {
                    if (callback != null) {
                        Exception e = null;
                        if (result != null) {
                            e = (Exception) result;
                        }
                        callback.onError(e);
                    }
                }
            });
            return result;
        }
        if (result instanceof DriveFile) {
            localFileData = (DriveFile) result;
        }
        if (localFileData != null) {
            String message = localFileData.getTitle()
            + " url " + localFileData.getDownloadUrl()
            + " modified " + localFileData.getModifiedDate();
            Log.d(TAG, message);
        }
        final DriveFile fileData = localFileData;
        ActivityUtil.runOnMainThread(new Runnable() {
            @Override
            public void run() {
                if (callback != null) {
                    List<String> data = new ArrayList<>();
                    if (fileData != null && fileData.getId() != null) {
                        data.add(fileData.getId());
                    }
                    callback.onUploadComplete(data);
                }
            }
        });
        return result;
    }

    private MeasurementItemType getFileType(String fileName) {
        if (TextUtils.isEmpty(fileName)) {
            return null;
        }
        for (MeasurementItemType type : MeasurementItemType.values()) {
            if (fileName.contains(type.name())) {
                return type;
            }
        }
        return null;
    }

    private String getParentDirId(MeasurementItemType type) {
        if (type == null && measurementDir != null) {
            return measurementDir.getId();
        }
        if (type == null) {
            return null;
        }
        switch (type) {
            case INTERVAl:
            case BUMP:
            case GEO_TAG:
                if (measurementDir != null) {
                    return measurementDir.getId();
                }
                break;
            case TAG:
                if (tagsDir != null) {
                   return tagsDir.getId();
                }
                break;
            case TAG_MEDIA:
                if (tagMediaDir != null) {
                    return tagMediaDir.getId();
                }
                break;
            case SUMMARY:
                if (projectDir != null) {
                    return projectDir.getId();
                }
                break;
        }
        return null;
    }
}
