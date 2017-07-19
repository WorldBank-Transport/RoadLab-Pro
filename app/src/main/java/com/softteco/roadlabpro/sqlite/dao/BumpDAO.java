package com.softteco.roadlabpro.sqlite.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.softteco.roadlabpro.sqlite.DataBaseHelper;
import com.softteco.roadlabpro.sqlite.model.BaseModel;
import com.softteco.roadlabpro.sqlite.model.BumpModel;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Aleksey on 17.04.2015.
 */
public class BumpDAO extends BaseDAO implements UploadDAO {

    public BumpDAO(final Context context) {
        super(context);
    }

    // columns of the bump table
    public static final String COLUMN_BUMP_ID = RecordDAO.COLUMN_RECORD_ID;
    public static final String COLUMN_BUMP_FOLDER_ID = "folder_id";
    public static final String COLUMN_BUMP_ROAD_ID = "road_id";
    public static final String COLUMN_BUMP_MEASUREMENT_ID = "measurement_id";
    public static final String COLUMN_BUMP_TIME = "time";
    public static final String COLUMN_BUMP_SPEED = "speed";
    public static final String COLUMN_BUMP_LATITUDE = "latitude";
    public static final String COLUMN_BUMP_LONGITUDE = "longitude";
    public static final String COLUMN_BUMP_ALTITUDE = "altitude";
    public static final String COLUMN_BUMP_RECORD_ID = "record_id";
    public static final String COLUMN_BUMP_UPLOADED = "uploaded";
    public static final String COLUMN_BUMP_PENDING = "pending";
    public static final String COLUMN_BUMP_ACCELERATION_X = "acceleration_x";
    public static final String COLUMN_BUMP_ACCELERATION_Y = "acceleration_y";
    public static final String COLUMN_BUMP_ACCELERATION_Z = "acceleration_z";
    public static final String COLUMN_BUMP_IS_FIXED = "is_fixed";

    private String[] allColumns = {
            COLUMN_BUMP_ID,
            COLUMN_BUMP_FOLDER_ID,
            COLUMN_BUMP_ROAD_ID,
            COLUMN_BUMP_MEASUREMENT_ID,
            COLUMN_BUMP_TIME,
            COLUMN_BUMP_SPEED,
            COLUMN_BUMP_LATITUDE,
            COLUMN_BUMP_LONGITUDE,
            COLUMN_BUMP_ALTITUDE,
            COLUMN_BUMP_ACCELERATION_X,
            COLUMN_BUMP_ACCELERATION_Y,
            COLUMN_BUMP_ACCELERATION_Z,
            COLUMN_BUMP_UPLOADED,
            COLUMN_BUMP_PENDING,
            COLUMN_BUMP_RECORD_ID,
            COLUMN_BUMP_IS_FIXED };

    public static final String SQL_CREATE_TABLE_BUMPS = "CREATE TABLE " + DataBaseHelper.TABLE_BUMPS + "("
            + COLUMN_BUMP_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + COLUMN_BUMP_FOLDER_ID + " INTEGER NOT NULL, "
            + COLUMN_BUMP_ROAD_ID + " INTEGER NOT NULL, "
            + COLUMN_BUMP_MEASUREMENT_ID + " INTEGER NOT NULL, "
            + COLUMN_BUMP_TIME + " INTEGER NOT NULL, "
            + COLUMN_BUMP_SPEED + " REAL NOT NULL, "
            + COLUMN_BUMP_LATITUDE + " REAL NOT NULL, "
            + COLUMN_BUMP_LONGITUDE + " REAL NOT NULL, "
            + COLUMN_BUMP_ALTITUDE + " REAL NOT NULL, "
            + COLUMN_BUMP_ACCELERATION_X + " REAL NOT NULL, "
            + COLUMN_BUMP_ACCELERATION_Y + " REAL NOT NULL, "
            + COLUMN_BUMP_ACCELERATION_Z + " REAL NOT NULL, "
            + COLUMN_BUMP_UPLOADED + " INTEGER NOT NULL DEFAULT (0), "
            + COLUMN_BUMP_PENDING + " INTEGER NOT NULL DEFAULT (0), "
            + COLUMN_BUMP_RECORD_ID + " INTEGER NOT NULL, "
            + COLUMN_BUMP_IS_FIXED + " INTEGER NOT NULL DEFAULT (0)"
            + ");";

    public static final String SQL_DROP_TABLE_BUMPS = "DROP TABLE IF EXISTS " + DataBaseHelper.TABLE_BUMPS;

    public long putBump(final BumpModel data) {
        long insertId = -1;
        SQLiteDatabase db = getDatabase();
        try {
            db.beginTransaction();
            final ContentValues values = getContentValues(data);
            insertId = db.insert(DataBaseHelper.TABLE_BUMPS, null, values);
            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.e(TAG, e != null && e.getMessage() != null ? e.getMessage() : "");
        } finally {
            db.endTransaction();
        }
        return insertId;
    }

    private ContentValues getContentValues(final BumpModel data) {
        final ContentValues values = new ContentValues();
        values.put(COLUMN_BUMP_FOLDER_ID, data.getFolderId());
        values.put(COLUMN_BUMP_ROAD_ID, data.getRoadId());
        values.put(COLUMN_BUMP_MEASUREMENT_ID, data.getMeasurementId());
        values.put(COLUMN_BUMP_TIME, data.getTime());
        values.put(COLUMN_BUMP_SPEED, data.getSpeed());
        values.put(COLUMN_BUMP_LATITUDE, data.getLatitude());
        values.put(COLUMN_BUMP_LONGITUDE, data.getLongitude());
        values.put(COLUMN_BUMP_ALTITUDE, data.getAltitude());
        values.put(COLUMN_BUMP_ACCELERATION_X, data.getAccelerationX());
        values.put(COLUMN_BUMP_ACCELERATION_Y, data.getAccelerationY());
        values.put(COLUMN_BUMP_ACCELERATION_Z, data.getAccelerationZ());
        values.put(COLUMN_BUMP_RECORD_ID, data.getRecordId());
        values.put(COLUMN_BUMP_UPLOADED, data.isUploaded() ? 1 : 0);
        values.put(COLUMN_BUMP_PENDING, data.isPending() ? 1 : 0);
        values.put(COLUMN_BUMP_IS_FIXED, data.isFixed() ? 1 : 0);
        return values;
    }

    public void putBumpList(final List<BumpModel> items) {
        putBumpList(items, -1, -1, -1);
    }

    public void putBumpList(final List<BumpModel> items,
        final long folderId, final long roadId, final long measurementId) {
        for (BumpModel d : items) {
            if (folderId >= 0) {
                d.setFolderId(folderId);
            }
            if (roadId >= 0) {
                d.setRoadId(roadId);
            }
            if (measurementId >= 0) {
                d.setMeasurementId(measurementId);
            }
            putBump(d);
        }
    }

    public long getAllItemsCount(long folderId, long roadId, long measurementId) {
        String queryStr = getQueryStr(folderId, roadId, measurementId);
        return getAllItemsCountForId(DataBaseHelper.TABLE_BUMPS, queryStr);
    }

    public void deleteItemsWithRecordId(final long recordId) {
        SQLiteDatabase db = getDatabase();
        try {
            db.beginTransaction();
            db.delete(DataBaseHelper.TABLE_BUMPS, COLUMN_BUMP_RECORD_ID + " = " + recordId, null);
            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.e(TAG, e != null && e.getMessage() != null ? e.getMessage() : "");
        } finally {
            db.endTransaction();
        }
    }

    public Cursor getItemsCursor(long folderId, long roadId, long measurementId) {
        String queryStr = getQueryStr(folderId, roadId, measurementId);
        Cursor cursor = null;
        try {
            cursor = getDatabase().query(DataBaseHelper.TABLE_BUMPS,
            allColumns, queryStr, null, null, null, null);
            cursor.moveToFirst();
        } catch (Exception e) {
            Log.e(TAG, e != null && e.getMessage() != null ? e.getMessage() : "");
        }
        return cursor;
    }

    public void moveBumps(long folderId, long roadId, long measurementId) {
        Cursor cursor = getItemsCursor(-1, -1, measurementId);
        if (cursor != null) {
            boolean moveToNext = cursor.getCount() > 0;
            BumpModel bump = null;
            while(moveToNext) {
                bump = cursorToRecord(cursor);
                if (folderId >= 0) {
                    bump.setFolderId(folderId);
                }
                if (roadId >= 0) {
                    bump.setRoadId(roadId);
                }
                if (measurementId >= 0) {
                    bump.setMeasurementId(measurementId);
                }
                updateItem(bump);
                moveToNext = cursor.moveToNext();
            }
            cursor.close();
        }
    }

    public void deleteItemsWithMeasurementId(final long measurementId) {
        SQLiteDatabase db = getDatabase();
        try {
            db.beginTransaction();
            db.delete(DataBaseHelper.TABLE_BUMPS, COLUMN_BUMP_MEASUREMENT_ID + " = " + measurementId, null);
            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.e(TAG, e != null && e.getMessage() != null ? e.getMessage() : "");
        } finally {
            db.endTransaction();
        }
    }

    public int deleteBump(final BumpModel data) {
        long id = data.getId();
        int count = 0;
        SQLiteDatabase db = getDatabase();
        try {
            db.beginTransaction();
            count = db.delete(DataBaseHelper.TABLE_BUMPS, COLUMN_BUMP_ID + " = " + id, null);
            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.e(TAG, e != null && e.getMessage() != null ? e.getMessage() : "");
        } finally {
            db.endTransaction();
        }
        return count;
    }

    public BumpModel getBumpByRecordId(final long id) {
        final Cursor cursor = getBumpByRecordIdCursor(id);
        final BumpModel record = cursorToRecord(cursor);
        return record;
    }

    public Cursor getBumpByRecordIdCursor(final long recordId) {
        final Cursor cursor = getDatabase().query(DataBaseHelper.TABLE_BUMPS, allColumns,
                COLUMN_BUMP_RECORD_ID + " = ?",
                new String[]{String.valueOf(recordId)}, null, null, null);
        cursor.moveToFirst();
        return cursor;
    }

    public Cursor getBumpByMeasurementIdCursor(final long measurementId) {
        final Cursor cursor = getDatabase().query(DataBaseHelper.TABLE_BUMPS, allColumns,
            COLUMN_BUMP_MEASUREMENT_ID + " = ?",
            new String[]{String.valueOf(measurementId)}, null, null, null);
        cursor.moveToFirst();
        return cursor;
    }

    public List<BumpModel> getBumpsByMeasurementId(final long id) {
        final Cursor cursor = getBumpByMeasurementIdCursor(id);
        List<BumpModel> items = new ArrayList<BumpModel>();
        BumpModel record = null;
        boolean moveToNext = cursor.getCount() > 0;
        while(moveToNext) {
            record = cursorToRecord(cursor);
            items.add(record);
            moveToNext = cursor.moveToNext();
        }
        return items;
    }

    public long updateItem(final BumpModel data) {
        long insertId = -1;
        SQLiteDatabase db = getDatabase();
        try {
            db.beginTransaction();
            ContentValues values = getContentValues(data);
            insertId = getDatabase().update(DataBaseHelper.TABLE_BUMPS, values,
            COLUMN_BUMP_ID + "=?", new String[]{String.valueOf(data.getId())});
            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.e(TAG, e != null && e.getMessage() != null ? e.getMessage() : "");
        } finally {
            db.endTransaction();
        }
        return insertId;
    }

    public BumpModel cursorToRecord(final Cursor cursor) {
        final BumpModel data = new BumpModel();
        data.setId(cursor.getLong(cursor.getColumnIndex(RecordDAO.COLUMN_RECORD_ID)));
        data.setFolderId(cursor.getLong(cursor.getColumnIndex(COLUMN_BUMP_FOLDER_ID)));
        data.setRoadId(cursor.getLong(cursor.getColumnIndex(COLUMN_BUMP_ROAD_ID)));
        data.setMeasurementId(cursor.getLong(cursor.getColumnIndex(COLUMN_BUMP_MEASUREMENT_ID)));
        data.setTime(cursor.getLong(cursor.getColumnIndex(COLUMN_BUMP_TIME)));
        data.setSpeed(cursor.getFloat(cursor.getColumnIndex(COLUMN_BUMP_SPEED)));
        data.setLatitude(cursor.getDouble(cursor.getColumnIndex(COLUMN_BUMP_LATITUDE)));
        data.setLongitude(cursor.getDouble(cursor.getColumnIndex(COLUMN_BUMP_LONGITUDE)));
        data.setAltitude(cursor.getDouble(cursor.getColumnIndex(COLUMN_BUMP_ALTITUDE)));
        data.setAccelerationX(cursor.getFloat(cursor.getColumnIndex(COLUMN_BUMP_ACCELERATION_X)));
        data.setAccelerationY(cursor.getFloat(cursor.getColumnIndex(COLUMN_BUMP_ACCELERATION_Y)));
        data.setAccelerationZ(cursor.getFloat(cursor.getColumnIndex(COLUMN_BUMP_ACCELERATION_Z)));
        data.setUploaded(cursor.getInt(cursor.getColumnIndex(COLUMN_BUMP_UPLOADED)) == 1);
        data.setPending(cursor.getInt(cursor.getColumnIndex(COLUMN_BUMP_PENDING)) == 1);
        data.setFixed(cursor.getInt(cursor.getColumnIndex(COLUMN_BUMP_IS_FIXED)) == 1);
        data.setRecordId(cursor.getLong(cursor.getColumnIndex(COLUMN_BUMP_RECORD_ID)));
        return data;
    }

    public Cursor getAllBumpCursor() {
        return getDatabase().query(DataBaseHelper.TABLE_BUMPS, allColumns, null, null, null, null, null);
    }

    private Cursor getBumpsForUploadCursor(final String selection) {
        String sel = COLUMN_BUMP_UPLOADED + "=0 and " + COLUMN_BUMP_PENDING + "=0";
        if (selection != null) {
            sel += " and " + selection;
        }
        return getDatabase().query(DataBaseHelper.TABLE_BUMPS, allColumns, sel, null, null, null, null);
    }

    @Override
    public List<? extends BaseModel> getDataForUpload(final String query) {
        final List<BumpModel> listData = new ArrayList<>();
        final Cursor cursor = getBumpsForUploadCursor(query);
        if (cursor != null) {
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                final BumpModel data = cursorToRecord(cursor);
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
    public boolean updateUploadedDB(List<? extends BaseModel> items, boolean flag) {
        Boolean result = false;
        List<BumpModel> castedItems = (List<BumpModel>) items;
        SQLiteDatabase db = getDatabase();
        try {
            db.beginTransaction();
            for (BumpModel item : castedItems) {
                item.setUploaded(flag);
                db.update(DataBaseHelper.TABLE_BUMPS, getContentValues(item), COLUMN_BUMP_ID + "=?", new String[]{String.valueOf(item.getId())});
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
    public boolean updatePendingDB(List<? extends BaseModel> items, boolean flag) {
        Boolean result = false;
        List<BumpModel> castedItems = (List<BumpModel>) items;
        SQLiteDatabase db = getDatabase();
        try {
            db.beginTransaction();
            for (BumpModel item : castedItems) {
                item.setPending(flag);
                db.update(DataBaseHelper.TABLE_BUMPS, getContentValues(item), COLUMN_BUMP_ID + "=?", new String[]{String.valueOf(item.getId())});
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
        List<BumpModel> castedItems = (List<BumpModel>) items;
        SQLiteDatabase db = getDatabase();
        try {
            db.beginTransaction();
            for (BumpModel item : castedItems) {
                item.setUploaded(uploaded);
                item.setPending(pending);
                db.update(DataBaseHelper.TABLE_BUMPS, getContentValues(item), COLUMN_BUMP_ID + "=?",
                        new String[]{String.valueOf(item.getId())});
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
}
