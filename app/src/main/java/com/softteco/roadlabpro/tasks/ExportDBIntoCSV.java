package com.softteco.roadlabpro.tasks;

import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.util.Log;

import com.opencsv.CSVWriter;
import com.softteco.roadlabpro.sqlite.DataBaseHelper;
import com.softteco.roadlabpro.sqlite.dao.RecordDAO;
import com.softteco.roadlabpro.sqlite.dao.RecordDetailsDAO;
import com.softteco.roadlabpro.sqlite.model.RecordModel;
import com.softteco.roadlabpro.util.DeviceUtil;
import com.softteco.roadlabpro.util.ExportToCSVResult;
import com.softteco.roadlabpro.util.FileUtils;
import com.softteco.roadlabpro.util.TimeUtil;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by Anton on 03.03.2015.
 */
public class ExportDBIntoCSV extends AsyncTask<String, Void, String[]> {

    public static final String SEPARATOR = ",";
    private static final String TAG = "ExportDBToCSV";
    private SQLiteDatabase sqLiteDatabase;

    private String[] tableNames = {
            "ms",
            "time",
            "int",
            "gps",
            "dist",
            "avg_lat",
            "avg_lon",
            "lat",
            "lon",
            "speed",
            "Ax",
            "Ay",
            "Az",
            "Lx",
            "Ly",
            "Lz",
            "Gx",
            "Gy",
            "Gz",
            "angle"
    };

    private ExportToCSVResult listener;
    private RecordModel record;

    public ExportDBIntoCSV(final ExportToCSVResult listener, final RecordModel record, final SQLiteDatabase sqLiteDatabase) {
        this.listener = listener;
        this.sqLiteDatabase = sqLiteDatabase;
        this.record = record;
    }

    public ExportDBIntoCSV(final SQLiteDatabase sqLiteDatabase) {
        this(null, null, sqLiteDatabase);
    }

    @Override
    protected void onPreExecute() {
    }

    protected String[] doInBackground(final String... args) {
        Date timestampDate = new Date();
        String dataDir = FileUtils.getDataDir();
        return new String[] { writeDetailsFile(record, timestampDate, dataDir), writeHeadFile(record, timestampDate, dataDir) };
    }

    public String writeDetailsFile(RecordModel record, Date date, String dataDir) {
        File file = new File(dataDir + String.format("%s_%s%s",
                DeviceUtil.getDeviceName(), new SimpleDateFormat(TimeUtil.DATE_FILENAME_FORMAT).format(date), ".csv"));
        try {
            CSVWriter csvWrite = new CSVWriter(new FileWriter(file));
            String parametersStr = record.getId() >= 0 ? " where " + RecordDetailsDAO.COLUMN_RECORD_DETAILS_RECORD_ID + "=" + record.getId() : "";
            Cursor curCSV = sqLiteDatabase.rawQuery(
                    "select * from " + DataBaseHelper.TABLE_RECORDS_DETAILS + parametersStr, null);
            Log.e("s", parametersStr);
            //csvWrite.writeNext(new String[]{record.getCommonInfo()});
            csvWrite.writeNext(new String[]{"sep=" + SEPARATOR});
            csvWrite.writeNext(tableNames);
            SimpleDateFormat timestampDate = new SimpleDateFormat(TimeUtil.DATE_TIMESTAMP, Locale.US);
            while (curCSV.moveToNext()) {
                long time = curCSV.getLong(1);
                long unixTimestamp = curCSV.getLong(2);
                String dateStr = timestampDate.format(new Date(time + TimeUtil.getSystemTimeMillisFromUnixTimestamp(unixTimestamp)));
                int intervalNum = curCSV.getInt(3);
                int gpsAccuracy = curCSV.getInt(4);
                double distance = curCSV.getDouble(5);
                double latitude = curCSV.getDouble(6);
                double longitude = curCSV.getDouble(7);
                double curLatitude = curCSV.getDouble(8);
                double curLongitude = curCSV.getDouble(9);
                double avgSpeed = curCSV.getDouble(10);

                double accelerometerX = curCSV.getDouble(11);
                double accelerometerY = curCSV.getDouble(12);
                double accelerometerZ = curCSV.getDouble(13);

                double accelerometerLinearX = curCSV.getDouble(14);
                double accelerometerLinearY = curCSV.getDouble(15);
                double accelerometerLinearZ = curCSV.getDouble(16);

                double accelerometerGravityX = curCSV.getDouble(17);
                double accelerometerGravityY = curCSV.getDouble(18);
                double accelerometerGravityZ = curCSV.getDouble(19);
                double rotationAngle = curCSV.getDouble(20);

                String[] arrStr = {
                    String.valueOf(time),
                    dateStr,
                    String.valueOf(intervalNum),
                    String.valueOf(gpsAccuracy),

                    String.format("%3.2f", distance, Locale.US),
                    String.format("%3.8f", latitude, Locale.US),
                    String.format("%3.8f", longitude, Locale.US),
                    String.format("%3.8f", curLatitude, Locale.US),
                    String.format("%3.8f", curLongitude, Locale.US),
                    String.format("%3.2f", avgSpeed, Locale.US),

                    String.format("%3.8f", accelerometerX, Locale.US),
                    String.format("%3.8f", accelerometerY, Locale.US),
                    String.format("%3.8f", accelerometerZ, Locale.US),

                    String.format("%3.8f", accelerometerLinearX, Locale.US),
                    String.format("%3.8f", accelerometerLinearY, Locale.US),
                    String.format("%3.8f", accelerometerLinearZ, Locale.US),

                    String.format("%3.8f", accelerometerGravityX, Locale.US),
                    String.format("%3.8f", accelerometerGravityY, Locale.US),
                    String.format("%3.8f", accelerometerGravityZ, Locale.US),

                    String.format("%3.8f", rotationAngle, Locale.US)
                };
                csvWrite.writeNext(arrStr);
            }
            csvWrite.close();
            curCSV.close();
            return file.getAbsolutePath();
        } catch (SQLException sqlEx) {
            Log.e(TAG, sqlEx.getMessage(), sqlEx);
            return null;
        } catch (IOException e) {
            Log.e(TAG, e.getMessage(), e);
            return null;
        }
    }

    public String writeHeadFile(final RecordModel record, final Date date, final String dataDir) {
        File file = new File(dataDir + String.format("%s_%s%s",
                DeviceUtil.getDeviceName(), new SimpleDateFormat(TimeUtil.DATE_FILENAME_FORMAT, Locale.US).format(date), ".info"));
        try {
            CSVWriter csvWrite = new CSVWriter(new FileWriter(file));

            csvWrite.writeNext(new String[]{"sep=" + SEPARATOR});
            csvWrite.writeNext(RecordDAO.allColumns);
            csvWrite.writeNext(new String[]{String.valueOf(record.getId()),
                    String.valueOf(record.getRunNumber()),
                    record.getRoadSegmentId(),
                    record.getVehicleId(),
                    record.getDate(),
                    record.getTime(),
                    record.getDeviceId(),
                    record.getSpeedDisplay()
            });

            csvWrite.close();
            return file.getAbsolutePath();
        } catch (SQLException sqlEx) {
            Log.e(TAG, sqlEx.getMessage(), sqlEx);
            return null;
        } catch (IOException e) {
            Log.e(TAG, e.getMessage(), e);
            return null;
        }
    }

    protected void onPostExecute(final String[] success) {
        if (listener != null) {
            listener.onResultsAfterExporting(success);
        }
    }
}
