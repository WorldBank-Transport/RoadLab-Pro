package com.softteco.roadlabpro.sqlite.dao;

import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.softteco.roadlabpro.sqlite.DataBaseHelper;

/**
 * Created by ppp on 15.04.2015.
 */
public class BaseDAO {

    public static final String TAG = BaseDAO.class.getName();

    private SQLiteDatabase database;
    private DataBaseHelper dbHelper;
    private Context context;

    public static final String REAL_NOT_NULL = "REAL NOT NULL";

    public BaseDAO(final Context context) {
        this.context = context;
        dbHelper = DataBaseHelper.getInstance();
        try {
            open();
        } catch (SQLException e) {
            Log.e(TAG, "SQLException on openning database " + e.getMessage());
            e.printStackTrace();
        }
    }

    protected SQLiteDatabase getDatabase() {
        return database;
    }

    public Context getContext() {
        return context;
    }

    protected long getAllItemsCount(String table) {
        return DatabaseUtils.queryNumEntries(getDatabase(), table);
    }

    protected long getAllItemsCountForId(String table, String selection) {
        return DatabaseUtils.queryNumEntries(getDatabase(), table, selection);
    }

    protected String getQueryStr(long folderId, long roadId, long measurementId) {
        String queryStr = "";
        boolean useFolderId = folderId >= 0;
        boolean useRoadId = roadId >= 0;
        boolean useMeasurementId = measurementId >= 0;
        String folderQuery = useFolderId ? ProcessedDataDAO.COLUMN_PROCESSED_DATA_FOLDER_ID + "=" + folderId + (useRoadId || useMeasurementId ? " AND " : ""): "";
        String roadQuery = useRoadId ? ProcessedDataDAO.COLUMN_PROCESSED_DATA_ROAD_ID + "=" + roadId + (useMeasurementId ? " AND " : ""): "";
        String measurementQuery = measurementId >= 0 ? ProcessedDataDAO.COLUMN_PROCESSED_DATA_MEASUREMENT_ID + "=" + measurementId : "";
        queryStr = folderQuery + roadQuery + measurementQuery;
        return queryStr;
    }

    public double getSum(long folderId, long roadId, long measurementId, String column, String table) {
        double sum = 0;
        try {
            String queryStr = getQueryStr(folderId, roadId, measurementId);
            String calcDistanceQuery = "SELECT SUM(" + column
                    + ") FROM " + table
                    + " WHERE " + queryStr;
            final Cursor cursor = getDatabase().rawQuery(calcDistanceQuery, null);
            cursor.moveToFirst();
            sum = cursor.getFloat(0);
        } catch (Exception e) {
            Log.e(TAG, e != null && e.getMessage() != null ? e.getMessage() : "");
        }
        return sum;
    }

    public void open() throws SQLException {
        database = dbHelper.getWritableDatabase();
    }

    public void close() {
        dbHelper.close();
    }
}
