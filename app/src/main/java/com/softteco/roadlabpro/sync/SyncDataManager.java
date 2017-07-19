package com.softteco.roadlabpro.sync;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.dropbox.core.v2.files.FileMetadata;
import com.softteco.roadlabpro.R;
import com.softteco.roadlabpro.sqlite.MeasurementsDataHelper;
import com.softteco.roadlabpro.sqlite.dao.FolderDAO;
import com.softteco.roadlabpro.sqlite.dao.MeasurementDAO;
import com.softteco.roadlabpro.sqlite.dao.RoadDAO;
import com.softteco.roadlabpro.sqlite.dao.TagDAO;
import com.softteco.roadlabpro.sqlite.model.FolderModel;
import com.softteco.roadlabpro.sqlite.model.MeasurementItemType;
import com.softteco.roadlabpro.sqlite.model.MeasurementModel;
import com.softteco.roadlabpro.sqlite.model.RoadModel;
import com.softteco.roadlabpro.sqlite.model.TagModel;
import com.softteco.roadlabpro.util.Constants;
import com.softteco.roadlabpro.util.DeviceUtil;
import com.softteco.roadlabpro.util.FileUtils;
import com.softteco.roadlabpro.util.KMLHelper;
import com.softteco.roadlabpro.util.PreferencesUtil;
import com.softteco.roadlabpro.util.TimeUtil;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class SyncDataManager {

    private static final String TAG = SyncDataManager.class.getName();
    protected static final String TAGS_PATH_PREFIX = "tags";
    protected static final String DIR_SLASH = "/";
    protected static final String[] TAGS_POSTFIXES = new String[] {"_1.jpg", "_2.jpg", "_3.jpg", ".mp3"};

    protected FolderDAO folderDAO;
    protected RoadDAO roadDAO;
    protected MeasurementDAO measurementDAO;
    protected TagDAO tagDAO;
    protected int uploadedFileCounter;
    protected int startUploadFileCounter;

    protected Context context;
    private ProgressDialog dialog;

    public SyncDataManager(Context context) {
        this.context = context;
    }

    public interface OnSyncDataListener<T> {
        void onComplete(T data);
    }

    public boolean authentication() {
        return false;
    }

    public boolean hasToken() {
        return false;
    }

    public void login(Context context, OnSyncDataListener<Boolean> callback) {
    }

    public void logout() {
    }

    public void deactivate() {
    }

    public void loadData(GetAccountCallback callback) {
    }

    public void initUpload() {
        if (folderDAO == null) {
            folderDAO = new FolderDAO(context);
        }
        if (roadDAO == null) {
            roadDAO = new RoadDAO(context);
        }
        if (measurementDAO == null) {
            measurementDAO = new MeasurementDAO(context);
        }
        if (tagDAO == null) {
            tagDAO = new TagDAO(context);
        }
        startUploadFileCounter = 0;
        uploadedFileCounter = 0;
    }

    public boolean isFolderAlreadyExists(final String folder) {
        return false;
    }

    protected void checkValidToken() {
    }

    protected void checkDataDirs(String dropBoxFolder, FolderModel folder, RoadModel road, DataSyncMode mode) {
    }

    public void uploadFiles(final String dropBoxFolder) {
        initUpload();
        showProgress(true);
        new AsyncTask<Object, Object, Object>() {
            @Override
            protected Object doInBackground(Object[] params) {
                checkValidToken();
                checkDataDirs(dropBoxFolder, null, null, DataSyncMode.ALL);
                final List<FolderModel> folderModels = folderDAO.getAllFolders();
                for (final FolderModel folder : folderModels) {
                    uploadProject(folder, dropBoxFolder + DIR_SLASH + folder.getName());
                }
                return null;
            }

            @Override
            protected void onPostExecute(Object o) {
                if (startUploadFileCounter == 0) {
                    showToastNoData();
                }
                showProgress(false);
            }
        }.execute();
    }

    public void uploadSingleProject(final FolderModel folder, final String dropboxPath,
                                    final MeasurementsDataHelper.MeasurementsDataLoaderListener listener) {
        initUpload();
        //showProgress(true);
        new AsyncTask<Object, Object, Object>() {
            @Override
            protected Object doInBackground(Object[] params) {
                checkValidToken();
                checkDataDirs(dropboxPath, folder, null, DataSyncMode.PROJECT);
                uploadProject(folder, dropboxPath);
                return null;
            }

            @Override
            protected void onPostExecute(Object o) {
                showProgress(false);
                if (listener != null) {
                    listener.onDataLoaded(true);
                }
            }
        }.execute();
    }

    public void uploadSingleRoad(final RoadModel road, final String dropboxPath,
                                 final MeasurementsDataHelper.MeasurementsDataLoaderListener listener) {
        initUpload();
        //showProgress(true);
        new AsyncTask<Object, Object, Object>() {
            @Override
            protected Object doInBackground(Object[] params) {
                checkValidToken();
                long folderId = road.getFolderId();
                FolderModel folder = folderDAO.getFolder(folderId);
                checkDataDirs(dropboxPath, folder, road, DataSyncMode.ROAD);
                uploadRoad(folder, road, dropboxPath);
                return null;
            }

            @Override
            protected void onPostExecute(Object o) {
                showProgress(false);
                if (listener != null) {
                    listener.onDataLoaded(true);
                }
            }
        }.execute();
    }

    protected void uploadProject(FolderModel folder, String dropboxPath) {
        uploadProjectSummaryFile(folder, dropboxPath);
        final List<RoadModel> roadModels = roadDAO.getAllRoadsByFolderId(folder.getId());
        String dropboxPathLoc = "";
        for (final RoadModel road : roadModels) {
            dropboxPathLoc = getProjectRoadPath(folder, road, dropboxPath);
            uploadRoad(folder, road, dropboxPathLoc);
        }
    }

    protected String getProjectRoadPath(FolderModel folder, RoadModel road, String dropboxPath) {
        String path = "";
        if (!TextUtils.isEmpty(dropboxPath)) {
            path = dropboxPath + DIR_SLASH + folder.getName() + DIR_SLASH + road.getName();
        }
        return path;
    }

    private void uploadProjectSummaryFile(FolderModel folder, String dropboxPath) {
        String projectName = folder.getName();
        Date projectDate = new Date(folder.getDate());
        String summaryFileLocalDir = FileUtils.getDataDir(projectName, false);
        File summaryFile = new File(summaryFileLocalDir + "/" + String.format(Constants.PROJECT_SUMMARY + "_%s_%s%s",
        DeviceUtil.getDeviceName(), new SimpleDateFormat(TimeUtil.DATE_FILENAME_FORMAT).format(projectDate), Constants.CSV_EXT));
        uploadEntityFile(summaryFile, dropboxPath, MeasurementItemType.SUMMARY);
    }

    protected void uploadRoad(FolderModel folder, RoadModel road, String dropboxPath) {
        final List<MeasurementModel> measurementModels = measurementDAO.getAllMeasurementsByRoadId(road.getId());
        uploadMeasurements(measurementModels, dropboxPath, folder, road);
        final List<TagModel> tagModels = tagDAO.getTagsListByRoadId(road.getId());
        uploadTags(tagModels, dropboxPath, folder, road);
    }

    protected void uploadMeasurement(String dropboxPath, final FolderModel folder, final RoadModel road, final MeasurementModel measurement) {
        uploadMeasurementData(dropboxPath, folder, road, measurement, MeasurementItemType.INTERVAl);
        uploadMeasurementData(dropboxPath, folder, road, measurement, MeasurementItemType.BUMP);
        uploadMeasurementData(dropboxPath, folder, road, measurement, MeasurementItemType.GEO_TAG);
    }

    protected void uploadTags(final List<TagModel> tagModels, String dropboxPath,
        final FolderModel folder, final RoadModel road) {
        String roadName = road.getName();
        String projectName = folder.getName();
        String localProjectPath = projectName + DIR_SLASH + roadName + DIR_SLASH + TAGS_PATH_PREFIX + DIR_SLASH;
        String dropboxProjectPath = dropboxPath;
        if (TextUtils.isEmpty(dropboxProjectPath)) {
            dropboxProjectPath += DIR_SLASH + projectName + DIR_SLASH + roadName;
        }
        dropboxProjectPath += DIR_SLASH + TAGS_PATH_PREFIX;
        String dataDir = FileUtils.getDataDir(localProjectPath, true);
        Date roadDate = new Date(road.getDate());
        File tagsCSVFile = new File(dataDir + String.format(MeasurementItemType.TAG.getName() + "_%s_%s%s",
        DeviceUtil.getDeviceName(), new SimpleDateFormat(TimeUtil.DATE_FILENAME_FORMAT).format(roadDate), Constants.CSV_EXT));
        uploadEntityFile(tagsCSVFile, dropboxProjectPath, MeasurementItemType.TAG);
        File tagsKmlFile = KMLHelper.createOutFile(roadDate, dataDir, MeasurementItemType.TAG);
        uploadEntityFile(tagsKmlFile, dropboxProjectPath, MeasurementItemType.TAG);
        for (final TagModel tag : tagModels) {
            uploadTagMediaFiles(tag, roadName, projectName, dataDir, dropboxProjectPath);
        }
    }

    protected void uploadTagMediaFiles(final TagModel tag, String roadName, String projectName,
        String dataDir, String dropboxProjectPath) {
        String tagName = getTagMediaDirName(tag, projectName, roadName);
        File mediaFile;
        for (final String postfix : TAGS_POSTFIXES) {
            mediaFile = new File(dataDir + tagName + DIR_SLASH + tagName + postfix);
            if (!mediaFile.exists()) {
                setTagIsUploaded(tag);
                continue;
            }
            uploadTagMedia(tag, mediaFile, tagName, dropboxProjectPath);
        }
    }

    private void uploadTagMedia(final TagModel tag, File file, String tagName, String dropboxProjectPath) {
        startUploadFileCounter++;
        String mediaFilesDir = dropboxProjectPath + DIR_SLASH + tagName;
        runUploadFiles(file.getPath(), mediaFilesDir, MeasurementItemType.TAG_MEDIA, new SyncFilesCallback() {
            @Override
            public void onUploadComplete(List<String> result) {
                uploadedFileCounter++;
                setTagIsUploaded(tag);
                //if (uploadedFileCounter == startUploadFileCounter) {
                //    showProgress(false);
                //}
            }
            @Override
            public void onError(Exception e) {
                Log.d(TAG, context.getString(R.string.toast_error_dbx_synchronise), e);
                uploadedFileCounter++;
                //if (uploadedFileCounter == startUploadFileCounter) {
                //    showProgress(false);
                //}
            }
        });
    }

    private void uploadEntityFile(File entityFile, String dropboxProjectPath, MeasurementItemType type) {
        startUploadFileCounter++;
        runUploadFiles(entityFile.getPath(), dropboxProjectPath, type, new SyncFilesCallback() {
            @Override
            public void onUploadComplete(List<String> result) {
                uploadedFileCounter++;
                if (uploadedFileCounter == startUploadFileCounter) {
                    //showProgress(false);
                    showToastSuccess();
                }
            }

            @Override
            public void onError(Exception e) {
                Log.d(TAG, context.getString(R.string.toast_error_dbx_synchronise), e);
                uploadedFileCounter++;
                if (uploadedFileCounter == startUploadFileCounter) {
                    //showProgress(false);
                    showToastFail();
                }
            }
        });
    }

    private void uploadMeasurements(final List<MeasurementModel> measurementModels,
        String dropboxPath, final FolderModel folder, final RoadModel road) {
        for (final MeasurementModel measurement : measurementModels) {
            uploadMeasurement(dropboxPath, folder, road, measurement);
        }
    }

    private void uploadMeasurementData(String dropboxPath, final FolderModel folder, final RoadModel road,
        final MeasurementModel measurement, MeasurementItemType type) {

        String roadName = road.getName();
        String projectName = folder.getName();
        String measurementName;
        Date measurementDate;

        if (TextUtils.isEmpty(dropboxPath)) {
            dropboxPath += DIR_SLASH + projectName + DIR_SLASH + roadName;
        }

        String localProjectPath;
        String dropboxProjectPath;
        String dataDir;

        File measurementCSV = null;
        File measurementKML = null;
        String[] filesPaths = new String[2];
        measurementName = getMeasurementDirName(measurement);
        localProjectPath = projectName + DIR_SLASH + roadName + DIR_SLASH + measurementName + DIR_SLASH;
        dropboxProjectPath = dropboxPath + DIR_SLASH + measurementName;
        dataDir = FileUtils.getDataDir(localProjectPath, true);
        measurementDate = new Date(measurement.getDate());
        String csvFileName = dataDir + String.format(type.getName() + "_%s_%s",
            DeviceUtil.getDeviceName(),
            new SimpleDateFormat(TimeUtil.DATE_FILENAME_FORMAT).format(measurementDate));
        if (MeasurementItemType.GEO_TAG.equals(type)) {
            csvFileName += Constants.GEO_TAG_SUFFIX;
        }
        csvFileName += Constants.CSV_EXT;
        measurementCSV = new File(csvFileName);
        measurementKML = KMLHelper.createOutFile(measurementDate, dataDir, type);
        filesPaths[0] = measurementCSV.getAbsolutePath();
        filesPaths[1] = measurementKML.getAbsolutePath();
        uploadMeasurementItem(folder, road, measurement, filesPaths, dropboxProjectPath);
    }

    protected String getMeasurementDirName(final MeasurementModel measurement) {
        return FileUtils.getExportItemName(context, measurement.getId(), measurement.getDate());
    }

    protected String getTagMediaDirName(final TagModel tag, String projectName, String roadName) {
        String tagName = FileUtils.getExportItemNameTag(tag, projectName, roadName);
        //String tagName = FileUtils.getExportItemName(context, tag.getId(), tag.getDate(), MeasurementItemType.TAG);
        return tagName;
    }

    private void uploadMeasurementItem(final FolderModel folder, final RoadModel road, final MeasurementModel measurement,
        String[] files, String dropboxProjectPath) {
        if (files == null) {
            return;
        }
        startUploadFileCounter += files.length;
        runUploadFiles(files, dropboxProjectPath, new SyncFilesCallback() {
            @Override
            public void onUploadComplete(List<String> result) {
                if (result != null) {
                    uploadedFileCounter += result.size();
                }
                measurement.setUploaded(true);
                measurementDAO.updateMeasurement(measurement);
                road.setUploaded(true);
                roadDAO.updateRoad(road);
                folder.setUploaded(true);
                folderDAO.updateFolder(folder);
                if (uploadedFileCounter == startUploadFileCounter) {
                    //showProgress(false);
                    showToastSuccess();
                }
            }

            @Override
            public void onError(Exception e) {
                Log.d(TAG, context.getString(R.string.toast_error_dbx_synchronise), e);
                uploadedFileCounter++;
                if (uploadedFileCounter == startUploadFileCounter) {
                    //showProgress(false);
                    showToastFail();
                }
            }
        });
    }

    protected void runUploadFiles(String[] filePaths, String dropboxProjectPath, final SyncFilesCallback callback) {
    }

    protected Object runUploadFiles(String filePath, String dropboxProjectPath, MeasurementItemType type, final SyncFilesCallback callback) {
        return null;
    }

    private void setTagIsUploaded(TagModel tag) {
        tag.setUploaded(true);
        tagDAO.updateItem(tag);
    }

    public String getUserName() {
        return PreferencesUtil.getInstance().getAccountUserName();
    }

    protected void showToast(int msgId, int toastLength) {
        Toast.makeText(context, context.getString(msgId), toastLength).show();
    }

    protected void showToastNoData() {
        showToast(R.string.toast_error_dbx_synchronise_no_data, Toast.LENGTH_LONG);
    }

    protected void showToastSuccess() {
        showToast(R.string.toast_successful_dbx_synchronise, Toast.LENGTH_SHORT);
    }

    protected void showToastFail() {
        showToast(R.string.toast_error_dbx_synchronise, Toast.LENGTH_SHORT);
    }

    protected void showProgress(final boolean show) {
        if (dialog == null) {
            dialog = new ProgressDialog(context);
            dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            dialog.setCancelable(false);
            dialog.setMessage("Uploading");
            dialog.show();
        }
        if (show) {
            if (!dialog.isShowing()) {
                dialog.show();
            }
        } else if (dialog.isShowing()) {
            dialog.dismiss();
        }
    }

    public void onCreate() {
    }

    public void onResume() {
    }

    public void onPause() {
    }

    public void onDestroy() {
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
    }
}
