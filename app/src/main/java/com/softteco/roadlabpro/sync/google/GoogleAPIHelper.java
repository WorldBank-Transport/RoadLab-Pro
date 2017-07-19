package com.softteco.roadlabpro.sync.google;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Scope;
import com.google.gson.Gson;
import com.softteco.roadlabpro.rest.RestClient;
import com.softteco.roadlabpro.rest.dto.AccountData;
import com.softteco.roadlabpro.rest.dto.DriveFile;
import com.softteco.roadlabpro.rest.dto.DriveFolder;
import com.softteco.roadlabpro.rest.dto.DriveSearchResult;
import com.softteco.roadlabpro.rest.dto.FileItem;
import com.softteco.roadlabpro.rest.dto.FileSearchItem;
import com.softteco.roadlabpro.rest.dto.GoogleToken;
import com.softteco.roadlabpro.rest.dto.UpdateDriveFile;
import com.softteco.roadlabpro.sync.SyncDataManager;
import com.softteco.roadlabpro.ui.LoginGoogleDialog;
import com.softteco.roadlabpro.util.ActivityUtil;
import com.softteco.roadlabpro.util.Constants;
import com.softteco.roadlabpro.util.FileUtils;
import com.softteco.roadlabpro.util.PreferencesUtil;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.RequestBody;

import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;

public class GoogleAPIHelper implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private final String TAG = GoogleAPIHelper.class.getName();

    public static int GOOGLE_AUTH_REQUEST_CODE = 1001;
    public static int REQUEST_CODE_RESOLUTION = 1002;

	private static String CLIENT_ID = "381412265055-sforar89nm7er7ullreo9lj90iqkmncv.apps.googleusercontent.com";

	private static String CLIENT_SECRET ="tDl7LuMSGMvjyOR2NOTKE2Av";
    private static String REDIRECT_URI = "http://localhost";

	private static String GRANT_TYPE_AUTH = "authorization_code";
	private static String GRANT_TYPE_REFRESH = "refresh_token";
	private static String GOOGLE_OAUTH_URL = "https://accounts.google.com/o/oauth2/auth";

    public static String GOOGLE_REFRESH_TOKEN_KEY = "google_refresh_token";
	public static String GOOGLE_ACCESS_TOKEN_KEY = "google_access_token";
	public static String GOOGLE_EXPIRES_IN_KEY = "google_expires_in";

    private static String GOOGLE_DRIVE_BEARER_PARAM = "Bearer";
    private static String GOOGLE_DRIVE_OAUTH_SCOPE = "https://www.googleapis.com/auth/drive";
    private static String GOOGLE_DRIVE_MEDIA_PARAM = "media";
    private static String GOOGLE_DRIVE_ABOUT_FIELDS = "name,user";
    private static String GOOGLE_DRIVE_FILE_FIELDS = "id,title,kind,mimeType,createdDate,modifiedDate,downloadUrl";


    private static String CONTENT_TYPE_JSON = "application/json";

    public static String GOOGLE_DRIVE_FOLDER_MIME = "application/vnd.google-apps.folder";

    private static String GOOGLE_DRIVE_FILE_SEARCH_IN_ROOT_TEXT = "name=\"%s\" and 'root' in parents and trashed!=true";

    public static String GOOGLE_CODE_PARAM = "?code=";
	public static String GOOGLE_ERROR_ACCESS_DENIED_PARAM = "error=access_denied";

    public static String MEDIA_TYPE_TEXT = "text/*";
    public static String MEDIA_TYPE_TEXT_STR = "text/%s";
    public static String MEDIA_TYPE_PHOTO = "image/jpeg";
    public static String MEDIA_TYPE_AUDIO = "audio/mp3";

	private Context context;
//    private Dialog authDialog;
    private Gson gson;

    private GoogleApiClient mGoogleApiClient;
    private GoogleSignInOptions gso;
    private SyncDataManager.OnSyncDataListener<Boolean> callback;

    public void init(Context ctx) {
		this.context = ctx;
        gson = new Gson();
	}

    public GoogleAPIHelper(Context context) {
        init(context);
    }

	public void login(SyncDataManager.OnSyncDataListener<Boolean> callback) {
        //showWebDialog(callback);
        loginGoogleAPI();
        this.callback = callback;
	}
	
	public void logout() {
        PreferencesUtil.getInstance().setStringValue(GOOGLE_ACCESS_TOKEN_KEY, null);
        PreferencesUtil.getInstance().setGoogleAccountUserName("");
        deactivate();
    }

    public void deactivate() {
        if (mGoogleApiClient != null) {
            mGoogleApiClient.disconnect();
            mGoogleApiClient = null;
        }
        this.callback = null;
    }

	private void loginGoogleAPI() {
        if (!(context instanceof FragmentActivity)) {
            return;
        }
        gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestProfile()
                .requestId()
                .requestScopes(new Scope(Scopes.PLUS_LOGIN), new Scope(Scopes.DRIVE_FILE))
                .requestServerAuthCode(CLIENT_ID)
                .build();
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(context)
                .addApi(com.google.android.gms.auth.api.Auth.GOOGLE_SIGN_IN_API, gso)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        }
        if (!mGoogleApiClient.isConnected()) {
            mGoogleApiClient.connect();
        } else {
            sendAuthRequest();
        }
    }

    private void sendAuthRequest() {
        Log.i("", "handleSignInResult login:");
        Intent signInIntent = com.google.android.gms.auth.api.Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        ((Activity)context).startActivityForResult(signInIntent, GOOGLE_AUTH_REQUEST_CODE);
    }

	public static String getUrl() {
		return GOOGLE_OAUTH_URL + "?redirect_uri=" + REDIRECT_URI + "&response_type=code&client_id=" + CLIENT_ID +
        "&scope=" + GOOGLE_DRIVE_OAUTH_SCOPE + "&access_type=offline" + "&approval_prompt=force";
	}

//	private void showWebDialog(SyncDataManager.OnSyncDataListener<Boolean> callback) {
//        if (authDialog != null) {
//            authDialog.dismiss();
//        }
//        authDialog = new LoginGoogleDialog(context, this, callback);
//		authDialog.show();
//		authDialog.setTitle(null);
//		authDialog.setCancelable(true);
//	}

	protected GoogleToken refreshOldToken(String refreshToken) {
        GoogleToken googleToken = null;
        try {
            Response<GoogleToken> result = RestClient.getInstance().getApiService().
            getGoogleApiToken(null, CLIENT_ID, CLIENT_SECRET, REDIRECT_URI, refreshToken, GRANT_TYPE_REFRESH).execute();
            if (result != null) {
                googleToken = result.body();
            }
        } catch (Exception e) {
            Log.e(TAG, "refreshOldToken", e);
        }
        return googleToken;
	}

    public void obtainNewToken(String code, final Callback<GoogleToken> callback) {
        RestClient.getInstance().getApiService().
        getGoogleApiToken(code, CLIENT_ID, CLIENT_SECRET, REDIRECT_URI, null, GRANT_TYPE_AUTH).enqueue(callback);
    }

	private Date getGoogleExpireDate() {
		String expire = PreferencesUtil.getInstance().getStringValue(GOOGLE_EXPIRES_IN_KEY);
		Date expireDate = null;
		try {
			if (!TextUtils.isEmpty(expire)) {
				long expireTime = Long.valueOf(expire);
				expireDate = new Date(expireTime);
				return expireDate;
			}
		} catch (Exception e) {
            Log.e(TAG, "getGoogleExpireDate", e);
        }
		expireDate = Calendar.getInstance().getTime();
		return expireDate;
	}

	public void checkToken() {
		Date curDate = Calendar.getInstance().getTime();
		Date expDate = getGoogleExpireDate();
		if (expDate != null && curDate.before(expDate)) {
			return;
		}
		String refreshToken = PreferencesUtil.getInstance().getStringValue(GOOGLE_REFRESH_TOKEN_KEY);
		if (TextUtils.isEmpty(refreshToken)) {
			return;
		}
        GoogleToken googleToken = refreshOldToken(refreshToken);
        rememberToken(googleToken);
	}

    public void rememberToken(GoogleToken token) {
        String accessToken = token.getAccessToken();
        if (!TextUtils.isEmpty(accessToken)) {
            PreferencesUtil.getInstance().setGoogleAccessToken(accessToken);
            Log.d("Access token: ", accessToken);
        }
        String refresh = token.getRefreshToken();
        if (!TextUtils.isEmpty(refresh)) {
            PreferencesUtil.getInstance().setStringValue(GoogleAPIHelper.GOOGLE_REFRESH_TOKEN_KEY, refresh);
            Log.d("Refresh token: ", refresh);
        }
        long expireIn = token.getExpiresIn();
        long expireTime = Calendar.getInstance().getTime().getTime() + (expireIn * 1000);
        PreferencesUtil.getInstance().setStringValue(GoogleAPIHelper.GOOGLE_EXPIRES_IN_KEY, String.valueOf(expireTime));
        Log.d("Expire", "Expire: " + token.getExpiresIn() + " Expire time: " + expireTime);
    }

    public void getAccountData(Callback<AccountData> callback) {
        String auth = getAuthStr();
        RestClient.getInstance().getApiService().getUserInfo(GOOGLE_DRIVE_ABOUT_FIELDS, auth).enqueue(callback);
    }

    public FileItem createFolderWithSubFoldersSync(String dirPath) {
        return createFolderWithSubFoldersSync(dirPath, false);
    }

    public FileItem createFolderWithSubFoldersSync(String dirPath, boolean checkIsExists) {
        String[] subDirs = dirPath.split("/");
        FileItem prevDir = null;
        String parentId = null;
        DriveSearchResult searchResult = null;
        for (String dirName : subDirs) {
            if (checkIsExists) {
                searchResult = searchFileByName(dirName);
                if (searchResult != null && searchResult.getFirst() != null) {
                    FileSearchItem item = searchResult.getFirst();
                    if (item.isDirectory()) {
                        parentId = item.getId();
                        continue;
                    }
                }
            }
            if (prevDir != null) {
                parentId = prevDir.getId();
            }
            prevDir = createFolderSync(parentId, dirName);
        }
        return prevDir;
    }

    public DriveFile createFolderSync(String parentId, String dirName) {
        String auth = getAuthStr();
        UpdateDriveFile driveFolder = new UpdateDriveFile();
        driveFolder.setMimeType(GOOGLE_DRIVE_FOLDER_MIME);
        driveFolder.setTitle(dirName);
        if (!TextUtils.isEmpty(parentId)) {
            List<DriveFolder> parentsList = new ArrayList<DriveFolder>();
            DriveFolder parent = new DriveFolder();
            parent.setId(parentId);
            parentsList.add(parent);
            driveFolder.setParents(parentsList);
        }
        MediaType contentType = MediaType.parse(CONTENT_TYPE_JSON);
        String driveFolderStr = gson.toJson(driveFolder);
        RequestBody data = RequestBody.create(contentType, driveFolderStr);
        DriveFile folderObj = null;
        try {
            Response<DriveFile> result = RestClient.getInstance().getApiService().
            createFolder(CONTENT_TYPE_JSON, GOOGLE_DRIVE_FILE_FIELDS, auth, data).execute();
            if (result != null) {
                folderObj = result.body();
            }
        } catch(Exception e) {
            Log.e(TAG, "createFolderSync", e);
        }
        return folderObj;
    }

    public Object createFileSync(String parentId, String filePath) {
        String auth = getAuthStr();
        File file = new File(filePath);
        String mediaType = getFileMime(file);
        long fileLength = file.length();
        MediaType type = MediaType.parse(mediaType);
        RequestBody data = RequestBody.create(type, file);
        DriveFile driveFile = null;
        Object resultObj = null;
        try {
            Response<DriveFile> result = RestClient.getInstance().getApiService().
            uploadGooogleFile(GOOGLE_DRIVE_MEDIA_PARAM, GOOGLE_DRIVE_FILE_FIELDS, mediaType, fileLength, auth, data).execute();
            if (result != null) {
                driveFile = result.body();
            }
            String fileId = null;
            if (driveFile != null) {
                fileId = driveFile.getId();
            }
            if (fileId != null && parentId != null) {
                driveFile = setFileFolderSync(parentId, fileId, file.getName());
            }
            resultObj = driveFile;
        } catch (Exception e) {
            resultObj = e;
            Log.e(TAG, "createFileSync", e);
        }
        return resultObj;
    }

    public DriveFile setFileFolderSync(String parentId, String fileId, String fileName) {
        UpdateDriveFile updateFileInfo = new UpdateDriveFile();
        if (!TextUtils.isEmpty(fileName)) {
            updateFileInfo.setTitle(fileName);
        }
        if (!TextUtils.isEmpty(parentId)) {
            List<DriveFolder> parentsList = new ArrayList<DriveFolder>();
            DriveFolder parent = new DriveFolder();
            parent.setId(parentId);
            parentsList.add(parent);
            updateFileInfo.setParents(parentsList);
        }
        String driveFileStr = gson.toJson(updateFileInfo);
        MediaType contentType = MediaType.parse(CONTENT_TYPE_JSON);
        String auth = getAuthStr();
        DriveFile driveFile = null;
        try {
            RequestBody data = RequestBody.create(contentType, driveFileStr);
            Response<DriveFile> result = RestClient.getInstance().getApiService().
            setFileFolder(fileId, GOOGLE_DRIVE_FILE_FIELDS, auth, data).execute();
            if (result != null) {
                driveFile = result.body();
            }
        } catch (Exception e) {
            Log.e(TAG, "setFileFolderSync", e);
        }
        return driveFile;
    }

    public DriveSearchResult searchFileByName(String fileName) {
        DriveSearchResult searchResult = null;
        String auth = getAuthStr();
        try {
            String searchQueryStr = String.format(GOOGLE_DRIVE_FILE_SEARCH_IN_ROOT_TEXT, fileName);
            Response<DriveSearchResult> result = RestClient.getInstance().getApiService().
            searchFiles(auth, searchQueryStr).execute();
            if (result != null) {
                searchResult = result.body();
            }
        } catch (Exception e) {
            Log.e(TAG, "searchFileByName", e);
        }
        return searchResult;
    }

    private String getAuthStr() {
        String token = PreferencesUtil.getInstance().getGoogleAccessToken();
        String auth = GOOGLE_DRIVE_BEARER_PARAM + " " + token;
        return auth;
    }

    private String getFileMime(File file) {
        String extension = FileUtils.getFileExtension(file);
        if (MEDIA_TYPE_PHOTO.contains(extension) || Constants.JPG_EXT.contains(extension)) {
            return MEDIA_TYPE_PHOTO;
        } else if (MEDIA_TYPE_AUDIO.contains(extension) || Constants.MP3_EXT.contains(extension)) {
            return MEDIA_TYPE_AUDIO;
        } else {
            if (!TextUtils.isEmpty(extension)) {
                return String.format(MEDIA_TYPE_TEXT_STR, extension);
            } else {
                return MEDIA_TYPE_TEXT;
            }
        }
    }

    private void obtainNewToken(String authCode) {
        obtainNewToken(authCode, new Callback<GoogleToken>() {
            @Override
            public void onResponse(Response<GoogleToken> response, Retrofit retrofit) {
                GoogleToken googleToken = null;
                if (response.body() != null) {
                    googleToken = response.body();
                }
                String token = "";
                if (googleToken != null) {
                    token = googleToken.getAccessToken();
                }
                if (TextUtils.isEmpty(token)) {
                    Toast.makeText(context, "Network Error", Toast.LENGTH_SHORT).show();
                } else {
                    rememberToken(googleToken);
                }
                checkUserName();
            }

            @Override
            public void onFailure(Throwable t) {
                Log.e(TAG, "obtainNewToken", t);
            }
        });
    }

    private void checkUserName(Callback<AccountData> callback) {
        getAccountData(callback);
    }

    private void checkUserName() {
        checkUserName(new Callback<AccountData>() {
            @Override
            public void onResponse(Response<AccountData> response, Retrofit retrofit) {
                AccountData data = null;
                if (response != null) {
                    data = response.body();
                }
                String userName = "";
                if (data != null) {
                    if (data.getUser() != null && !TextUtils.isEmpty(data.getUser().getDisplayName())) {
                        userName = data.getUser().getDisplayName();
                    } else if (!TextUtils.isEmpty(data.getName())) {
                        userName = data.getName();
                    }
                }
                if (!TextUtils.isEmpty(userName)) {
                    PreferencesUtil.getInstance().setGoogleAccountUserName(userName);
                }
                if (callback != null) {
                    callback.onComplete(true);
                }
            }
            @Override
            public void onFailure(Throwable t) {
                Log.e(TAG, "checkUserName", t);
            }
        });
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.i(TAG, "onConnected, bundle: " + bundle);
        sendAuthRequest();
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i(TAG, "onConnectionSuspended");
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult result) {
        Log.i(TAG, "onConnectionFailed: connectionResult: " + result);
    }

    protected void onResume() {
    }

    protected void onPause() {
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.i(TAG, "onActivityResult: data: " + ActivityUtil.dumpIntent(data));
        if (requestCode == GOOGLE_AUTH_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            GoogleSignInResult result = com.google.android.gms.auth.api.Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if (result.isSuccess()) {
                // Signed in successfully, show authenticated UI.
                GoogleSignInAccount acct = result.getSignInAccount();
                String authCode = acct.getServerAuthCode();
                Log.i(TAG, "onActivityResult: authCode: " + authCode);
                if (!TextUtils.isEmpty(authCode)) {
                    obtainNewToken(authCode);
                }
            } else {
                // Signed out, show unauthenticated UI.
            }
        }
    }
}
