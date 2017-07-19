package com.softteco.roadlabpro.sqlite.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.softteco.roadlabpro.sqlite.DataBaseHelper;
import com.softteco.roadlabpro.sqlite.model.BaseModel;
import com.softteco.roadlabpro.sqlite.model.MeasurementModel;

import java.util.ArrayList;
import java.util.List;

public class MeasurementDAO extends BaseDAO implements UploadDAO {

    public MeasurementDAO(final Context context) {
        super(context);
    }

    // columns of the measurement table
    public static final String COLUMN_MEASUREMENT_ID = "_id";
    public static final String COLUMN_MEASUREMENT_ROAD_ID = "road_id";
    public static final String COLUMN_MEASUREMENT_TIME = "time";
    public static final String COLUMN_MEASUREMENT_DATE = "date";
    public static final String COLUMN_MEASUREMENT_INTERVALS = "intervals";
    public static final String COLUMN_MEASUREMENT_AVG_IRI = "avg_iri";
    public static final String COLUMN_MEASUREMENT_ALL_DISTANCE = "all_distance";
    public static final String COLUMN_MEASUREMENT_PATH_DISTANCE = "path_distance";
    public static final String COLUMN_MEASUREMENT_UPLOADED = "uploaded";

    private String[] allColumns = {
            COLUMN_MEASUREMENT_ID,
            COLUMN_MEASUREMENT_ROAD_ID,
            COLUMN_MEASUREMENT_TIME,
            COLUMN_MEASUREMENT_DATE,
            COLUMN_MEASUREMENT_INTERVALS,
            COLUMN_MEASUREMENT_AVG_IRI,
            COLUMN_MEASUREMENT_ALL_DISTANCE,
            COLUMN_MEASUREMENT_PATH_DISTANCE,
            COLUMN_MEASUREMENT_UPLOADED,};

    public static final String SQL_CREATE_TABLE_MEASUREMENTS = "CREATE TABLE " + DataBaseHelper.TABLE_MEASUREMENTS + "("
            + COLUMN_MEASUREMENT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + COLUMN_MEASUREMENT_ROAD_ID + " INTEGER NOT NULL, "
            + COLUMN_MEASUREMENT_TIME + " INTEGER NOT NULL, "
            + COLUMN_MEASUREMENT_DATE + " INTEGER NOT NULL, "
            + COLUMN_MEASUREMENT_INTERVALS + " INTEGER NOT NULL, "
            + COLUMN_MEASUREMENT_AVG_IRI + " REAL NOT NULL, "
            + COLUMN_MEASUREMENT_ALL_DISTANCE + " REAL NOT NULL, "
            + COLUMN_MEASUREMENT_PATH_DISTANCE + " REAL NOT NULL, "
            + COLUMN_MEASUREMENT_UPLOADED + " INTEGER NOT NULL DEFAULT (0) "
            + ");";

    public static final String SQL_DROP_TABLE_MEASUREMENTS = "DROP TABLE IF EXISTS " + DataBaseHelper.TABLE_MEASUREMENTS;


    public long putMeasurement(final MeasurementModel data) {
        long insertId = -1;
        SQLiteDatabase db = getDatabase();
        try {
            db.beginTransaction();
            final ContentValues values = getContentValues(data);
            insertId = db.insert(DataBaseHelper.TABLE_MEASUREMENTS, null, values);
            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.e(TAG, e != null && e.getMessage() != null ? e.getMessage() : "");
        } finally {
            db.endTransaction();
        }
        return insertId;
    }

    private ContentValues getContentValues(final MeasurementModel data) {
        final ContentValues values = new ContentValues();
        values.put(COLUMN_MEASUREMENT_ROAD_ID, data.getRoadId());
        values.put(COLUMN_MEASUREMENT_TIME, data.getTime());
        values.put(COLUMN_MEASUREMENT_DATE, data.getDate());
        values.put(COLUMN_MEASUREMENT_INTERVALS, data.getIntervalsNumber());
        values.put(COLUMN_MEASUREMENT_AVG_IRI, data.getAvgIRI());
        values.put(COLUMN_MEASUREMENT_ALL_DISTANCE, data.getOverallDistance());
        values.put(COLUMN_MEASUREMENT_PATH_DISTANCE, data.getPathDistance());
        values.put(COLUMN_MEASUREMENT_UPLOADED, data.isUploaded() ? 1 : 0);
        return values;
    }

    public List<MeasurementModel> getAllMeasurementsByRoadId(long roadId) {
        final List<MeasurementModel> listMeasurements = new ArrayList<>();
        final Cursor cursor = getAllMeasurementCursor();
        if (cursor != null) {
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                final MeasurementModel measurementModel = cursorToMeasurement(cursor);
                if (measurementModel.getRoadId() == roadId) {
                    listMeasurements.add(measurementModel);
                }
                cursor.moveToNext();
            }
            cursor.close();
        }
        return listMeasurements;
    }

    public void deleteItemsWithRecordId(final long recordId) {
        SQLiteDatabase db = getDatabase();
        try {
            db.beginTransaction();
            db.delete(DataBaseHelper.TABLE_MEASUREMENTS, COLUMN_MEASUREMENT_ID + " = " + recordId, null);
            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.e(TAG, e != null && e.getMessage() != null ? e.getMessage() : "");
        } finally {
            db.endTransaction();
        }
    }

    public void deleteItemsWithRoadId(final long roadId) {
        SQLiteDatabase db = getDatabase();
        try {
            db.beginTransaction();
            db.delete(DataBaseHelper.TABLE_MEASUREMENTS, COLUMN_MEASUREMENT_ROAD_ID + " = " + roadId, null);
            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.e(TAG, e != null && e.getMessage() != null ? e.getMessage() : "");
        } finally {
            db.endTransaction();
        }
    }

    public long getAllItemsCountByRoadId(long roadId) {
        String selection = COLUMN_MEASUREMENT_ROAD_ID + "=" + roadId;
        return getAllItemsCountForId(DataBaseHelper.TABLE_MEASUREMENTS, selection);
    }

    public long updateMeasurement(final MeasurementModel measurement) {
        long insertId = -1;
        SQLiteDatabase db = getDatabase();
        try {
            db.beginTransaction();
            ContentValues values = getContentValues(measurement);
            insertId = getDatabase().update(DataBaseHelper.TABLE_MEASUREMENTS, values, COLUMN_MEASUREMENT_ID + "=?",
                    new String[]{String.valueOf(measurement.getId())});
            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.e(TAG, e != null && e.getMessage() != null ? e.getMessage() : "");
        } finally {
            db.endTransaction();
        }
        return insertId;
    }

    public int deleteMeasurement(long measurementId) {
        int count = 0;
        SQLiteDatabase db = getDatabase();
        try {
            db.beginTransaction();
            count = db.delete(DataBaseHelper.TABLE_MEASUREMENTS, COLUMN_MEASUREMENT_ID + " = " + measurementId, null);
            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.e(TAG, e != null && e.getMessage() != null ? e.getMessage() : "");
        } finally {
            db.endTransaction();
        }
        return count;
    }

    public MeasurementModel cursorToMeasurement(final Cursor cursor) {
        final MeasurementModel data = new MeasurementModel();
        data.setId(cursor.getLong(cursor.getColumnIndex(MeasurementDAO.COLUMN_MEASUREMENT_ID)));
        data.setRoadId(cursor.getLong(cursor.getColumnIndex(MeasurementDAO.COLUMN_MEASUREMENT_ROAD_ID)));
        data.setTime(cursor.getLong(cursor.getColumnIndex(MeasurementDAO.COLUMN_MEASUREMENT_TIME)));
        data.setDate(cursor.getLong(cursor.getColumnIndex(MeasurementDAO.COLUMN_MEASUREMENT_DATE)));
        data.setIntervalsNumber(cursor.getLong(cursor.getColumnIndex(MeasurementDAO.COLUMN_MEASUREMENT_INTERVALS)));
        data.setAvgIRI(cursor.getDouble(cursor.getColumnIndex(MeasurementDAO.COLUMN_MEASUREMENT_AVG_IRI)));
        data.setOverallDistance(cursor.getDouble(cursor.getColumnIndex(MeasurementDAO.COLUMN_MEASUREMENT_ALL_DISTANCE)));
        data.setPathDistance(cursor.getDouble(cursor.getColumnIndex(MeasurementDAO.COLUMN_MEASUREMENT_PATH_DISTANCE)));
        data.setUploaded(cursor.getInt(cursor.getColumnIndex(MeasurementDAO.COLUMN_MEASUREMENT_UPLOADED)) == 1);
        return data;
    }

    public List<MeasurementModel> getAllMeasurements() {
        final List<MeasurementModel> listMeas = new ArrayList<>();
        final Cursor cursor = getAllMeasurementCursor();
        try {
            if (cursor != null) {
                cursor.moveToFirst();
                while (!cursor.isAfterLast()) {
                    final MeasurementModel measModel = cursorToMeasurement(cursor);
                    listMeas.add(measModel);
                    cursor.moveToNext();
                }
                cursor.close();
            }
        } catch (Exception e) {
            Log.e(TAG, e != null && e.getMessage() != null ? e.getMessage() : "");
        }
        return listMeas;
    }

    public Cursor getAllMeasurementCursor() {
        return getDatabase().query(DataBaseHelper.TABLE_MEASUREMENTS, allColumns, null, null, null, null, null);
    }

    public Cursor getAllMeasurementByRoadIdCursor(long roadId) {
        return getDatabase().query(DataBaseHelper.TABLE_MEASUREMENTS, allColumns, COLUMN_MEASUREMENT_ROAD_ID + " = ?", new String[]{Long.toString(roadId)}, null, null, null);
    }

    public MeasurementModel getMeasurementById(long measurementId) {
        MeasurementModel measurement = null;
        try {
            Cursor cursor = getDatabase().query(DataBaseHelper.TABLE_MEASUREMENTS, allColumns,
             COLUMN_MEASUREMENT_ID + "=?", new String[]{Long.toString(measurementId)}, null, null, null);
            cursor.moveToFirst();
            measurement = cursorToMeasurement(cursor);
        } catch (Exception e) {
            Log.e(TAG, e != null && e.getMessage() != null ? e.getMessage() : "");
        } finally {
        }
        return measurement;
    }

    public long getAllItemsCount() {
        return getAllItemsCount(DataBaseHelper.TABLE_MEASUREMENTS);
    }

    @Override
    public List<? extends BaseModel> getDataForUpload(final String query) {
        final List<MeasurementModel> listData = new ArrayList<>();
//        final Cursor cursor = getBumpsForUploadCursor(query);
//        if (cursor != null) {
//            cursor.moveToFirst();
//            while (!cursor.isAfterLast()) {
//                final MeasurementModel data = cursorToMeasurement(cursor);
//                listData.add(data);
//                cursor.moveToNext();
//            }
//            cursor.close();
//        }
        return listData;
    }

    @Override
    public List<? extends BaseModel> getDataForUpload() {
        return null;
    }

    @Override
    public boolean updateUploadedDB(List<? extends BaseModel> items, boolean flag) {
        Boolean result = false;
        List<MeasurementModel> castedItems = (List<MeasurementModel>) items;
        SQLiteDatabase db = getDatabase();
        try {
            db.beginTransaction();
            for (MeasurementModel item : castedItems) {
                item.setUploaded(flag);
                db.update(DataBaseHelper.TABLE_MEASUREMENTS, getContentValues(item), COLUMN_MEASUREMENT_ID + "=?", new String[]{String.valueOf(item.getId())});
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
        List<MeasurementModel> castedItems = (List<MeasurementModel>) items;
        SQLiteDatabase db = getDatabase();
        try {
            db.beginTransaction();
            for (MeasurementModel item : castedItems) {
                item.setPending(flag);
                db.update(DataBaseHelper.TABLE_MEASUREMENTS, getContentValues(item), COLUMN_MEASUREMENT_ID + "=?", new String[]{String.valueOf(item.getId())});
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
        List<MeasurementModel> castedItems = (List<MeasurementModel>) items;
        SQLiteDatabase db = getDatabase();
        try {
            db.beginTransaction();
            for (MeasurementModel item : castedItems) {
                item.setUploaded(uploaded);
                item.setPending(pending);
                db.update(DataBaseHelper.TABLE_MEASUREMENTS, getContentValues(item), COLUMN_MEASUREMENT_ID + "=?",
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
