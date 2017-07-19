package com.softteco.roadlabpro.util;

/**
 * Created by Aleksey on 06.05.2015.
 */
public class Constants {

    public static final double LATITUDE_BELARUS = 53.9167;
    public static final double LONGITUDE_BELARUS = 27.5500;
    public static final float DEFAULT_CAMERA_ZOOM = 5.5f;
    public static final float DEFAULT_LOCATION_FOUND_CAMERA_ZOOM = 13.0f;

    public static final String GOOGLE_TOKEN_URL = "https://accounts.google.com/o/oauth2/token";
    public static final String GOOGLE_BASE_URL = "https://maps.googleapis.com";
    public static final String GOOGLE_API_BASE_URL = "https://www.googleapis.com";
    public static final String USER_AGENT_HEADER = "User-Agent";
    public static final String USER_AGENT_HEADER_VALUE = "Chrome/41.0.2228.0";

    public static final String ACTION_DEVICE_NOT_FIXED = "com.softteco.roadlabpro.ACTION_DEVICE_NOT_FIXED";
    public static final String ACTION_DEVICE_FIXED = "com.softteco.roadlabpro.ACTION_DEVICE_FIXED";
    public static final String DROPBOX_ACCESS_TOKEN  = "ppdYKV1X-VAAAAAAAAAACPNRth-gM9tymLkvHH89aeZZp9bjdou3rGFZ0GKjD4Z7";
    public static final String DROPBOX_DEFAULT_FOLDER = "RoadLabPro";
    public static final String DROPBOX_APPLICATION_KEY  = "msomsfp2vv5fmfh";
    public static final String DROPBOX_ACCESS_PREF_KEY = "dropbox_access_pref_key";
    public static final String DROPBOX_ACCESS_TOKEN_KEY = "dropbox_access_token_key";
    public static final String DROPBOX_EXPORT_ID_KEY = "export_id";

    public static final String EXPORT_ITEM_NAME_TAGS_FORMAT = "T%03d-%s-%s";
    public static final String NEW_TAG_SCREN_TITLE_FORMAT = "T%03d - ";
    public static final String FLOAT_FORMAT_NO_FRACTION = "%2.0f";
    public static final String PERCENTS_FLOAT_FORMAT = FLOAT_FORMAT_NO_FRACTION + " %%";

    public static final String DROPBOX_ACCOUNT_USER_NAME_KEY = "dropbox_user_name";
    public static final String GOOGLE_ACCOUNT_USER_NAME_KEY = "google_user_name";
    public static final String TEMPORAL_AUDIO_FILE = "temporalAudioFile.mp3";
    public static final String PROJECT_SUMMARY = "ProjectSummary";
    public static final String ROAD_INTERVALS = "RoadIntervals";
    public static final String BUMPS = "Bumps";
    public static final String TAGS = "Tags";
    public static final String TAG_MEDIA = "TagMedia";
    public static final String GEO_TAGS = "RoadPath";

    public static final String GEO_TAG_SUFFIX = "-gps";
    public static final String KML_EXT = ".kml";
    public static final String CSV_EXT = ".csv";
    public static final String JPG_EXT = ".jpg";
    public static final String MP3_EXT = ".mp3";

    public static final String TAG_KML_ICON_PATH = "http://maps.google.com/mapfiles/ms/micons/";
    public static final String TAG_KML_ICON_GREEN = "green.png";
    public static final String TAG_KML_ICON_LIGHT_BLUE = "lightblue.png";
    public static final String TAG_KML_ICON_ORANGE = "orange.png";
    public static final String TAG_KML_ICON_RED = "red.png";
    public static final String TAG_KML_ICON_YELLOW = "yellow.png";
    public static final String BUMP_KML_ICON = "http://maps.google.com/mapfiles/kml/pal3/icon45.png";

    public static final String NO_DATA = "no_data";
    public static final String OK_DATA = "ok_data";

    public static final int DIALOG_TYPE_DEVICE_WARNING = 0;
    public static final int DIALOG_TYPE_GPS_WARNING = 1;

    public static final float INTERVAL_LENGTH = 100f;
}
