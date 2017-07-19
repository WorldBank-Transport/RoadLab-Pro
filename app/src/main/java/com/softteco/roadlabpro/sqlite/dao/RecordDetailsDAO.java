package com.softteco.roadlabpro.sqlite.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.softteco.roadlabpro.sqlite.DataBaseHelper;
import com.softteco.roadlabpro.sqlite.model.RecordDetailsModel;
import com.softteco.roadlabpro.sqlite.model.RecordModel;
import com.softteco.roadlabpro.tasks.ExportDBIntoCSV;
import com.softteco.roadlabpro.util.DeviceUtil;
import com.softteco.roadlabpro.util.ExportToCSVResult;
import com.softteco.roadlabpro.util.ValidateUtil;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by Vadim on 4/6/2015.
 */
public class RecordDetailsDAO extends BaseDAO {

    public static final String TAG = RecordDetailsDAO.class.getName();

    // columns of the record details table
    public static final String COLUMN_RECORD_DETAILS_ID = RecordDAO.COLUMN_RECORD_ID;
    public static final String COLUMN_RECORD_DETAILS_TIME = "time";
    public static final String COLUMN_RECORD_DETAILS_TIMESTAMP = "timeStamp";
    public static final String COLUMN_RECORD_DETAILS_INTERVAL_NUMBER = "interval_num";
    public static final String COLUMN_RECORD_DETAILS_GPS_ACCURACY = "gps_accuracy";
    public static final String COLUMN_RECORD_DETAILS_AVERAGE_SPEED = "average_speed";
    public static final String COLUMN_RECORD_DETAILS_DISTANCE = "distance";
    public static final String COLUMN_RECORD_DETAILS_LATITUDE = "latitude";
    public static final String COLUMN_RECORD_DETAILS_LONGITUDE = "longitude";
    public static final String COLUMN_RECORD_DETAILS_CURRENT_LATITUDE = "curLatitude";
    public static final String COLUMN_RECORD_DETAILS_CURRENT_LONGITUDE = "curLongitude";
    public static final String COLUMN_RECORD_DETAILS_ACCELEROMETER_X = "accelerometer_x";
    public static final String COLUMN_RECORD_DETAILS_ACCELEROMETER_Y = "accelerometer_y";
    public static final String COLUMN_RECORD_DETAILS_ACCELEROMETER_Z = "accelerometer_z";
    public static final String COLUMN_RECORD_DETAILS_ACCELEROMETER_LINEAR_X = "accelerometer_linear_x";
    public static final String COLUMN_RECORD_DETAILS_ACCELEROMETER_LINEAR_Y = "accelerometer_linear_y";
    public static final String COLUMN_RECORD_DETAILS_ACCELEROMETER_LINEAR_Z = "accelerometer_linear_z";
    public static final String COLUMN_RECORD_DETAILS_ACCELEROMETER_GRAVITY_X = "accelerometer_gravity_x";
    public static final String COLUMN_RECORD_DETAILS_ACCELEROMETER_GRAVITY_Y = "accelerometer_gravity_y";
    public static final String COLUMN_RECORD_DETAILS_ACCELEROMETER_GRAVITY_Z = "accelerometer_gravity_z";
    public static final String COLUMN_RECORD_DETAILS_ROTATION_ANGLE = "rotation_angle";
    public static final String COLUMN_RECORD_DETAILS_RECORD_ID = "record_id";

    private String[] allColumns = {
            COLUMN_RECORD_DETAILS_ID,
            COLUMN_RECORD_DETAILS_TIME,
            COLUMN_RECORD_DETAILS_TIMESTAMP,
            COLUMN_RECORD_DETAILS_INTERVAL_NUMBER,
            COLUMN_RECORD_DETAILS_GPS_ACCURACY,
            COLUMN_RECORD_DETAILS_DISTANCE,
            COLUMN_RECORD_DETAILS_LATITUDE,
            COLUMN_RECORD_DETAILS_LONGITUDE,
            COLUMN_RECORD_DETAILS_CURRENT_LATITUDE,
            COLUMN_RECORD_DETAILS_CURRENT_LONGITUDE,
            COLUMN_RECORD_DETAILS_AVERAGE_SPEED,
            COLUMN_RECORD_DETAILS_ACCELEROMETER_X,
            COLUMN_RECORD_DETAILS_ACCELEROMETER_Y,
            COLUMN_RECORD_DETAILS_ACCELEROMETER_Z,
            COLUMN_RECORD_DETAILS_ACCELEROMETER_LINEAR_X,
            COLUMN_RECORD_DETAILS_ACCELEROMETER_LINEAR_Y,
            COLUMN_RECORD_DETAILS_ACCELEROMETER_LINEAR_Z,
            COLUMN_RECORD_DETAILS_ACCELEROMETER_GRAVITY_X,
            COLUMN_RECORD_DETAILS_ACCELEROMETER_GRAVITY_Y,
            COLUMN_RECORD_DETAILS_ACCELEROMETER_GRAVITY_Z,
            COLUMN_RECORD_DETAILS_ROTATION_ANGLE,
            COLUMN_RECORD_DETAILS_RECORD_ID};

    public static final String SQL_CREATE_TABLE_RECORD_DETAILS = "CREATE TABLE " + DataBaseHelper.TABLE_RECORDS_DETAILS + "("
            + COLUMN_RECORD_DETAILS_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + COLUMN_RECORD_DETAILS_TIME + " INTEGER NOT NULL, "
            + COLUMN_RECORD_DETAILS_TIMESTAMP + " INTEGER NOT NULL, "
            + COLUMN_RECORD_DETAILS_INTERVAL_NUMBER + " INTEGER NOT NULL, "
            + COLUMN_RECORD_DETAILS_GPS_ACCURACY + " INTEGER NOT NULL, "
            + COLUMN_RECORD_DETAILS_DISTANCE + " REAL NOT NULL, "
            + COLUMN_RECORD_DETAILS_LATITUDE + " REAL NOT NULL, "
            + COLUMN_RECORD_DETAILS_LONGITUDE + " REAL NOT NULL, "
            + COLUMN_RECORD_DETAILS_CURRENT_LATITUDE + " REAL NOT NULL, "
            + COLUMN_RECORD_DETAILS_CURRENT_LONGITUDE + " REAL NOT NULL, "
            + COLUMN_RECORD_DETAILS_AVERAGE_SPEED + " REAL NOT NULL, "
            + COLUMN_RECORD_DETAILS_ACCELEROMETER_X + " REAL NOT NULL, "
            + COLUMN_RECORD_DETAILS_ACCELEROMETER_Y + " REAL NOT NULL, "
            + COLUMN_RECORD_DETAILS_ACCELEROMETER_Z + " REAL NOT NULL, "
            + COLUMN_RECORD_DETAILS_ACCELEROMETER_LINEAR_X + " REAL NOT NULL, "
            + COLUMN_RECORD_DETAILS_ACCELEROMETER_LINEAR_Y + " REAL NOT NULL, "
            + COLUMN_RECORD_DETAILS_ACCELEROMETER_LINEAR_Z + " REAL NOT NULL, "
            + COLUMN_RECORD_DETAILS_ACCELEROMETER_GRAVITY_X + " REAL NOT NULL, "
            + COLUMN_RECORD_DETAILS_ACCELEROMETER_GRAVITY_Y + " REAL NOT NULL, "
            + COLUMN_RECORD_DETAILS_ACCELEROMETER_GRAVITY_Z + " REAL NOT NULL, "
            + COLUMN_RECORD_DETAILS_ROTATION_ANGLE + " REAL NOT NULL, "
            + COLUMN_RECORD_DETAILS_RECORD_ID + " INTEGER NOT NULL "
            + ");";

    public static final String SQL_DROP_TABLE_RECORD_DETAILS = "DROP TABLE IF EXISTS " + DataBaseHelper.TABLE_RECORDS_DETAILS;

    public RecordDetailsDAO(final Context context) {
        super(context);
    }

    public long putRecordDetails(final RecordDetailsModel recordDetails) {
        long insertId = -1;
        SQLiteDatabase db = getDatabase();
        try {
            db.beginTransaction();
            ContentValues values = getContentValues(recordDetails);
            insertId = db.insert(DataBaseHelper.TABLE_RECORDS_DETAILS, null, values);
            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.e(TAG, e != null && e.getMessage() != null ? e.getMessage() : "");
        } finally {
            db.endTransaction();
        }
        return insertId;
    }

    public ContentValues getContentValues(final RecordDetailsModel recordDetails) {
        final ContentValues values = new ContentValues();
        values.put(COLUMN_RECORD_DETAILS_TIME, recordDetails.getTime());
        values.put(COLUMN_RECORD_DETAILS_TIMESTAMP, recordDetails.getTimeStamp());
        values.put(COLUMN_RECORD_DETAILS_GPS_ACCURACY, recordDetails.getGpsAccuracy());
        values.put(COLUMN_RECORD_DETAILS_INTERVAL_NUMBER, recordDetails.getIntervalNumber());
        values.put(COLUMN_RECORD_DETAILS_DISTANCE, recordDetails.getDistance());
        values.put(COLUMN_RECORD_DETAILS_LATITUDE, recordDetails.getLatitude());
        values.put(COLUMN_RECORD_DETAILS_LONGITUDE, recordDetails.getLongitude());
        values.put(COLUMN_RECORD_DETAILS_CURRENT_LATITUDE, recordDetails.getCurLatitude());
        values.put(COLUMN_RECORD_DETAILS_CURRENT_LONGITUDE, recordDetails.getCurLongitude());
        values.put(COLUMN_RECORD_DETAILS_AVERAGE_SPEED, recordDetails.getAverageSpeed());
        values.put(COLUMN_RECORD_DETAILS_ACCELEROMETER_X, recordDetails.getAccelerometerX());
        values.put(COLUMN_RECORD_DETAILS_ACCELEROMETER_Y, recordDetails.getAccelerometerY());
        values.put(COLUMN_RECORD_DETAILS_ACCELEROMETER_Z, recordDetails.getAccelerometerZ());
        values.put(COLUMN_RECORD_DETAILS_ACCELEROMETER_LINEAR_X, recordDetails.getAccelerometerLinearX());
        values.put(COLUMN_RECORD_DETAILS_ACCELEROMETER_LINEAR_Y, recordDetails.getAccelerometerLinearY());
        values.put(COLUMN_RECORD_DETAILS_ACCELEROMETER_LINEAR_Z, recordDetails.getAccelerometerLinearZ());
        values.put(COLUMN_RECORD_DETAILS_ACCELEROMETER_GRAVITY_X, recordDetails.getAccelerometerGravityX());
        values.put(COLUMN_RECORD_DETAILS_ACCELEROMETER_GRAVITY_Y, recordDetails.getAccelerometerGravityY());
        values.put(COLUMN_RECORD_DETAILS_ACCELEROMETER_GRAVITY_Z, recordDetails.getAccelerometerGravityZ());
        values.put(COLUMN_RECORD_DETAILS_ROTATION_ANGLE,
        ValidateUtil.isValidNumber(recordDetails.getRotationAngle()) ? recordDetails.getRotationAngle() : 0);
        values.put(COLUMN_RECORD_DETAILS_RECORD_ID, recordDetails.getId());
        return values;
    }

    public void putRecordDetailsList(final List<RecordDetailsModel> records) {
        for (RecordDetailsModel r : records) {
            putRecordDetails(r);
        }
    }

    public void deleteRecordDetails(final RecordDetailsModel recordDetailsModel) {
        long id = recordDetailsModel.getId();
        SQLiteDatabase db = getDatabase();
        try {
            db.beginTransaction();
            db.delete(DataBaseHelper.TABLE_RECORDS_DETAILS, COLUMN_RECORD_DETAILS_ID + " = " + id, null);
            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.e(TAG, e != null && e.getMessage() != null ? e.getMessage() : "");
        } finally {
            db.endTransaction();
        }
    }

    public List<RecordDetailsModel> getAllRecordDetails() {
        final List<RecordDetailsModel> listRecordDetails = new ArrayList<>();

        final Cursor cursor = getDatabase().query(DataBaseHelper.TABLE_RECORDS_DETAILS,
                allColumns, null, null, null, null, null);

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            RecordDetailsModel recordDetailsModel = cursorToRecordDetails(cursor);
            listRecordDetails.add(recordDetailsModel);
            cursor.moveToNext();
        }
        cursor.close();
        return listRecordDetails;
    }

    public List<RecordDetailsModel> getRecordDetailsOfRecord(final long recordId) {
        final List<RecordDetailsModel> listRecordDetails = new ArrayList<>();
        final Cursor cursor = getRecordDetailsByRecordIdCursor(recordId);
        while (!cursor.isAfterLast()) {
            final RecordDetailsModel recordDetailsModel = cursorToRecordDetails(cursor);
            listRecordDetails.add(recordDetailsModel);
            cursor.moveToNext();
        }
        cursor.close();
        return listRecordDetails;
    }

    public Cursor getRecordDetailsByRecordIdCursor(final long recordId) {
        final Cursor cursor = getDatabase().query(DataBaseHelper.TABLE_RECORDS_DETAILS, allColumns,
            COLUMN_RECORD_DETAILS_RECORD_ID + " = ?",
            new String[]{String.valueOf(recordId)}, null, null, null);
        cursor.moveToFirst();
        return cursor;
    }

    public RecordDetailsModel cursorToRecordDetails(final Cursor cursor) {
        final RecordDetailsModel recordDetails = new RecordDetailsModel();
        recordDetails.setId(cursor.getLong(cursor.getColumnIndex(COLUMN_RECORD_DETAILS_ID)));
        recordDetails.setTime(cursor.getLong(cursor.getColumnIndex(COLUMN_RECORD_DETAILS_TIME)));
        recordDetails.setTimeStamp(cursor.getLong(cursor.getColumnIndex(COLUMN_RECORD_DETAILS_TIMESTAMP)));
        recordDetails.setGpsAccuracy(cursor.getInt(cursor.getColumnIndex(COLUMN_RECORD_DETAILS_GPS_ACCURACY)));
        recordDetails.setIntervalNumber(cursor.getInt(cursor.getColumnIndex(COLUMN_RECORD_DETAILS_INTERVAL_NUMBER)));
        recordDetails.setAverageSpeed(cursor.getFloat(cursor.getColumnIndex(COLUMN_RECORD_DETAILS_AVERAGE_SPEED)));
        recordDetails.setRotationAngle(cursor.getFloat(cursor.getColumnIndex(COLUMN_RECORD_DETAILS_ROTATION_ANGLE)));
        recordDetails.setAccelerometerGravityX(cursor.getFloat(cursor.getColumnIndex(COLUMN_RECORD_DETAILS_ACCELEROMETER_GRAVITY_X)));
        recordDetails.setAccelerometerGravityY(cursor.getFloat(cursor.getColumnIndex(COLUMN_RECORD_DETAILS_ACCELEROMETER_GRAVITY_Y)));
        recordDetails.setAccelerometerGravityZ(cursor.getFloat(cursor.getColumnIndex(COLUMN_RECORD_DETAILS_ACCELEROMETER_GRAVITY_Z)));
        recordDetails.setAccelerometerLinearX(cursor.getFloat(cursor.getColumnIndex(COLUMN_RECORD_DETAILS_ACCELEROMETER_LINEAR_X)));
        recordDetails.setAccelerometerLinearY(cursor.getFloat(cursor.getColumnIndex(COLUMN_RECORD_DETAILS_ACCELEROMETER_LINEAR_Y)));
        recordDetails.setAccelerometerLinearZ(cursor.getFloat(cursor.getColumnIndex(COLUMN_RECORD_DETAILS_ACCELEROMETER_LINEAR_Z)));
        recordDetails.setAccelerometerX(cursor.getFloat(cursor.getColumnIndex(COLUMN_RECORD_DETAILS_ACCELEROMETER_X)));
        recordDetails.setAccelerometerY(cursor.getFloat(cursor.getColumnIndex(COLUMN_RECORD_DETAILS_ACCELEROMETER_Y)));
        recordDetails.setAccelerometerZ(cursor.getFloat(cursor.getColumnIndex(COLUMN_RECORD_DETAILS_ACCELEROMETER_Z)));
        recordDetails.setDistance(cursor.getDouble(cursor.getColumnIndex(COLUMN_RECORD_DETAILS_DISTANCE)));
        recordDetails.setLatitude(cursor.getDouble(cursor.getColumnIndex(COLUMN_RECORD_DETAILS_LATITUDE)));
        recordDetails.setLongitude(cursor.getDouble(cursor.getColumnIndex(COLUMN_RECORD_DETAILS_LONGITUDE)));
        recordDetails.setCurLatitude(cursor.getDouble(cursor.getColumnIndex(COLUMN_RECORD_DETAILS_CURRENT_LATITUDE)));
        recordDetails.setCurLongitude(cursor.getDouble(cursor.getColumnIndex(COLUMN_RECORD_DETAILS_CURRENT_LONGITUDE)));
        long recordId = cursor.getLong(cursor.getColumnIndex(COLUMN_RECORD_DETAILS_RECORD_ID));
        recordDetails.setRecordId(recordId);
        return recordDetails;
    }

    public void exportDBIntoCSV(final ExportToCSVResult listener, final RecordModel record) {
        record.setDeviceId(DeviceUtil.getDeviceId(getContext()));
        ExportDBIntoCSV exportDBIntoCSV = new ExportDBIntoCSV(listener, record,  getDatabase());
        exportDBIntoCSV.execute("");
    }
}
