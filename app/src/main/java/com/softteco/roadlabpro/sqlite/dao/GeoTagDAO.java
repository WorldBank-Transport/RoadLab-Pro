package com.softteco.roadlabpro.sqlite.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.softteco.roadlabpro.sqlite.DataBaseHelper;
import com.softteco.roadlabpro.sqlite.model.GeoTagModel;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Aleksey on 17.04.2015.
 */
public class GeoTagDAO extends BaseDAO {

    public GeoTagDAO(final Context context) {
        super(context);
    }

    // columns of the geo tags table
    public static final String COLUMN_GEO_TAG_ID = RecordDAO.COLUMN_RECORD_ID;
    public static final String COLUMN_GEO_TAG_FOLDER_ID = "folder_id";
    public static final String COLUMN_GEO_TAG_ROAD_ID = "road_id";
    public static final String COLUMN_GEO_TAG_MEASUREMENT_ID = "measurement_id";
    public static final String COLUMN_GEO_TAG_TIME = "time";
    public static final String COLUMN_GEO_TAG_SPEED = "speed";
    public static final String COLUMN_GEO_TAG_DISTANCE = "distance";
    public static final String COLUMN_GEO_TAG_LATITUDE = "latitude";
    public static final String COLUMN_GEO_TAG_LONGITUDE = "longitude";
    public static final String COLUMN_GEO_TAG_ALTITUDE = "altitude";
    public static final String COLUMN_GEO_TAG_UPLOADED = "uploaded";

    private String[] allColumns = {
        COLUMN_GEO_TAG_ID,
        COLUMN_GEO_TAG_FOLDER_ID,
        COLUMN_GEO_TAG_ROAD_ID,
        COLUMN_GEO_TAG_MEASUREMENT_ID,
        COLUMN_GEO_TAG_TIME,
        COLUMN_GEO_TAG_SPEED,
        COLUMN_GEO_TAG_DISTANCE,
        COLUMN_GEO_TAG_LATITUDE,
        COLUMN_GEO_TAG_LONGITUDE,
        COLUMN_GEO_TAG_ALTITUDE,
        COLUMN_GEO_TAG_UPLOADED
    };

    public static final String SQL_CREATE_TABLE_GEO_TAGS = "CREATE TABLE " + DataBaseHelper.TABLE_GEO_TAGS + "("
            + COLUMN_GEO_TAG_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + COLUMN_GEO_TAG_FOLDER_ID + " INTEGER NOT NULL, "
            + COLUMN_GEO_TAG_ROAD_ID + " INTEGER NOT NULL, "
            + COLUMN_GEO_TAG_MEASUREMENT_ID + " INTEGER NOT NULL, "
            + COLUMN_GEO_TAG_TIME + " INTEGER NOT NULL, "
            + COLUMN_GEO_TAG_SPEED + " REAL NOT NULL, "
            + COLUMN_GEO_TAG_DISTANCE + " REAL NOT NULL, "
            + COLUMN_GEO_TAG_LATITUDE + " REAL NOT NULL, "
            + COLUMN_GEO_TAG_LONGITUDE + " REAL NOT NULL, "
            + COLUMN_GEO_TAG_ALTITUDE + " REAL NOT NULL, "
            + COLUMN_GEO_TAG_UPLOADED + " INTEGER NOT NULL DEFAULT (0) "
            + ");";

    public static final String SQL_DROP_TABLE_GEO_TAGS = "DROP TABLE IF EXISTS " + DataBaseHelper.TABLE_GEO_TAGS;

    public long putGeoTag(final GeoTagModel data) {
        long insertId = -1;
        SQLiteDatabase db = getDatabase();
        try {
            db.beginTransaction();
            final ContentValues values = getContentValues(data);
            insertId = db.insert(DataBaseHelper.TABLE_GEO_TAGS, null, values);
            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.e(TAG, e != null && e.getMessage() != null ? e.getMessage() : "");
        } finally {
            db.endTransaction();
        }
        return insertId;
    }

    private ContentValues getContentValues(final GeoTagModel data) {
        final ContentValues values = new ContentValues();
        values.put(COLUMN_GEO_TAG_FOLDER_ID, data.getFolderId());
        values.put(COLUMN_GEO_TAG_ROAD_ID, data.getRoadId());
        values.put(COLUMN_GEO_TAG_MEASUREMENT_ID, data.getMeasurementId());
        values.put(COLUMN_GEO_TAG_TIME, data.getTime());
        values.put(COLUMN_GEO_TAG_SPEED, data.getSpeed());
        values.put(COLUMN_GEO_TAG_DISTANCE, data.getDistance());
        values.put(COLUMN_GEO_TAG_LATITUDE, data.getLatitude());
        values.put(COLUMN_GEO_TAG_LONGITUDE, data.getLongitude());
        values.put(COLUMN_GEO_TAG_ALTITUDE, data.getAltitude());
        values.put(COLUMN_GEO_TAG_UPLOADED, data.isUploaded() ? 1 : 0);
        return values;
    }

    public void putGeoTagList(final List<GeoTagModel> items) {
        putGeoTagsList(items, -1, -1, -1);
    }

    public void putGeoTagsList(final List<GeoTagModel> items,
        final long folderId, final long roadId, final long measurementId) {
        for (GeoTagModel d : items) {
            if (folderId >= 0) {
                d.setFolderId(folderId);
            }
            if (roadId >= 0) {
                d.setRoadId(roadId);
            }
            if (measurementId >= 0) {
                d.setMeasurementId(measurementId);
            }
            putGeoTag(d);
        }
    }

    public long getAllItemsCount(long folderId, long roadId, long measurementId) {
        String queryStr = getQueryStr(folderId, roadId, measurementId);
        return getAllItemsCountForId(DataBaseHelper.TABLE_GEO_TAGS, queryStr);
    }

    public double getOverallDistance(long folderId, long roadId, long measurementId) {
        double distance = getSum(folderId, roadId, measurementId,
               COLUMN_GEO_TAG_DISTANCE, DataBaseHelper.TABLE_GEO_TAGS);
        return distance;
    }

    public Cursor getItemsCursor(long folderId, long roadId, long measurementId) {
        String queryStr = getQueryStr(folderId, roadId, measurementId);
        Cursor cursor = null;
        try {
            cursor = getDatabase().query(DataBaseHelper.TABLE_GEO_TAGS,
            allColumns, queryStr, null, null, null, null);
            cursor.moveToFirst();
        } catch (Exception e) {
            Log.e(TAG, e != null && e.getMessage() != null ? e.getMessage() : "");
        }
        return cursor;
    }

    public void moveGeoTags(long folderId, long roadId, long measurementId) {
        Cursor cursor = getItemsCursor(-1, -1, measurementId);
        if (cursor != null) {
            boolean moveToNext = cursor.getCount() > 0;
            GeoTagModel geoTag = null;
            while(moveToNext) {
                geoTag = cursorToRecord(cursor);
                if (folderId >= 0) {
                    geoTag.setFolderId(folderId);
                }
                if (roadId >= 0) {
                    geoTag.setRoadId(roadId);
                }
                if (measurementId >= 0) {
                    geoTag.setMeasurementId(measurementId);
                }
                updateItem(geoTag);
                moveToNext = cursor.moveToNext();
            }
            cursor.close();
        }
    }

    public void deleteItemsWithMeasurementId(final long measurementId) {
        SQLiteDatabase db = getDatabase();
        try {
            db.beginTransaction();
            db.delete(DataBaseHelper.TABLE_GEO_TAGS, COLUMN_GEO_TAG_MEASUREMENT_ID + " = " + measurementId, null);
            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.e(TAG, e != null && e.getMessage() != null ? e.getMessage() : "");
        } finally {
            db.endTransaction();
        }
    }

    public int delete(final GeoTagModel data) {
        long id = data.getId();
        int count = 0;
        SQLiteDatabase db = getDatabase();
        try {
            db.beginTransaction();
            count = db.delete(DataBaseHelper.TABLE_GEO_TAGS, COLUMN_GEO_TAG_ID + " = " + id, null);
            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.e(TAG, e != null && e.getMessage() != null ? e.getMessage() : "");
        } finally {
            db.endTransaction();
        }
        return count;
    }

    public Cursor getGeoTagsByMeasurementIdCursor(final long measurementId) {
        final Cursor cursor = getDatabase().query(DataBaseHelper.TABLE_GEO_TAGS, allColumns,
            COLUMN_GEO_TAG_MEASUREMENT_ID + " = ?",
            new String[]{String.valueOf(measurementId)}, null, null, null);
        cursor.moveToFirst();
        return cursor;
    }

    public List<GeoTagModel> getGeoTagsByMeasurementId(final long id) {
        List<GeoTagModel> items = new ArrayList<GeoTagModel>();
        try {
            final Cursor cursor = getGeoTagsByMeasurementIdCursor(id);
            GeoTagModel record = null;
            boolean moveToNext = cursor.getCount() > 0;
            while(moveToNext) {
                record = cursorToRecord(cursor);
                items.add(record);
                moveToNext = cursor.moveToNext();
            }
        } catch (Exception e) {
            Log.e(TAG, e != null && e.getMessage() != null ? e.getMessage() : "");
        }
        return items;
    }

    public long updateItem(final GeoTagModel data) {
        long insertId = -1;
        SQLiteDatabase db = getDatabase();
        try {
            db.beginTransaction();
            ContentValues values = getContentValues(data);
            insertId = getDatabase().update(DataBaseHelper.TABLE_GEO_TAGS, values,
            COLUMN_GEO_TAG_ID + "=?", new String[]{String.valueOf(data.getId())});
            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.e(TAG, e != null && e.getMessage() != null ? e.getMessage() : "");
        } finally {
            db.endTransaction();
        }
        return insertId;
    }

    public GeoTagModel cursorToRecord(final Cursor cursor) {
        final GeoTagModel data = new GeoTagModel();
        data.setId(cursor.getLong(cursor.getColumnIndex(RecordDAO.COLUMN_RECORD_ID)));
        data.setFolderId(cursor.getLong(cursor.getColumnIndex(COLUMN_GEO_TAG_FOLDER_ID)));
        data.setRoadId(cursor.getLong(cursor.getColumnIndex(COLUMN_GEO_TAG_ROAD_ID)));
        data.setMeasurementId(cursor.getLong(cursor.getColumnIndex(COLUMN_GEO_TAG_MEASUREMENT_ID)));
        data.setTime(cursor.getLong(cursor.getColumnIndex(COLUMN_GEO_TAG_TIME)));
        data.setSpeed(cursor.getFloat(cursor.getColumnIndex(COLUMN_GEO_TAG_SPEED)));
        data.setDistance(cursor.getDouble(cursor.getColumnIndex(COLUMN_GEO_TAG_DISTANCE)));
        data.setLatitude(cursor.getDouble(cursor.getColumnIndex(COLUMN_GEO_TAG_LATITUDE)));
        data.setLongitude(cursor.getDouble(cursor.getColumnIndex(COLUMN_GEO_TAG_LONGITUDE)));
        data.setAltitude(cursor.getDouble(cursor.getColumnIndex(COLUMN_GEO_TAG_ALTITUDE)));
        data.setUploaded(cursor.getInt(cursor.getColumnIndex(COLUMN_GEO_TAG_UPLOADED)) == 1);
        return data;
    }

    public Cursor getAllGeoTagsCursor() {
        return getDatabase().query(DataBaseHelper.TABLE_GEO_TAGS, allColumns, null, null, null, null, null);
    }
}
