package com.softteco.roadlabpro.sqlite.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.softteco.roadlabpro.sqlite.DataBaseHelper;
import com.softteco.roadlabpro.sqlite.model.BaseModel;
import com.softteco.roadlabpro.sqlite.model.RoadModel;

import java.util.ArrayList;
import java.util.List;

public class RoadDAO extends BaseDAO implements UploadDAO {

    public static final String TAG = RoadDAO.class.getName();

    public RoadDAO(final Context context) {
        super(context);
    }

    // columns of the road table
    public static final String COLUMN_ROAD_ID = "_id";
    public static final String COLUMN_ROAD_FOLDER_ID = "folder_id";
    public static final String COLUMN_ROAD_TIME = "time";
    public static final String COLUMN_ROAD_DATE = "date";
    public static final String COLUMN_ROAD_NAME = "name";
    public static final String COLUMN_ROAD_EXPERIMENTS = "experiments";
    public static final String COLUMN_ROAD_ALL_DISTANCE = "all_distance";
    public static final String COLUMN_ROAD_PATH_DISTANCE = "path_distance";
    public static final String COLUMN_ROAD_AVG_IRI = "avg_iri";
    public static final String COLUMN_ROAD_AVG_SPEED = "avg_speed";
    public static final String COLUMN_ROAD_STAT_BAD_ISSUES = "stat_bad_issues";
    public static final String COLUMN_ROAD_STAT_NORMAL_ISSUES = "stat_normal_issues";
    public static final String COLUMN_ROAD_STAT_GOOD_ISSUES = "stat_good_issues";
    public static final String COLUMN_ROAD_STAT_PERFECT_ISSUES = "stat_perfect_issues";
    public static final String COLUMN_ROAD_DEFAULT = "default_road";
    public static final String COLUMN_ROAD_UPLOADED = "uploaded";

    private String[] allColumns = {
            COLUMN_ROAD_ID,
            COLUMN_ROAD_FOLDER_ID,
            COLUMN_ROAD_TIME,
            COLUMN_ROAD_DATE,
            COLUMN_ROAD_NAME,
            COLUMN_ROAD_EXPERIMENTS,
            COLUMN_ROAD_ALL_DISTANCE,
            COLUMN_ROAD_PATH_DISTANCE,
            COLUMN_ROAD_AVG_IRI,
            COLUMN_ROAD_AVG_SPEED,
            COLUMN_ROAD_STAT_BAD_ISSUES,
            COLUMN_ROAD_STAT_NORMAL_ISSUES,
            COLUMN_ROAD_STAT_GOOD_ISSUES,
            COLUMN_ROAD_STAT_PERFECT_ISSUES,
            COLUMN_ROAD_DEFAULT,
            COLUMN_ROAD_UPLOADED,};

    public static final String SQL_CREATE_TABLE_ROADS = "CREATE TABLE " + DataBaseHelper.TABLE_ROADS + "("
            + COLUMN_ROAD_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + COLUMN_ROAD_FOLDER_ID + " INTEGER NOT NULL, "
            + COLUMN_ROAD_TIME + " INTEGER NOT NULL, "
            + COLUMN_ROAD_DATE + " INTEGER NOT NULL, "
            + COLUMN_ROAD_NAME + " TEXT, "
            + COLUMN_ROAD_EXPERIMENTS + " INTEGER NOT NULL, "
            + COLUMN_ROAD_ALL_DISTANCE + " REAL NOT NULL DEFAULT (0), "
            + COLUMN_ROAD_PATH_DISTANCE + " REAL NOT NULL DEFAULT (0), "
            + COLUMN_ROAD_AVG_IRI + " REAL NOT NULL DEFAULT (0), "
            + COLUMN_ROAD_AVG_SPEED + " REAL NOT NULL DEFAULT (0), "
            + COLUMN_ROAD_STAT_BAD_ISSUES + " REAL NOT NULL DEFAULT (0), "
            + COLUMN_ROAD_STAT_NORMAL_ISSUES + " REAL NOT NULL DEFAULT (0), "
            + COLUMN_ROAD_STAT_GOOD_ISSUES + " REAL NOT NULL DEFAULT (0), "
            + COLUMN_ROAD_STAT_PERFECT_ISSUES + " REAL NOT NULL DEFAULT (0), "
            + COLUMN_ROAD_DEFAULT + " INTEGER NOT NULL DEFAULT (0), "
            + COLUMN_ROAD_UPLOADED + " INTEGER NOT NULL DEFAULT (0) "
            + ");";

    public static final String SQL_DROP_TABLE_ROADS = "DROP TABLE IF EXISTS " + DataBaseHelper.TABLE_ROADS;


    public long putRoad(final RoadModel data) {
        long insertId = -1;
        SQLiteDatabase db = getDatabase();
        try {
            db.beginTransaction();
            final ContentValues values = getContentValues(data);
            insertId = db.insert(DataBaseHelper.TABLE_ROADS, null, values);
            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.e(TAG, e != null && e.getMessage() != null ? e.getMessage() : "");
        } finally {
            db.endTransaction();
        }
        return insertId;
    }

    public long getAllItemsCount() {
        return getAllItemsCount(DataBaseHelper.TABLE_ROADS);
    }

    public long getAllItemsCountByFolderId(long folderId) {
        String selection = COLUMN_ROAD_FOLDER_ID + "=" + folderId;
        return getAllItemsCountForId(DataBaseHelper.TABLE_ROADS, selection);
    }

    private ContentValues getContentValues(final RoadModel data) {
        final ContentValues values = new ContentValues();
        values.put(COLUMN_ROAD_FOLDER_ID, data.getFolderId());
        values.put(COLUMN_ROAD_TIME, data.getTime());
        values.put(COLUMN_ROAD_DATE, data.getDate());
        values.put(COLUMN_ROAD_NAME, data.getName());
        values.put(COLUMN_ROAD_EXPERIMENTS, data.getExperiments());
        values.put(COLUMN_ROAD_ALL_DISTANCE, data.getOverallDistance());
        values.put(COLUMN_ROAD_PATH_DISTANCE, data.getPathDistance());
        values.put(COLUMN_ROAD_AVG_IRI, data.getAverageIRI());
        values.put(COLUMN_ROAD_AVG_SPEED, data.getAverageSpeed());
        values.put(COLUMN_ROAD_STAT_BAD_ISSUES, data.getStatBadIssues());
        values.put(COLUMN_ROAD_STAT_NORMAL_ISSUES, data.getStatNormalIssues());
        values.put(COLUMN_ROAD_STAT_GOOD_ISSUES, data.getStatGoodIssues());
        values.put(COLUMN_ROAD_STAT_PERFECT_ISSUES, data.getStatPerfectIssues());
        values.put(COLUMN_ROAD_DEFAULT, data.isDefaultRoad() ? 1 : 0);
        values.put(COLUMN_ROAD_UPLOADED, data.isUploaded() ? 1 : 0);
        return values;
    }

    public void putRoadList(final List<RoadModel> items) {
        putRoadList(items, -1);
    }

    public void putRoadList(final List<RoadModel> items, final int recordId) {
        if (recordId >= 0) {
            deleteItemsWithRecordId(recordId);
        }
        for (RoadModel d : items) {
            if (recordId >= 0) {
                d.setId(recordId);
            }
            putRoad(d);
        }
    }

    public List<RoadModel> getAllRoadsByFolderId(long folderId) {
        final List<RoadModel> listRoads = new ArrayList<>();
        final Cursor cursor = getAllRoadCursor();
        if (cursor != null) {
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                final RoadModel roadModel = cursorToRoad(cursor);
                if (roadModel.getFolderId() == folderId) {
                    listRoads.add(roadModel);
                }
                cursor.moveToNext();
            }
            cursor.close();
        }
        return listRoads;
    }

    public RoadModel getRoad(long roadId) {
        return getRoad(-1, roadId);
    }

    public RoadModel getRoad(long folderId, long roadId) {
        RoadModel road = null;
        Cursor cursor = null;
        try {
            if (folderId < 0) {
                cursor = getRoadCursor(roadId);
            } else {
                cursor = getRoadCursor(folderId, roadId);
            }
            cursor.moveToFirst();
            road = cursorToRoad(cursor);
        } catch (Exception e) {
            Log.e(TAG, e != null && e.getMessage() != null ? e.getMessage() : "");
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }
        return road;
    }

    public RoadModel getDefaultRoad() {
        RoadModel road = null;
        Cursor cursor = null;
        try {
            cursor = getDefaultRoadCursor();
            if (cursor != null) {
                cursor.moveToFirst();
                road = cursorToRoad(cursor);
            }
        } catch (Exception e) {
            Log.e(TAG, e != null && e.getMessage() != null ? e.getMessage() : "");
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }
        return road;
    }

    public Cursor searchRoadsByFolderIdCursor(String selection, long folderId) {
        String selString = COLUMN_ROAD_NAME + " LIKE '%" + selection + "%' " + "AND " +
                COLUMN_ROAD_FOLDER_ID + " = " + Long.toString(folderId);
        return getDatabase().query(DataBaseHelper.TABLE_ROADS, allColumns,
                selString, null, null, null, null);
    }

    public void deleteItemsWithRecordId(final long recordId) {
        SQLiteDatabase db = getDatabase();
        try {
            db.beginTransaction();
            db.delete(DataBaseHelper.TABLE_ROADS, COLUMN_ROAD_ID + " = " + recordId, null);
            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.e(TAG, e != null && e.getMessage() != null ? e.getMessage() : "");
        } finally {
            db.endTransaction();
        }
    }

    public void deleteItemsWithProjectId(final long projectId) {
        SQLiteDatabase db = getDatabase();
        try {
            db.beginTransaction();
            db.delete(DataBaseHelper.TABLE_ROADS, COLUMN_ROAD_FOLDER_ID + " = " + projectId, null);
            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.e(TAG, e != null && e.getMessage() != null ? e.getMessage() : "");
        } finally {
            db.endTransaction();
        }
    }

    public long updateRoad(final RoadModel road) {
        long insertId = -1;
        SQLiteDatabase db = getDatabase();
        try {
            db.beginTransaction();
            ContentValues values = getContentValues(road);
            insertId = getDatabase().update(DataBaseHelper.TABLE_ROADS, values, COLUMN_ROAD_ID + "=?",
                    new String[]{String.valueOf(road.getId())});
            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.e(TAG, e != null && e.getMessage() != null ? e.getMessage() : "");
        } finally {
            db.endTransaction();
        }
        return insertId;
    }

    public int deleteRoad(final long roadId) {
        int count = 0;
        SQLiteDatabase db = getDatabase();
        try {
            db.beginTransaction();
            count = db.delete(DataBaseHelper.TABLE_ROADS, COLUMN_ROAD_ID + " = " + roadId, null);
            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.e(TAG, e != null && e.getMessage() != null ? e.getMessage() : "");
        } finally {
            db.endTransaction();
        }
        return count;
    }

    public RoadModel cursorToRoad(final Cursor cursor) {
        final RoadModel data = new RoadModel();
        data.setId(cursor.getLong(cursor.getColumnIndex(RoadDAO.COLUMN_ROAD_ID)));
        data.setFolderId(cursor.getLong(cursor.getColumnIndex(RoadDAO.COLUMN_ROAD_FOLDER_ID)));
        data.setTime(cursor.getLong(cursor.getColumnIndex(RoadDAO.COLUMN_ROAD_TIME)));
        data.setDate(cursor.getLong(cursor.getColumnIndex(RoadDAO.COLUMN_ROAD_DATE)));
        data.setName(cursor.getString(cursor.getColumnIndex(RoadDAO.COLUMN_ROAD_NAME)));
        data.setExperiments(cursor.getLong(cursor.getColumnIndex(RoadDAO.COLUMN_ROAD_EXPERIMENTS)));
        data.setOverallDistance(cursor.getDouble(cursor.getColumnIndex(RoadDAO.COLUMN_ROAD_ALL_DISTANCE)));
        data.setPathDistance(cursor.getDouble(cursor.getColumnIndex(RoadDAO.COLUMN_ROAD_PATH_DISTANCE)));
        data.setAverageIRI(cursor.getDouble(cursor.getColumnIndex(RoadDAO.COLUMN_ROAD_AVG_IRI)));
        data.setAverageSpeed(cursor.getDouble(cursor.getColumnIndex(RoadDAO.COLUMN_ROAD_AVG_SPEED)));
        float[] stats = new float[4];
        stats[0] = cursor.getFloat(cursor.getColumnIndex(RoadDAO.COLUMN_ROAD_STAT_BAD_ISSUES));
        stats[1] = cursor.getFloat(cursor.getColumnIndex(RoadDAO.COLUMN_ROAD_STAT_NORMAL_ISSUES));
        stats[2] = cursor.getFloat(cursor.getColumnIndex(RoadDAO.COLUMN_ROAD_STAT_GOOD_ISSUES));
        stats[3] = cursor.getFloat(cursor.getColumnIndex(RoadDAO.COLUMN_ROAD_STAT_PERFECT_ISSUES));
        data.setIssuesStats(stats);
        data.setDefaultRoad(cursor.getInt(cursor.getColumnIndex(RoadDAO.COLUMN_ROAD_DEFAULT)) == 1);
        data.setUploaded(cursor.getInt(cursor.getColumnIndex(RoadDAO.COLUMN_ROAD_UPLOADED)) == 1);
        return data;
    }

    public Cursor getRoadCursor(long folderId, long roadId) {
        return getDatabase().query(DataBaseHelper.TABLE_ROADS, allColumns,
        COLUMN_ROAD_ID + "=? AND " + COLUMN_ROAD_FOLDER_ID + "= ?",
        new String[]{Long.toString(roadId), Long.toString(folderId)}, null, null, null);
    }

    public Cursor getRoadCursor(long roadId) {
        return getDatabase().query(DataBaseHelper.TABLE_ROADS, allColumns,
        COLUMN_ROAD_ID + "=?",
        new String[]{Long.toString(roadId)}, null, null, null);
    }

    public Cursor getDefaultRoadCursor() {
        return getDatabase().query(DataBaseHelper.TABLE_ROADS, allColumns,
        COLUMN_ROAD_DEFAULT + "=1", null, null, null, null);
    }

    public List<RoadModel> getAllRoads() {
        final List<RoadModel> listFolders = new ArrayList<>();
        final Cursor cursor = getAllRoadCursor();
        try {
            if (cursor != null) {
                cursor.moveToFirst();
                while (!cursor.isAfterLast()) {
                    final RoadModel roadModel = cursorToRoad(cursor);
                    listFolders.add(roadModel);
                    cursor.moveToNext();
                }
                cursor.close();
            }
        } catch (Exception e) {
            Log.e(TAG, e != null && e.getMessage() != null ? e.getMessage() : "");
        }
        return listFolders;
    }

    public Cursor getAllRoadCursor() {
        return getDatabase().query(DataBaseHelper.TABLE_ROADS, allColumns, null, null, null, null, null);
    }

    public Cursor getAllRoadByFolderIdCursor(long folderId) {
        Cursor cursor = null;
        try {
            cursor = getDatabase().query(DataBaseHelper.TABLE_ROADS, allColumns,
            COLUMN_ROAD_FOLDER_ID + " = ?",
            new String[]{Long.toString(folderId)}, null, null, null);
            cursor.moveToFirst();
        } catch (Exception e) {
            Log.e(TAG, e != null && e.getMessage() != null ? e.getMessage() : "");
        }
        return cursor;
    }

    public RoadModel getLastRoad(long folderId) {
        Cursor c = getAllRoadByFolderIdCursor(folderId);
        RoadModel road = null;
        try {
            if (c != null) {
                if (c.getCount() > 0) {
                    c.moveToLast();
                    road = cursorToRoad(c);
                }
                c.close();
            }
        } catch (Exception e) {
            Log.e(TAG, e != null && e.getMessage() != null ? e.getMessage() : "");
        }
        return road;
    }

    @Override
    public List<? extends BaseModel> getDataForUpload(final String query) {
        final List<RoadModel> listData = new ArrayList<>();
//        final Cursor cursor = getBumpsForUploadCursor(query);
//        if (cursor != null) {
//            cursor.moveToFirst();
//            while (!cursor.isAfterLast()) {
//                final RoadModel data = cursorToRoad(cursor);
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
        List<RoadModel> castedItems = (List<RoadModel>) items;
        SQLiteDatabase db = getDatabase();
        try {
            db.beginTransaction();
            for (RoadModel item : castedItems) {
                item.setUploaded(flag);
                db.update(DataBaseHelper.TABLE_ROADS, getContentValues(item), COLUMN_ROAD_ID + "=?", new String[]{String.valueOf(item.getId())});
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
        List<RoadModel> castedItems = (List<RoadModel>) items;
        SQLiteDatabase db = getDatabase();
        try {
            db.beginTransaction();
            for (RoadModel item : castedItems) {
                item.setPending(flag);
                db.update(DataBaseHelper.TABLE_ROADS, getContentValues(item), COLUMN_ROAD_ID + "=?", new String[]{String.valueOf(item.getId())});
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
        List<RoadModel> castedItems = (List<RoadModel>) items;
        SQLiteDatabase db = getDatabase();
        try {
            db.beginTransaction();
            for (RoadModel item : castedItems) {
                item.setUploaded(uploaded);
                item.setPending(pending);
                db.update(DataBaseHelper.TABLE_ROADS, getContentValues(item), COLUMN_ROAD_ID + "=?",
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
