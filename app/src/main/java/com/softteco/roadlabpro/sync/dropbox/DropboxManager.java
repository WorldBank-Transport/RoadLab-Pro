package com.softteco.roadlabpro.sync.dropbox;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.StrictMode;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.dropbox.core.DbxException;
import com.dropbox.core.android.Auth;
import com.dropbox.core.android.AuthActivity;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.FileMetadata;
import com.dropbox.core.v2.files.ListFolderResult;
import com.dropbox.core.v2.files.WriteMode;
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
import com.softteco.roadlabpro.sync.GetAccountCallback;
import com.softteco.roadlabpro.sync.SyncDataManager;
import com.softteco.roadlabpro.sync.SyncFilesCallback;
import com.softteco.roadlabpro.util.ActivityUtil;
import com.softteco.roadlabpro.util.Constants;
import com.softteco.roadlabpro.util.DeviceUtil;
import com.softteco.roadlabpro.util.FileUtils;
import com.softteco.roadlabpro.util.KMLHelper;
import com.softteco.roadlabpro.util.PreferencesUtil;
import com.softteco.roadlabpro.util.TimeUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by bogdan on 13.04.2016.
 */

/**
 * Base class that require auth tokens
 * Will redirect to auth flow if needed
 */

public class DropboxManager extends SyncDataManager {

    private final String TAG = DropboxManager.class.getName();

    public DropboxManager(Context context) {
        super(context);
    }

    @Override
    public boolean authentication() {
        String accessToken = PreferencesUtil.getInstance().getDropboxAccessToken();
        if (TextUtils.isEmpty(accessToken)) {
            accessToken = Auth.getOAuth2Token();
            if (accessToken != null) {
                PreferencesUtil.getInstance().setDropboxAccessToken(accessToken);
                initData(accessToken);
                return true;
            }
        } else {
            initData(accessToken);
            return true;
        }
        return false;
    }

    private void initData(String accessToken) {
        DropboxClientFactory.init(accessToken);
    }

    @Override
    public void loadData(final GetAccountCallback callback) {
        new GetCurrentAccountTaskDropbox(DropboxClientFactory.getClient(), callback).execute();
    }

    @Override
    public boolean hasToken() {
        String accessToken = PreferencesUtil.getInstance().getDropboxAccessToken();
        return !TextUtils.isEmpty(accessToken);
    }

    @Override
    public void login(Context context, OnSyncDataListener<Boolean> callback) {
        Auth.startOAuth2Authentication(context, Constants.DROPBOX_APPLICATION_KEY);
    }

    @Override
    public void logout() {
        AuthActivity.result = null;
        PreferencesUtil.getInstance().resetDropboxAccessToken();
        PreferencesUtil.getInstance().setDropboxAccountUserName("");
    }

    @Override
    public boolean isFolderAlreadyExists(String folder) {
        ListFolderResult dropboxFolders = null;
        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
            try {
                String dir = "/" + folder.substring(0, folder.lastIndexOf("/"));
                dropboxFolders = DropboxClientFactory.getClient().files().listFolder(dir);
            } catch (DbxException e) {
                e.printStackTrace();
            }
        }
        if (dropboxFolders != null) {
            Log.d(TAG, dropboxFolders.toString());
            String s = dropboxFolders.toStringMultiline();
            if (s.contains("/" + folder))
                return true;
        }
        return false;
    }

    @Override
    protected void runUploadFiles(String[] filePaths, String dropboxProjectPath, final SyncFilesCallback callback) {
        Exception e = null;
        final List<String> uploadedFiles = new ArrayList<>();
        for (String filePath : filePaths) {
            Object data = runUploadFiles(filePath, dropboxProjectPath, null, null);
            if (data != null) {
                if (data instanceof Exception) {
                    e = (Exception) data;
                } else if(data instanceof FileMetadata) {
                    FileMetadata metadata = (FileMetadata) data;
                    if (metadata.getName() != null) {
                        uploadedFiles.add(metadata.getName());
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
    protected Object runUploadFiles(String filePath, String dropboxProjectPath, MeasurementItemType type, final SyncFilesCallback callback) {
        //int exportId = PreferencesUtil.getInstance().getIntValue(Constants.DROPBOX_EXPORT_ID_KEY, 0);
        FileMetadata localMetadata = null;
        final Object data = syncUploadFile(DropboxClientFactory.getClient(), filePath, "/" + dropboxProjectPath);
        if (data == null || data instanceof Exception) {
            if (callback != null) {
                ActivityUtil.runOnMainThread(new Runnable() {
                    @Override
                    public void run() {
                        Exception e = null;
                        if (data != null) {
                            e = (Exception) data;
                        }
                        callback.onError(e);
                    }
                });
            }
            return data;
        }
        if (data instanceof FileMetadata) {
            localMetadata = (FileMetadata) data;
        }
        if (localMetadata != null) {
            String message = localMetadata.getName() + " size " + localMetadata.getSize() + " modified " +
                    DateFormat.getDateTimeInstance().format(localMetadata.getClientModified());
            Log.d(TAG, message);
        }
        final FileMetadata metadata = localMetadata;
        ActivityUtil.runOnMainThread(new Runnable() {
            @Override
            public void run() {
                if (callback != null) {
                    List<String> data = new ArrayList<>();
                    if (metadata != null && metadata.getName() != null) {
                        data.add(metadata.getName());
                    }
                    callback.onUploadComplete(data);
                }
            }
        });
        return data;
    }

    public Object syncUploadFile(DbxClientV2 mDbxClient, String localUri, String remoteFolder) {
        Log.d(TAG, "localUri: " + localUri);
        File localFile = new File(localUri);
        if (localFile.exists()) {
            // Note - this is not ensuring the name is a valid dropbox file name
            String remoteFileName = localFile.getName();
            try (InputStream inputStream = new FileInputStream(localFile)) {
                return mDbxClient.files().uploadBuilder(remoteFolder + "/" + remoteFileName)
                        .withMode(WriteMode.OVERWRITE).uploadAndFinish(inputStream);
            } catch (Exception e) {
                Log.e(TAG, "stringUploadFile", e);
                return e;
            }
        }
        return null;
    }
}
