package com.softteco.roadlabpro.sqlite.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.softteco.roadlabpro.sqlite.DataBaseHelper;
import com.softteco.roadlabpro.sqlite.model.RecordDetailsModel;
import com.softteco.roadlabpro.sqlite.model.RecordModel;
import com.softteco.roadlabpro.tasks.ExportAllRecordsToCsv;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by Vadim on 4/6/2015.
 */
public class RecordDAO extends BaseDAO {

    public static final String TAG = RecordDAO.class.getName();

    // columns of the record table
    public static final String COLUMN_RECORD_ID = "_id";
    public static final String COLUMN_ROAD_ID = "road_id";
    public static final String COLUMN_RUN_NUMBER = "run_number";
    public static final String COLUMN_ROAD_SEGMENT_ID = "road_segment_id";
    public static final String COLUMN_VEHICLE_ID = "vehicle_id";
    public static final String COLUMN_DATE_STRING = "date_string";
    public static final String COLUMN_TIME_STRING = "time_string";
    public static final String COLUMN_DEVICE_ID = "device_id";
    public static final String COLUMN_SPEED_ENTERED = "speed_entered";

    public static final String[] allColumns = {
            COLUMN_RECORD_ID,
            COLUMN_ROAD_ID,
            COLUMN_RUN_NUMBER,
            COLUMN_ROAD_SEGMENT_ID,
            COLUMN_VEHICLE_ID,
            COLUMN_DATE_STRING,
            COLUMN_TIME_STRING,
            COLUMN_DEVICE_ID,
            COLUMN_SPEED_ENTERED};

    public static final String SQL_CREATE_TABLE_RECORD = "CREATE TABLE " + DataBaseHelper.TABLE_RECORDS + "("
            + COLUMN_RECORD_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + COLUMN_ROAD_ID + " INTEGER NOT NULL, "
            + COLUMN_RUN_NUMBER + " INTEGER NOT NULL, "
            + COLUMN_ROAD_SEGMENT_ID + " TEXT, "
            + COLUMN_VEHICLE_ID + " TEXT, "
            + COLUMN_DATE_STRING + " TEXT, "
            + COLUMN_TIME_STRING + " TEXT, "
            + COLUMN_DEVICE_ID + " TEXT, "
            + COLUMN_SPEED_ENTERED + " TEXT "
            + ");";

    public static final String SQL_DROP_TABLE_RECORD = "DROP TABLE IF EXISTS " + DataBaseHelper.TABLE_RECORDS;

    public RecordDAO(final Context context) {
        super(context);
    }


    ContentValues getContentValues(final RecordModel record) {
        final ContentValues values = new ContentValues();
        values.put(COLUMN_RUN_NUMBER, record.getRunNumber());
        values.put(COLUMN_ROAD_ID, record.getRoadId());
        values.put(COLUMN_ROAD_SEGMENT_ID, record.getRoadSegmentId());
        values.put(COLUMN_VEHICLE_ID, record.getVehicleId());
        values.put(COLUMN_DATE_STRING, record.getDate());
        values.put(COLUMN_TIME_STRING, record.getTime());
        values.put(COLUMN_DEVICE_ID, record.getDeviceId());
        values.put(COLUMN_SPEED_ENTERED, record.getSpeedDisplay());
        return values;
    }

    public long putRecord(final RecordModel record) {
        long insertId = -1;
        SQLiteDatabase db = getDatabase();
        try {
            db.beginTransaction();
            ContentValues values = getContentValues(record);
            insertId = getDatabase().insert(DataBaseHelper.TABLE_RECORDS, null, values);
            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.e(TAG, e != null && e.getMessage() != null ? e.getMessage() : "");
        } finally {
            db.endTransaction();
        }
        return insertId;
    }

    public long updateRecord(final RecordModel record) {
        long insertId = -1;
        SQLiteDatabase db = getDatabase();
        try {
            db.beginTransaction();
            ContentValues values = getContentValues(record);
            insertId = getDatabase().update(DataBaseHelper.TABLE_RECORDS, values, COLUMN_RECORD_ID + "=?",
                    new String[]{String.valueOf(record.getId())});
            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.e(TAG, e != null && e.getMessage() != null ? e.getMessage() : "");
        } finally {
            db.endTransaction();
        }
        return insertId;
    }

    public void deleteRecord(final RecordModel recordModel) {
        long recordId = recordModel.getId();
        deleteRelatedRecordDetails(recordId);
        SQLiteDatabase db = getDatabase();
        try {
            db.beginTransaction();
            db.delete(DataBaseHelper.TABLE_RECORDS, COLUMN_RECORD_ID + " = " + recordId, null);
            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.e(TAG, e != null && e.getMessage() != null ? e.getMessage() : "");
        } finally {
            db.endTransaction();
        }
    }

    public void deleteRelatedRecordDetails(final long recordId) {
        final RecordDetailsDAO recordDetailsDAO = new RecordDetailsDAO(getContext());
        final List<RecordDetailsModel> listRecordDetailsModel = recordDetailsDAO.getRecordDetailsOfRecord(recordId);
        if (listRecordDetailsModel != null && !listRecordDetailsModel.isEmpty()) {
            for (RecordDetailsModel e : listRecordDetailsModel) {
                recordDetailsDAO.deleteRecordDetails(e);
            }
        }
    }

    public List<RecordModel> getAllRecords() {
        final List<RecordModel> listRecords = new ArrayList<>();
        final Cursor cursor = getAllRecordsCursor();
        if (cursor != null) {
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                final RecordModel recordModel = cursorToRecord(cursor);
                listRecords.add(recordModel);
                cursor.moveToNext();
            }
            cursor.close();
        }
        return listRecords;
    }

    public Cursor getAllRecordsCursor() {
        return getDatabase().query(DataBaseHelper.TABLE_RECORDS, allColumns,
                null, null, null, null, null);
    }

    public Cursor getAllRecordsCursorByRoadId(int roadId) {
        return getDatabase().query(DataBaseHelper.TABLE_RECORDS, allColumns,
                COLUMN_ROAD_ID + " = ?", new String[]{Long.toString(roadId)}, null, null, null);
    }

    public RecordModel getRecordModelById(final long id) {
        final Cursor cursor = getDatabase().query(DataBaseHelper.TABLE_RECORDS, allColumns,
                COLUMN_RECORD_ID + " = ?",
                new String[]{String.valueOf(id)}, null, null, null);
        if (cursor != null) {
            cursor.moveToFirst();
        }

        final RecordModel record = cursorToRecord(cursor);
        return record;
    }


    public RecordModel cursorToRecord(final Cursor cursor) {
        final RecordModel record = new RecordModel();
        record.setId(cursor.getLong(cursor.getColumnIndex(COLUMN_RECORD_ID)));
        record.setRoadId(cursor.getInt(cursor.getColumnIndex(COLUMN_ROAD_ID)));
        record.setRunNumber(cursor.getInt(cursor.getColumnIndex(COLUMN_RUN_NUMBER)));
        record.setRoadSegmentId(cursor.getString(cursor.getColumnIndex(COLUMN_ROAD_SEGMENT_ID)));
        record.setVehicleId(cursor.getString(cursor.getColumnIndex(COLUMN_VEHICLE_ID)));

        record.setDate(cursor.getString(cursor.getColumnIndex(COLUMN_DATE_STRING)));
        record.setTime(cursor.getString(cursor.getColumnIndex(COLUMN_TIME_STRING)));
        record.setDeviceId(cursor.getString(cursor.getColumnIndex(COLUMN_DEVICE_ID)));
        record.setSpeedDisplay(cursor.getString(cursor.getColumnIndex(COLUMN_SPEED_ENTERED)));
        return record;
    }

    public ExportAllRecordsToCsv exportAllRecordsIntoCSV(ExportAllRecordsToCsv.OnExportAllRecordsDoneListener listener) {
        ExportAllRecordsToCsv exportAllRecordsIntoCSV = new ExportAllRecordsToCsv(this, getDatabase(), listener);
        exportAllRecordsIntoCSV.execute("");
        return exportAllRecordsIntoCSV;
    }
}
