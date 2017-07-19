package com.softteco.roadlabpro.sqlite.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.softteco.roadlabpro.algorithm.RoadQuality;
import com.softteco.roadlabpro.sqlite.DataBaseHelper;
import com.softteco.roadlabpro.sqlite.model.BaseModel;
import com.softteco.roadlabpro.sqlite.model.ProcessedDataModel;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ppp on 15.04.2015.
 */
public class ProcessedDataDAO extends BaseDAO implements UploadDAO {

    public static final String TAG = RecordDAO.class.getName();
    private static final int MAX_COORDS_SIZE = 3;

    // columns of the record table
    public static final String COLUMN_PROCESSED_DATA_ID = RecordDAO.COLUMN_RECORD_ID;
    public static final String COLUMN_PROCESSED_DATA_RECORD_ID = "record_id";
    public static final String COLUMN_PROCESSED_DATA_FOLDER_ID = "folder_id";
    public static final String COLUMN_PROCESSED_DATA_ROAD_ID = "road_id";
    public static final String COLUMN_PROCESSED_DATA_MEASUREMENT_ID = "measurement_id";
    public static final String COLUMN_PROCESSED_DATA_TIME = "time";
    public static final String COLUMN_PROCESSED_DATA_SPEED = "speed";
    public static final String COLUMN_PROCESSED_DATA_BUMPS = "bumps";
    public static final String COLUMN_PROCESSED_DATA_CATEGORY = "category";
    public static final String COLUMN_PROCESSED_DATA_STD_DEVIATION = "std_deviation";
    public static final String COLUMN_PROCESSED_DATA_START_LATITUDE = "start_latitude";
    public static final String COLUMN_PROCESSED_DATA_START_LONGITUDE = "start_longitude";
    public static final String COLUMN_PROCESSED_DATA_START_ALTITUDE = "start_altitude";
    public static final String COLUMN_PROCESSED_DATA_END_LATITUDE = "end_latitude";
    public static final String COLUMN_PROCESSED_DATA_END_LONGITUDE = "end_longitude";
    public static final String COLUMN_PROCESSED_DATA_END_ALTITUDE = "end_altitude";
    public static final String COLUMN_PROCESSED_DATA_UPLOADED = "uploaded";
    public static final String COLUMN_PROCESSED_DATA_PENDING = "pending";
    public static final String COLUMN_PROCESSED_DATA_VERTICAL_ACCELERATION = "av";
    public static final String COLUMN_PROCESSED_DATA_SESSION_ID = "session";
    public static final String COLUMN_PROCESSED_DATA_ITEMS_CNT = "items_cnt";
    public static final String COLUMN_PROCESSED_DATA_IS_FIXED = "is_fixed";
    public static final String COLUMN_PROCESSED_DATA_IRI = "iri";
    public static final String COLUMN_PROCESSED_DATA_SUSPENSION = "suspension";
    public static final String COLUMN_PROCESSED_DATA_DISTANCE = "distance";

    private String[] allColumns = {
            COLUMN_PROCESSED_DATA_ID,
            COLUMN_PROCESSED_DATA_FOLDER_ID,
            COLUMN_PROCESSED_DATA_ROAD_ID,
            COLUMN_PROCESSED_DATA_MEASUREMENT_ID,
            COLUMN_PROCESSED_DATA_TIME,
            COLUMN_PROCESSED_DATA_SPEED,
            COLUMN_PROCESSED_DATA_BUMPS,
            COLUMN_PROCESSED_DATA_CATEGORY,
            COLUMN_PROCESSED_DATA_STD_DEVIATION,
            COLUMN_PROCESSED_DATA_START_LATITUDE,
            COLUMN_PROCESSED_DATA_START_LONGITUDE,
            COLUMN_PROCESSED_DATA_START_ALTITUDE,
            COLUMN_PROCESSED_DATA_END_LATITUDE,
            COLUMN_PROCESSED_DATA_END_LONGITUDE,
            COLUMN_PROCESSED_DATA_END_ALTITUDE,
            COLUMN_PROCESSED_DATA_UPLOADED,
            COLUMN_PROCESSED_DATA_PENDING,
            COLUMN_PROCESSED_DATA_RECORD_ID,
            COLUMN_PROCESSED_DATA_VERTICAL_ACCELERATION,
            COLUMN_PROCESSED_DATA_SESSION_ID,
            COLUMN_PROCESSED_DATA_ITEMS_CNT,
            COLUMN_PROCESSED_DATA_IS_FIXED,
            COLUMN_PROCESSED_DATA_IRI,
            COLUMN_PROCESSED_DATA_SUSPENSION,
            COLUMN_PROCESSED_DATA_DISTANCE
    };

    public static final String SQL_CREATE_TABLE_PROCESSED_DATA = "CREATE TABLE " + DataBaseHelper.TABLE_PROCESSED_DATA + "("
            + COLUMN_PROCESSED_DATA_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + COLUMN_PROCESSED_DATA_FOLDER_ID + " INTEGER NOT NULL, "
            + COLUMN_PROCESSED_DATA_ROAD_ID + " INTEGER NOT NULL, "
            + COLUMN_PROCESSED_DATA_MEASUREMENT_ID + " INTEGER NOT NULL, "
            + COLUMN_PROCESSED_DATA_TIME + " INTEGER NOT NULL, "
            + COLUMN_PROCESSED_DATA_SPEED + " REAL NOT NULL, "
            + COLUMN_PROCESSED_DATA_BUMPS + " INTEGER NOT NULL, "
            + COLUMN_PROCESSED_DATA_CATEGORY + " INTEGER NOT NULL, "
            + COLUMN_PROCESSED_DATA_STD_DEVIATION + " REAL NOT NULL, "
            + COLUMN_PROCESSED_DATA_START_LATITUDE + " REAL NOT NULL, "
            + COLUMN_PROCESSED_DATA_START_LONGITUDE + " REAL NOT NULL, "
            + COLUMN_PROCESSED_DATA_START_ALTITUDE + " REAL NOT NULL, "
            + COLUMN_PROCESSED_DATA_END_LATITUDE + " REAL NOT NULL, "
            + COLUMN_PROCESSED_DATA_END_LONGITUDE + " REAL NOT NULL, "
            + COLUMN_PROCESSED_DATA_END_ALTITUDE + " REAL NOT NULL, "
            + COLUMN_PROCESSED_DATA_UPLOADED + " INTEGER NOT NULL DEFAULT (0), "
            + COLUMN_PROCESSED_DATA_PENDING + " INTEGER NOT NULL DEFAULT (0), "
            + COLUMN_PROCESSED_DATA_RECORD_ID + " INTEGER NOT NULL, "
            + COLUMN_PROCESSED_DATA_VERTICAL_ACCELERATION + " REAL NOT NULL, "
            + COLUMN_PROCESSED_DATA_SESSION_ID + " INTEGER NOT NULL, "
            + COLUMN_PROCESSED_DATA_ITEMS_CNT + " INTEGER NOT NULL, "
            + COLUMN_PROCESSED_DATA_IS_FIXED + " INTEGER NOT NULL DEFAULT (0), "
            + COLUMN_PROCESSED_DATA_IRI + " REAL NOT NULL, "
            + COLUMN_PROCESSED_DATA_SUSPENSION + " INTEGER NOT NULL, "
            + COLUMN_PROCESSED_DATA_DISTANCE + " REAL NOT NULL"
            + ");";

    public static final String SQL_DROP_TABLE_PROCESSED_DATA = "DROP TABLE IF EXISTS " + DataBaseHelper.TABLE_PROCESSED_DATA;
    public static final String SQL_DELETE_ALL_RECORDS_FROM_TABLE_PROCESSED_DATA = "DELETE FROM " + DataBaseHelper.TABLE_PROCESSED_DATA;

    public ProcessedDataDAO(final Context context) {
        super(context);
    }

    public long putProcessedData(final ProcessedDataModel data) {
        SQLiteDatabase db = getDatabase();
        long insertId = -1;
        try {
            db.beginTransaction();
            final ContentValues values = getContentValues(data);
            insertId = getDatabase().insert(DataBaseHelper.TABLE_PROCESSED_DATA, null, values);
            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.e(TAG, e != null && e.getMessage() != null ? e.getMessage() : "");
        } finally {
            db.endTransaction();
        }
        return insertId;
    }

    private ContentValues getContentValues(final ProcessedDataModel data) {
        final ContentValues values = new ContentValues();
        values.put(COLUMN_PROCESSED_DATA_FOLDER_ID, data.getFolderId());
        values.put(COLUMN_PROCESSED_DATA_ROAD_ID, data.getRoadId());
        values.put(COLUMN_PROCESSED_DATA_MEASUREMENT_ID, data.getMeasurementId());
        values.put(COLUMN_PROCESSED_DATA_TIME, data.getTime());
        values.put(COLUMN_PROCESSED_DATA_SPEED, data.getSpeed());
        values.put(COLUMN_PROCESSED_DATA_BUMPS, data.getBumps());
        values.put(COLUMN_PROCESSED_DATA_CATEGORY, data.getCategory() != null ? data.getCategory().getId() : RoadQuality.POOR.getId());
        values.put(COLUMN_PROCESSED_DATA_STD_DEVIATION, data.getStdDeviation());
        values.put(COLUMN_PROCESSED_DATA_VERTICAL_ACCELERATION, data.getVerticalAcceleration());
        values.put(COLUMN_PROCESSED_DATA_SESSION_ID, data.getSession());
        values.put(COLUMN_PROCESSED_DATA_ITEMS_CNT, data.getItemsCount());
        values.put(COLUMN_PROCESSED_DATA_IS_FIXED, data.isFixed() ? 1 : 0);
        values.put(COLUMN_PROCESSED_DATA_IRI, data.getIri());
        values.put(COLUMN_PROCESSED_DATA_SUSPENSION, data.getSuspension());
        values.put(COLUMN_PROCESSED_DATA_DISTANCE, data.getDistance());
        double[] coordsStart = data.getCoordsStart();
        double latStart = 0;
        double lonStart = 0;
        double altStart = 0;
        if (coordsStart != null && coordsStart.length == MAX_COORDS_SIZE) {
            latStart = coordsStart[0];
            lonStart = coordsStart[1];
            altStart = coordsStart[2];
        }
        values.put(COLUMN_PROCESSED_DATA_START_LATITUDE, latStart);
        values.put(COLUMN_PROCESSED_DATA_START_LONGITUDE, lonStart);
        values.put(COLUMN_PROCESSED_DATA_START_ALTITUDE, altStart);
        double[] coordsEnd = data.getCoordsEnd();
        double latEnd = 0;
        double lonEnd = 0;
        double altEnd = 0;
        if (coordsEnd != null && coordsEnd.length == MAX_COORDS_SIZE) {
            latEnd = coordsEnd[0];
            lonEnd = coordsEnd[1];
            altEnd = coordsEnd[2];
        }
        values.put(COLUMN_PROCESSED_DATA_END_LATITUDE, latEnd);
        values.put(COLUMN_PROCESSED_DATA_END_LONGITUDE, lonEnd);
        values.put(COLUMN_PROCESSED_DATA_END_ALTITUDE, altEnd);
        values.put(COLUMN_PROCESSED_DATA_RECORD_ID, data.getRecordId());
        values.put(COLUMN_PROCESSED_DATA_UPLOADED, data.isUploaded() ? 1 : 0);
        values.put(COLUMN_PROCESSED_DATA_PENDING, data.isPending() ? 1 : 0);
        return values;
    }

    public void putProcessedDataList(final List<ProcessedDataModel> items, final long recordId) {
        deleteItemsWithRecordId(recordId);
        for (ProcessedDataModel d : items) {
            d.setRecordId(recordId);
            putProcessedData(d);
        }
    }

    public void deleteItemsWithRecordId(final long recordId) {
        SQLiteDatabase db = getDatabase();
        try {
            db.beginTransaction();
            db.delete(DataBaseHelper.TABLE_PROCESSED_DATA, COLUMN_PROCESSED_DATA_RECORD_ID + " = " + recordId, null);
            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.e(TAG, e != null && e.getMessage() != null ? e.getMessage() : "");
        } finally {
            db.endTransaction();
        }
    }

    public void deleteItemsWithMeasurementId(final long measurementId) {
        SQLiteDatabase db = getDatabase();
        try {
            db.beginTransaction();
            db.delete(DataBaseHelper.TABLE_PROCESSED_DATA, COLUMN_PROCESSED_DATA_MEASUREMENT_ID + " = " + measurementId, null);
            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.e(TAG, e != null && e.getMessage() != null ? e.getMessage() : "");
        } finally {
            db.endTransaction();
        }
    }

    public double getOverallDistance(long folderId, long roadId, long measurementId) {
        double distance = getSum(folderId, roadId, measurementId,
               COLUMN_PROCESSED_DATA_DISTANCE, DataBaseHelper.TABLE_PROCESSED_DATA);
        return distance;
    }

    public double getAverageIRI(long folderId, long roadId, long measurementId) {
        double iri = 0;
        try {
            String queryStr = getQueryStr(folderId, roadId, measurementId);
            String calcDistanceQuery = "SELECT AVG(" + COLUMN_PROCESSED_DATA_IRI
                    + ") FROM " + DataBaseHelper.TABLE_PROCESSED_DATA
                    + " WHERE " + queryStr;
            final Cursor cursor = getDatabase().rawQuery(calcDistanceQuery, null);
            cursor.moveToFirst();
            iri = cursor.getFloat(0);
        } catch (Exception e) {
            Log.e(TAG, e != null && e.getMessage() != null ? e.getMessage() : "");
        }
        return iri;
    }

    public double getAverageSpeed(long folderId, long roadId, long measurementId) {
        double speed = 0;
        try {
            String queryStr = getQueryStr(folderId, roadId, measurementId);
            String calcDistanceQuery = "SELECT AVG(" + COLUMN_PROCESSED_DATA_SPEED
                    + ") FROM " + DataBaseHelper.TABLE_PROCESSED_DATA
                    + " WHERE " + queryStr;
            final Cursor cursor = getDatabase().rawQuery(calcDistanceQuery, null);
            cursor.moveToFirst();
            speed = cursor.getFloat(0);
        } catch (Exception e) {
            Log.e(TAG, e != null && e.getMessage() != null ? e.getMessage() : "");
        }
        return speed;
    }

    public Cursor getItemsCursor(long folderId, long roadId, long measurementId) {
        String queryStr = getQueryStr(folderId, roadId, measurementId);
        Cursor cursor = null;
        try {
            cursor = getDatabase().query(DataBaseHelper.TABLE_PROCESSED_DATA,
            allColumns, queryStr, null, null, null, null);
            cursor.moveToFirst();
        } catch (Exception e) {
            Log.e(TAG, e != null && e.getMessage() != null ? e.getMessage() : "");
        }
        return cursor;
    }

    public void moveProcessedData(long folderId, long roadId, long measurementId) {
        Cursor cursor = getItemsCursor(-1, -1, measurementId);
        if (cursor != null) {
            boolean moveToNext = cursor.getCount() > 0;
            ProcessedDataModel data = null;
            while(moveToNext) {
                data = cursorToRecord(cursor);
                if (folderId >= 0) {
                    data.setFolderId(folderId);
                }
                if (roadId >= 0) {
                    data.setRoadId(roadId);
                }
                if (measurementId >= 0) {
                    data.setMeasurementId(measurementId);
                }
                updateItem(data);
                moveToNext = cursor.moveToNext();
            }
            cursor.close();
        }
    }

    public long getAllItemsCount(long folderId, long roadId, long measurementId) {
        String queryStr = getQueryStr(folderId, roadId, measurementId);
        return getAllItemsCountForId(DataBaseHelper.TABLE_PROCESSED_DATA, queryStr);
    }

    public void deleteAllProcessedData() {
        SQLiteDatabase db = getDatabase();
        db.execSQL(SQL_DELETE_ALL_RECORDS_FROM_TABLE_PROCESSED_DATA);
    }

    public void deleteAllSessionProcessedData(long sessionId) {
        SQLiteDatabase db = getDatabase();
        db.delete(DataBaseHelper.TABLE_PROCESSED_DATA, COLUMN_PROCESSED_DATA_SESSION_ID + "=?", new String[]{String.valueOf(sessionId)});
    }

    public int deleteProcessedData(final ProcessedDataModel data) {
        long id = data.getId();
        int count = 0;
        SQLiteDatabase db = getDatabase();
        try {
            db.beginTransaction();
            count = db.delete(DataBaseHelper.TABLE_PROCESSED_DATA, COLUMN_PROCESSED_DATA_ID + " = " + id, null);
            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.e(TAG, e != null && e.getMessage() != null ? e.getMessage() : "");
        } finally {
            db.endTransaction();
        }
        return count;
    }

    public ProcessedDataModel getLastInterval(long measurementId) {
        Cursor c = getProcessedDataByMeasurementIdCursor(measurementId);
        ProcessedDataModel processedData = null;
        try {
            if (c != null) {
                if (c.getCount() > 0) {
                    c.moveToLast();
                    processedData = cursorToRecord(c);
                }
                c.close();
            }
        } catch (Exception e) {
            Log.e(TAG, e != null && e.getMessage() != null ? e.getMessage() : "");
        }
        return processedData;
    }

    public ProcessedDataModel getProcessedDataById(final long id) {
        final Cursor cursor = getProcessedDataByRecordIdCursor(id);
        final ProcessedDataModel record = cursorToRecord(cursor);
        return record;
    }

    public Cursor getProcessedDataBySessionIdCursor(final long sessionId) {
        final Cursor cursor = getDatabase().query(DataBaseHelper.TABLE_PROCESSED_DATA, allColumns,
                COLUMN_PROCESSED_DATA_SESSION_ID + " = ?",
                new String[]{String.valueOf(sessionId)}, null, null, null);
        cursor.moveToFirst();
        return cursor;
    }

    public Cursor getProcessedDataForId(long folderId, long roadId, long measurementId) {
        Cursor cursor = null;
        try {
            String queryStr = getQueryStr(folderId, roadId, measurementId);
            cursor = getDatabase().query(DataBaseHelper.TABLE_PROCESSED_DATA, allColumns,
            queryStr, null, null, null, null);
            cursor.moveToFirst();
        } catch (Exception e) {
            Log.e(TAG, e != null && e.getMessage() != null ? e.getMessage() : "");
        }
        return cursor;
    }

    public Cursor getProcessedDataByRecordIdCursor(final long recordId) {
        final Cursor cursor = getDatabase().query(DataBaseHelper.TABLE_PROCESSED_DATA, allColumns,
                COLUMN_PROCESSED_DATA_RECORD_ID + " = ?",
                new String[]{String.valueOf(recordId)}, null, null, null);
        cursor.moveToFirst();
        return cursor;
    }

    public Cursor getProcessedDataByMeasurementIdCursor(final long measurementId) {
        final Cursor cursor = getDatabase().query(DataBaseHelper.TABLE_PROCESSED_DATA, allColumns,
                COLUMN_PROCESSED_DATA_MEASUREMENT_ID + " = ?",
                new String[]{String.valueOf(measurementId)}, null, null, null);
        cursor.moveToFirst();
        return cursor;
    }

    public List<ProcessedDataModel> getProcessedDataByMeasurementId(final long id) {
        final Cursor cursor = getProcessedDataByMeasurementIdCursor(id);
        List<ProcessedDataModel> items = new ArrayList<ProcessedDataModel>();
        ProcessedDataModel record = null;
        if (cursor != null) {
            boolean moveToNext = cursor.getCount() > 0;
            while(moveToNext) {
                record = cursorToRecord(cursor);
                items.add(record);
                moveToNext = cursor.moveToNext();
            }
            cursor.close();
        }
        return items;
    }

    public long updateItem(final ProcessedDataModel data) {
        long insertId = -1;
        SQLiteDatabase db = getDatabase();
        try {
            db.beginTransaction();
            ContentValues values = getContentValues(data);
            insertId = getDatabase().update(DataBaseHelper.TABLE_PROCESSED_DATA, values,
            COLUMN_PROCESSED_DATA_ID + "=?", new String[]{String.valueOf(data.getId())});
            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.e(TAG, e != null && e.getMessage() != null ? e.getMessage() : "");
        } finally {
            db.endTransaction();
        }
        return insertId;
    }

    public ProcessedDataModel cursorToRecord(final Cursor cursor) {
        final ProcessedDataModel data = new ProcessedDataModel();
        data.setFolderId(cursor.getLong(cursor.getColumnIndex(COLUMN_PROCESSED_DATA_FOLDER_ID)));
        data.setRoadId(cursor.getLong(cursor.getColumnIndex(COLUMN_PROCESSED_DATA_ROAD_ID)));
        data.setMeasurementId(cursor.getLong(cursor.getColumnIndex(COLUMN_PROCESSED_DATA_MEASUREMENT_ID)));
        data.setId(cursor.getLong(cursor.getColumnIndex(RecordDAO.COLUMN_RECORD_ID)));
        data.setTime(cursor.getLong(cursor.getColumnIndex(COLUMN_PROCESSED_DATA_TIME)));
        data.setSpeed(cursor.getFloat(cursor.getColumnIndex(COLUMN_PROCESSED_DATA_SPEED)));
        data.setBumps(cursor.getInt(cursor.getColumnIndex(COLUMN_PROCESSED_DATA_BUMPS)));
        int roadQualityId = cursor.getInt(cursor.getColumnIndex(COLUMN_PROCESSED_DATA_CATEGORY));
        data.setCategory(RoadQuality.getRoadQuality(roadQualityId));
        data.setStdDeviation(cursor.getFloat(cursor.getColumnIndex(COLUMN_PROCESSED_DATA_STD_DEVIATION)));
        data.setVerticalAccleration(cursor.getFloat(cursor.getColumnIndex(COLUMN_PROCESSED_DATA_VERTICAL_ACCELERATION)));
        data.setSession(cursor.getLong(cursor.getColumnIndex(COLUMN_PROCESSED_DATA_SESSION_ID)));
        data.setItemsCount(cursor.getInt(cursor.getColumnIndex(COLUMN_PROCESSED_DATA_ITEMS_CNT)));
        data.setFixed(cursor.getInt(cursor.getColumnIndex(COLUMN_PROCESSED_DATA_IS_FIXED)) == 1);
        data.setIri(cursor.getFloat(cursor.getColumnIndex(COLUMN_PROCESSED_DATA_IRI)));
        data.setDistance(cursor.getFloat(cursor.getColumnIndex(COLUMN_PROCESSED_DATA_DISTANCE)));
        data.setCoordsStart(
                cursor.getDouble(cursor.getColumnIndex(COLUMN_PROCESSED_DATA_START_LATITUDE)),
                cursor.getDouble(cursor.getColumnIndex(COLUMN_PROCESSED_DATA_START_LONGITUDE)),
                cursor.getDouble(cursor.getColumnIndex(COLUMN_PROCESSED_DATA_START_ALTITUDE)));
        data.setCoordsEnd(
                cursor.getDouble(cursor.getColumnIndex(COLUMN_PROCESSED_DATA_END_LATITUDE)),
                cursor.getDouble(cursor.getColumnIndex(COLUMN_PROCESSED_DATA_END_LONGITUDE)),
                cursor.getDouble(cursor.getColumnIndex(COLUMN_PROCESSED_DATA_END_ALTITUDE)));
        data.setUploaded(cursor.getInt(cursor.getColumnIndex(COLUMN_PROCESSED_DATA_UPLOADED)) == 1);
        data.setPending(cursor.getInt(cursor.getColumnIndex(COLUMN_PROCESSED_DATA_PENDING)) == 1);
        data.setRecordId(cursor.getLong(cursor.getColumnIndex(COLUMN_PROCESSED_DATA_RECORD_ID)));
        data.setSuspension(cursor.getInt(cursor.getColumnIndex(COLUMN_PROCESSED_DATA_SUSPENSION)));
        return data;
    }

    @Override
    public List<ProcessedDataModel> getDataForUpload(final String query) {
        final List<ProcessedDataModel> listData = new ArrayList<>();
        final Cursor cursor = getProcessedDataForUploadCursor(query);
        if (cursor != null) {
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                final ProcessedDataModel data = cursorToRecord(cursor);
                listData.add(data);
                cursor.moveToNext();
            }
            cursor.close();
        }
        return listData;
    }

    @Override
    public List<? extends BaseModel> getDataForUpload() {
        return null;
    }

    @Override
    public boolean updateUploadedDB(List<? extends BaseModel> items, boolean uploaded) {
        Boolean result = false;
        List<ProcessedDataModel> castedItems = (List<ProcessedDataModel>) items;
//        for (int i = 0; i < castedItems.size(); i++) {
//            LogUtils.logInFile("ProcessedDataDAO", "updateUploadedDB() :: data.iri == " + castedItems.get(i).getIri() + " :: data.time == " + castedItems.get(i).getTime());
//        }
        SQLiteDatabase db = getDatabase();
        try {
            db.beginTransaction();
            for (ProcessedDataModel item : castedItems) {
                item.setUploaded(uploaded);
                db.update(DataBaseHelper.TABLE_PROCESSED_DATA, getContentValues(item), COLUMN_PROCESSED_DATA_ID + "=?", new String[]{String.valueOf(item.getId())});
            }
            db.setTransactionSuccessful();
            result = true;
        } catch (Exception e) {
            result = false;
        } finally {
            db.endTransaction();
        }
        return result;
    }

    @Override
    public boolean updatePendingDB(List<? extends BaseModel> items, boolean pending) {
        Boolean result = false;
        List<ProcessedDataModel> castedItems = (List<ProcessedDataModel>) items;
        SQLiteDatabase db = getDatabase();
        try {
            db.beginTransaction();
            for (ProcessedDataModel item : castedItems) {
                item.setPending(pending);
                db.update(DataBaseHelper.TABLE_PROCESSED_DATA, getContentValues(item), COLUMN_PROCESSED_DATA_ID + "=?", new String[]{String.valueOf(item.getId())});
            }
            db.setTransactionSuccessful();
            result = true;
        } catch (Exception e) {
            result = false;
        } finally {
            db.endTransaction();
        }
        return result;
    }

    @Override
    public boolean updateUploadedPendingDB(List<? extends BaseModel> items, boolean uploaded, boolean pending) {
        Boolean result = false;
        List<ProcessedDataModel> castedItems = (List<ProcessedDataModel>) items;
        SQLiteDatabase db = getDatabase();
        try {
            db.beginTransaction();
            for (ProcessedDataModel item : castedItems) {
                item.setUploaded(uploaded);
                item.setPending(pending);
                db.update(DataBaseHelper.TABLE_PROCESSED_DATA, getContentValues(item), COLUMN_PROCESSED_DATA_ID + "=?", new String[]{String.valueOf(item.getId())});
            }
            db.setTransactionSuccessful();
            result = true;
        } catch (Exception e) {
            result = false;
        } finally {
            db.endTransaction();
        }
        return result;
    }

    @Override
    public boolean putDB(List<? extends BaseModel> items) {
        return false;
    }

    public List<LatLng> getLatLngDataForMap(final long recordId) {
        final List<LatLng> listData = new ArrayList<>();
        final Cursor cursor = getProcessedDataByRecordIdCursor(recordId);
        if (cursor != null) {
            while (!cursor.isAfterLast()) {
                final ProcessedDataModel data = cursorToRecord(cursor);
                listData.add(new LatLng(data.getCoordsStart()[0], data.getCoordsStart()[1]));
//                if (data.getCoordsEnd()[0] == data.getCoordsStart()[0]) {
//                    data.setCoordsEnd(data.getCoordsEnd()[0] + 0.003, data.getCoordsEnd()[1] + 0.003);
//                }
                listData.add(new LatLng(data.getCoordsEnd()[0], data.getCoordsEnd()[1]));
                cursor.moveToNext();
            }
            cursor.close();
        }
        return listData;
    }

    public Cursor getAllProcessedDataCursor() {
        return getDatabase().query(DataBaseHelper.TABLE_PROCESSED_DATA, allColumns, null, null, null, null, null);
    }

    private Cursor getProcessedDataForUploadCursor(final String selection) {
        String sel = COLUMN_PROCESSED_DATA_UPLOADED + "=0 and " + COLUMN_PROCESSED_DATA_PENDING + "=0";
        if (selection != null) {
            sel += " and " + selection;
        }
        return getDatabase().query(DataBaseHelper.TABLE_PROCESSED_DATA, allColumns, sel, null, null, null, null);
    }
}
