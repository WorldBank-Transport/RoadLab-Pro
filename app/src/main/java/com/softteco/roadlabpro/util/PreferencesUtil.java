package com.softteco.roadlabpro.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.text.TextUtils;
import android.util.TypedValue;

import com.softteco.roadlabpro.R;
import com.softteco.roadlabpro.RAApplication;
import com.softteco.roadlabpro.sync.SyncDataType;
import com.softteco.roadlabpro.sync.google.GoogleAPIHelper;

/**
 * Created by Aleksey on 08.04.2015.
 */
public final class PreferencesUtil {

    private static final String KEY_DEVICE_ID = "device_id";
    private static final String KEY_DEVICE_NAME = "device_name";
    private static final String KEY_USER_EMAIL = "user_email_id";
    private static final String KEY_USER_EMPLOYEE = "user_employee";
    private static final String KEY_SESSION_ID = "session_id";
    private static final String EXPERIMENT_NUMBER = "EXPERIMENT_NUMBER";
    private static final String VEHICLE = "VEHICLE";
    private static final String SYNC_MODE = "SYNC_MODE";
    private static final String TIME_INTERVAL = "TIME_INTERVAL";
    private static final String THRESHOLDS_PREF = "THRESHOLDS";
    private static final String THRESHOLDS_MORE_PREF = "THRESHOLDS_MORE_PREF";
    private static final String SUSPENSION_TYPE = "SUSPENSION_TYPE";
    private static final String EXPORT_TYPE = "EXPORT_TYPE";
    private static final String KEY_SLEEP_MODE_ENABLED = "KEY_SLEEP_MODE_ENABLED";
    private static final String KEY_AUTO_ROUGHNESS_DETECTION = "KEY_AUTO_ROUGHNESS_DETECTION";
    private static final String KEY_OVERALL_DISTANCE = "KEY_OVERALL_DISTANCE";
    private static final String SHOW_HELP = "show_help";
    private static final String SHOW_LOGIN = "show_login";
    private static final String KEY_CURRENT_FOLDER_ID = "current_folder_id";
    private static final String KEY_CURRENT_ROAD_ID = "current_road_id";

    private static final String KEY_FILTER_SHOW_BUMPS = "filter_show_bumps";
    private static final String KEY_FILTER_SHOW_TAGS = "filter_show_tags";
    private static final String KEY_FILTER_SHOW_INTERVALS = "filter_show_intervals";

    private static final String KEY_PROJECTS_EXISTS = "key_projects_exists";

    private static final String KEY_SYNC_DATA_TYPE = "sync_data_type";

    private static volatile PreferencesUtil instance;
    private final Context context;

    private PreferencesUtil(Context context) {
        this.context = context;
    }

    public static PreferencesUtil getInstance() {
        return PreferencesUtil.getInstance(RAApplication.getInstance());
    }

    public static PreferencesUtil getInstance(Context context) {
        PreferencesUtil localInstance = instance;
        if (localInstance == null) {
            synchronized (PreferencesUtil.class) {
                localInstance = instance;
                if (localInstance == null) {
                    instance = localInstance = new PreferencesUtil(context);
                }
            }
        }
        return localInstance;
    }

    public Context getContext() {
        return context;
    }

    private SharedPreferences getDropboxSharedPrefs() {
        SharedPreferences prefs = RAApplication.getInstance().
        getSharedPreferences(Constants.DROPBOX_ACCESS_PREF_KEY, RAApplication.MODE_PRIVATE);
        return prefs;
    }

    public void setSyncProviderType(SyncDataType value) {
        if (value != null) {
            setStringValue(KEY_SYNC_DATA_TYPE, value.name());
        }
    }

    public String getSyncProviderTypeName(Context context) {
        SyncDataType syncDataType = getInstance().getSyncProviderType();
        int syncDataTypeNameId = syncDataType.getNameId();
        String dataTypeName = context.getString(syncDataTypeNameId);
        return dataTypeName;
    }

    public SyncDataType getSyncProviderType() {
        SyncDataType type = SyncDataType.DROPBOX;
        String syncDataTypeStr = getStringValue(KEY_SYNC_DATA_TYPE, "");
        if (TextUtils.isEmpty(syncDataTypeStr)) {
            return type;
        }
        SyncDataType[] types = SyncDataType.values();
        for (SyncDataType dataType : types) {
            if (dataType.name().equals(syncDataTypeStr)) {
                type = dataType;
            }
        }
        return type;
    }

    public void setGoogleAccountUserName(String value) {
        setStringValue(Constants.GOOGLE_ACCOUNT_USER_NAME_KEY, value);
    }

    public String getGoogleAccountUserName() {
        return getStringValue(Constants.GOOGLE_ACCOUNT_USER_NAME_KEY, "");
    }

    public String getGoogleAccessToken() {
        return getStringValue(GoogleAPIHelper.GOOGLE_ACCESS_TOKEN_KEY, "");
    }

    public void setGoogleAccessToken(String value) {
        if (!TextUtils.isEmpty(value)) {
            setStringValue(GoogleAPIHelper.GOOGLE_ACCESS_TOKEN_KEY, value);
        }
    }

    public void setDropboxAccountUserName(String value) {
        setStringValue(Constants.DROPBOX_ACCOUNT_USER_NAME_KEY, value);
    }

    public String getAccountUserName() {
        SyncDataType type = getSyncProviderType();
        switch (type) {
            case GOOGLE_DRIVE:
                return getStringValue(Constants.GOOGLE_ACCOUNT_USER_NAME_KEY, "");
            case DROPBOX:
            default:
                return getStringValue(Constants.DROPBOX_ACCOUNT_USER_NAME_KEY, "");
        }
    }

    public String getDropboxAccessToken() {
        SharedPreferences prefs = getDropboxSharedPrefs();
        return getStringValue(prefs, Constants.DROPBOX_ACCESS_TOKEN_KEY, "");
    }

    public void setDropboxAccessToken(String value) {
        if (!TextUtils.isEmpty(value)) {
            SharedPreferences prefs = getDropboxSharedPrefs();
            setStringValue(prefs, Constants.DROPBOX_ACCESS_TOKEN_KEY, value);
        }
    }

    public void resetDropboxAccessToken() {
        SharedPreferences prefs = getDropboxSharedPrefs();
        setStringValue(prefs, Constants.DROPBOX_ACCESS_TOKEN_KEY, "");
    }

    public boolean isProjectsExists() {
        return getBooleanValue(KEY_PROJECTS_EXISTS, false);
    }

    public void setProjectsExists(boolean isProjectExists) {
        setBooleanValue(KEY_PROJECTS_EXISTS, isProjectExists);
    }

    public boolean getShowBumpsFilter() {
        return getBooleanValue(KEY_FILTER_SHOW_BUMPS, true);
    }

    public void setShowBumpsFilter(boolean value) {
        setBooleanValue(KEY_FILTER_SHOW_BUMPS, value);
    }

    public boolean getShowTagsFilter() {
        return getBooleanValue(KEY_FILTER_SHOW_TAGS, true);
    }

    public void setShowTagsFilter(boolean value) {
        setBooleanValue(KEY_FILTER_SHOW_TAGS, value);
    }

    public boolean getShowIntervalsFilter() {
        return getBooleanValue(KEY_FILTER_SHOW_INTERVALS, true);
    }

    public void setShowIntervalsFilter(boolean value) {
        setBooleanValue(KEY_FILTER_SHOW_INTERVALS, value);
    }

    public void generateDataRecordSession() {
        long session = TimeUtil.getCurrentTimeMillis();
        setLongValue(KEY_SESSION_ID, session);
    }

    public long getDataRecordSession() {
        return getLongValue(KEY_SESSION_ID, TimeUtil.getCurrentTimeMillis());
    }

    public float getOverallDistance() {
        return getFloatValue(KEY_OVERALL_DISTANCE, 0);
    }

    public void setOverallDistance(float distance) {
        setFloatValue(KEY_OVERALL_DISTANCE, distance);
    }

    public String getVehicle() {
        return getStringValue(VEHICLE, "");
    }

    public void setVehicle(String value) {
        setStringValue(VEHICLE, value);
    }

    public String getDeviceId() {
        return getStringValue(KEY_DEVICE_ID, "");
    }

    public void setDeviceId(String value) {
        setStringValue(KEY_DEVICE_ID, value);
    }

    public long getCurrentFolderId() {
        return getLongValue(KEY_CURRENT_FOLDER_ID, 0);
    }

    public void setCurrentFolderId(long value) {
        setLongValue(KEY_CURRENT_FOLDER_ID, value);
    }

    public long getCurrentRoadId() {
        return getLongValue(KEY_CURRENT_ROAD_ID, 0);
    }

    public void setCurrentRoadId(long value) {
        setLongValue(KEY_CURRENT_ROAD_ID, value);
    }

    public void setUserEmail(String value) {
        setStringValue(KEY_USER_EMAIL, value);
    }

    public String getUserEmail() {
        return getStringValue(KEY_USER_EMAIL, "");
    }

    public void setHasEmployeeLogin(boolean value) {
        setBooleanValue(KEY_USER_EMPLOYEE, value);
    }

    public boolean hasEmployeeLogin() {
        return getBooleanValue(KEY_USER_EMPLOYEE, false);
    }

    public String getDeviceName() {
        return getStringValue(KEY_DEVICE_NAME, "");
    }

    public void setDeviceName(String value) {
        setStringValue(KEY_DEVICE_NAME, value);
    }

    public boolean getSleepModeEnabled() {
        return getBooleanValue(KEY_SLEEP_MODE_ENABLED, false);
    }

    public void setAlwaysOnScreenModeEnabled(boolean enabled) {
        setBooleanValue(KEY_SLEEP_MODE_ENABLED, enabled);
    }

    public boolean autoRoughnessDetectionEnabled() {
        return getBooleanValue(KEY_AUTO_ROUGHNESS_DETECTION, false);
    }

    public void setAutoRoughnessDetectionEnabled(boolean enabled) {
        setBooleanValue(KEY_AUTO_ROUGHNESS_DETECTION, enabled);
    }

    public enum SYNC_MODES { //  order is important
        AUTO,
        WIFI,
        MANUAL
    }

    public enum SUSPENSION_TYPES {
        SOFT,  // 0
        MEDIUM, // 1
        HARD,  // 2
    }

    public enum EXPORT_TYPES {
        LOCAL,  // 0
        DROPBOX, // 1
    }


    public enum MEASUREMENT_TYPES {
        INTERVAL,  // 0
        BUMP, // 1
        //TAG,  // 2
    }

    public enum TIMEINTERVAL {

        MS_100(100),
        MS_200(200),
        MS_300(300),
        MS_400(400),
        MS_500(500),
        MS_600(600),
        MS_700(700),
        MS_800(800),
        MS_900(900),
        MS_1000(1000);

        private int timeInterval;

        TIMEINTERVAL(int interval) {
            timeInterval = interval;
        }

        public int getTimeInterval() {
            return timeInterval;
        }

        @Override
        public String toString() {
            return RAApplication.getInstance().getApplicationContext().getString(R.string.ms_format, String.valueOf(timeInterval));
        }
    }

    public void setSuspensionType(int type) {
        if (type >= SUSPENSION_TYPES.values().length) {
            type = 0;
        }
        setIntValue(SUSPENSION_TYPE, type);
    }

    public void setExportType(int type) {
        if (type >= EXPORT_TYPES.values().length) {
            type = 0;
        }
        setIntValue(EXPORT_TYPE, type);
    }

    public void setLess50SpeedThresholds(float thresholds) {

        setFloatValue(THRESHOLDS_PREF, thresholds);
    }

    public void setMore50SpeedThresholds(float thresholds) {
        setFloatValue(THRESHOLDS_MORE_PREF, thresholds);
    }

    public void setTimeInterval(int timeInterval) {
        if (timeInterval >= TIMEINTERVAL.values().length) {
            timeInterval = 6;
        }
        setIntValue(TIME_INTERVAL, timeInterval);
    }

    public SUSPENSION_TYPES getSuspensionType() {
        return SUSPENSION_TYPES.values()[getIntValue(SUSPENSION_TYPE, 1)];
    }

    public float getLess50SpeedThresholds() {
        return getFloatValue(THRESHOLDS_PREF, 0.2f);
    }

    public float getMore50SpeedThresholds() {
        return getFloatValue(THRESHOLDS_MORE_PREF, 0.3f);
    }

    public void clearLess50SpeedThresholds() {
        clear(THRESHOLDS_PREF);
    }

    public void clearMore50SpeedThresholds() {
        clear(THRESHOLDS_MORE_PREF);
    }

    public TIMEINTERVAL getTimeInterval() {
        return TIMEINTERVAL.values()[getIntValue(TIME_INTERVAL, 6)];
    }

    public int getSyncModeAsInt() {
        return getIntValue(SYNC_MODE, 1);
    }

    public void setShowHelp(final boolean value) {
        setBooleanValue(SHOW_HELP, value);
    }

    public boolean isShowLogin() {
        return getBooleanValue(SHOW_LOGIN, true);
    }

    public void setShowLogin(final boolean value) {
        setBooleanValue(SHOW_LOGIN, value);
    }

    public boolean isShowHelp() {
        return getBooleanValue(SHOW_HELP, true);
    }

    public int increaseAndGetExperimentNumber() {
        int number = getIntValue(EXPERIMENT_NUMBER, 0);
        number++;
        setIntValue(EXPERIMENT_NUMBER, number);
        return number;
    }

    public void incExportId() {
        int exportId = PreferencesUtil.getInstance().getIntValue(Constants.DROPBOX_EXPORT_ID_KEY, 0);
        PreferencesUtil.getInstance().setIntValue(Constants.DROPBOX_EXPORT_ID_KEY, ++exportId);
    }

    public float getSettingsValue(SETTINGS settingsEntry) {
        TypedValue outValue = new TypedValue();
        context.getResources().getValue(settingsEntry.getDefaultValueResource(), outValue, true);
        float defaultValue = outValue.getFloat();
        return getFloatValue(settingsEntry.name(), defaultValue);
    }

    public void setSettingsValue(SETTINGS settingsEntry, float value) {
        setFloatValue(settingsEntry.name(), value);
    }

    public void clear(String key) {
        SharedPreferences preferences = getSharedPreferences(context);
        preferences.edit().remove(key).commit();
    }

    public float getFloatValue(String key, float defaultValue) {
        SharedPreferences preferences = getSharedPreferences(context);
        return preferences.getFloat(key, defaultValue);
    }

    public void setFloatValue(String key, float value) {
        SharedPreferences preferences = getSharedPreferences(context);
        Editor editor = preferences.edit();
        editor.putFloat(key, value);
        editor.commit();
    }

    public long getLongValue(String key, long defaultValue) {
        SharedPreferences preferences = getSharedPreferences(context);
        return preferences.getLong(key, defaultValue);
    }

    public void setLongValue(String key, long value) {
        SharedPreferences preferences = getSharedPreferences(context);
        Editor editor = preferences.edit();
        editor.putLong(key, value);
        editor.commit();
    }

    public void setIntValue(String key, int value) {
        SharedPreferences preferences = getSharedPreferences(context);
        Editor editor = preferences.edit();
        editor.putInt(key, value);
        editor.commit();
    }


    public int getIntValue(String key, int defaultValue) {
        SharedPreferences preferences = getSharedPreferences(context);
        return preferences.getInt(key, defaultValue);
    }

    public void setStringValue(SharedPreferences preferences, String key, String value) {
        Editor editor = preferences.edit();
        editor.putString(key, value);
        editor.commit();
    }

    public void setStringValue(String key, String value) {
        SharedPreferences preferences = getSharedPreferences(context);
        setStringValue(preferences, key, value);
    }

    public String getStringValue(String key) {
        SharedPreferences preferences = getSharedPreferences(context);
        return preferences.getString(key, null);
    }

    public String getStringValue(String key, String defValue) {
        SharedPreferences preferences = getSharedPreferences(context);
        return getStringValue(preferences, key, defValue);
    }

    public String getStringValue(SharedPreferences preferences, String key, String defValue) {
        return preferences.getString(key, defValue);
    }

    public boolean getBooleanValue(String key, boolean defaultValue) {
        SharedPreferences preferences = getSharedPreferences(context);
        return preferences.getBoolean(key, defaultValue);
    }

    public void setBooleanValue(String key, boolean value) {
        SharedPreferences preferences = getSharedPreferences(context);
        Editor editor = preferences.edit();
        editor.putBoolean(key, value);
        editor.commit();
    }

    private SharedPreferences getSharedPreferences(Context context) {
        return context.getSharedPreferences(context.getString(R.string.app_name), Context.MODE_PRIVATE);
    }

    public enum SETTINGS {

        MIN_SPEED(R.dimen.minSpeed), MAX_SPEED(R.dimen.maxSpeed), ROAD_INTERVAL_LENGTH(R.dimen.RoadIntervalLength),
        DELTA_QUALITY_TIME(R.dimen.deltaQualityTime), QUALITY1(R.dimen.Quality1),
        QUALITY2(R.dimen.Quality2), QUALITY3(R.dimen.Quality3), DELTA_BUMP_TIME(R.dimen.deltaBumpTime),
        FILTER_ACCELERATION(R.dimen.filterAcceleration), BUMP_ACCELERATION(R.dimen.bumpAcceleration),
        DELTA_TIME(R.dimen.deltaTime), DELTA_ANGLE(R.dimen.deltaAngle);

        private int defaultValueResource;

        SETTINGS(int defaultValueResource) {
            this.defaultValueResource = defaultValueResource;
        }

        public int getDefaultValueResource() {
            return defaultValueResource;
        }
    }
}
