package com.softteco.roadlabpro.sqlite.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.softteco.roadlabpro.sqlite.DataBaseHelper;
import com.softteco.roadlabpro.sqlite.model.BaseModel;
import com.softteco.roadlabpro.sqlite.model.FolderModel;

import java.util.ArrayList;
import java.util.List;

public class FolderDAO extends BaseDAO implements UploadDAO {

    public FolderDAO(final Context context) {
        super(context);
    }

    // columns of the folder table
    public static final String COLUMN_FOLDER_ID = "_id";
    public static final String COLUMN_FOLDER_TIME = "time";
    public static final String COLUMN_FOLDER_DATE = "date";
    public static final String COLUMN_FOLDER_NAME = "name";
    public static final String COLUMN_FOLDER_ROADS = "roads";
    public static final String COLUMN_FOLDER_ALL_DISTANCE = "all_distance";
    public static final String COLUMN_FOLDER_PATH_DISTANCE = "path_distance";
    public static final String COLUMN_FOLDER_AVG_IRI = "avg_iri";
    public static final String COLUMN_FOLDER_AVG_SPEED = "avg_speed";
    public static final String COLUMN_FOLDER_DEFAULT = "default_folder";
    public static final String COLUMN_FOLDER_UPLOADED = "uploaded";

    private String[] allColumns = {
            COLUMN_FOLDER_ID,
            COLUMN_FOLDER_TIME,
            COLUMN_FOLDER_DATE,
            COLUMN_FOLDER_NAME,
            COLUMN_FOLDER_ROADS,
            COLUMN_FOLDER_ALL_DISTANCE,
            COLUMN_FOLDER_PATH_DISTANCE,
            COLUMN_FOLDER_AVG_IRI,
            COLUMN_FOLDER_AVG_SPEED,
            COLUMN_FOLDER_DEFAULT,
            COLUMN_FOLDER_UPLOADED,};

    public static final String SQL_CREATE_TABLE_FOLDERS = "CREATE TABLE " + DataBaseHelper.TABLE_FOLDERS + "("
            + COLUMN_FOLDER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + COLUMN_FOLDER_TIME + " INTEGER NOT NULL, "
            + COLUMN_FOLDER_DATE + " INTEGER NOT NULL, "
            + COLUMN_FOLDER_NAME + " TEXT, "
            + COLUMN_FOLDER_ROADS + " INTEGER NOT NULL DEFAULT (0), "
            + COLUMN_FOLDER_ALL_DISTANCE + " REAL NOT NULL DEFAULT (0), "
            + COLUMN_FOLDER_PATH_DISTANCE + " REAL NOT NULL DEFAULT (0), "
            + COLUMN_FOLDER_AVG_IRI + " REAL NOT NULL DEFAULT (0), "
            + COLUMN_FOLDER_AVG_SPEED + " REAL NOT NULL DEFAULT (0), "
            + COLUMN_FOLDER_DEFAULT + " INTEGER NOT NULL DEFAULT (0), "
            + COLUMN_FOLDER_UPLOADED + " INTEGER NOT NULL DEFAULT (0) "
            + ");";

    public static final String SQL_DROP_TABLE_FOLDERS = "DROP TABLE IF EXISTS " + DataBaseHelper.TABLE_FOLDERS;

    public long putFolder(final FolderModel data) {
        long insertId = -1;
        SQLiteDatabase db = getDatabase();
        try {
            db.beginTransaction();
            final ContentValues values = getContentValues(data);
            insertId = db.insert(DataBaseHelper.TABLE_FOLDERS, null, values);
            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.e(TAG, e != null && e.getMessage() != null ? e.getMessage() : "");
        } finally {
            db.endTransaction();
        }
        return insertId;
    }

    public long getAllItemsCount() {
        return getAllItemsCount(DataBaseHelper.TABLE_FOLDERS);
    }

    private ContentValues getContentValues(final FolderModel data) {
        final ContentValues values = new ContentValues();
        values.put(COLUMN_FOLDER_TIME, data.getTime());
        values.put(COLUMN_FOLDER_DATE, data.getDate());
        values.put(COLUMN_FOLDER_NAME, data.getName());
        values.put(COLUMN_FOLDER_ROADS, data.getRoads());
        values.put(COLUMN_FOLDER_ALL_DISTANCE, data.getOverallDistance());
        values.put(COLUMN_FOLDER_PATH_DISTANCE, data.getPathDistance());
        values.put(COLUMN_FOLDER_AVG_IRI, data.getAverageIRI());
        values.put(COLUMN_FOLDER_AVG_SPEED, data.getAverageSpeed());
        values.put(COLUMN_FOLDER_DEFAULT, data.isDefaultProject() ? 1 : 0);
        values.put(COLUMN_FOLDER_UPLOADED, data.isUploaded() ? 1 : 0);
        return values;
    }

    public void putFolderList(final List<FolderModel> items) {
        putFolderList(items, -1);
    }

    public void putFolderList(final List<FolderModel> items, final long recordId) {
        if (recordId >= 0) {
            deleteItemsWithRecordId(recordId);
        }
        for (FolderModel d : items) {
            if (recordId >= 0) {
                d.setId(recordId);
            }
            putFolder(d);
        }
    }

    public List<FolderModel> getAllFolders() {
        final List<FolderModel> listFolders = new ArrayList<>();
        final Cursor cursor = getAllFolderCursor();
        try {
            if (cursor != null) {
                cursor.moveToFirst();
                while (!cursor.isAfterLast()) {
                    final FolderModel folderModel = cursorToFolder(cursor);
                    listFolders.add(folderModel);
                    cursor.moveToNext();
                }
                cursor.close();
            }
        } catch (Exception e) {
            Log.e(TAG, e != null && e.getMessage() != null ? e.getMessage() : "");
        }
        return listFolders;
    }

    public FolderModel getFolder(long folderId) {
        FolderModel folder = null;
        Cursor cursor = null;
        try {
          cursor = getFolderCursor(folderId);
            if (cursor != null) {
                cursor.moveToFirst();
                folder = cursorToFolder(cursor);
            }
        } catch (Exception e) {
            Log.e(TAG, e != null && e.getMessage() != null ? e.getMessage() : "");
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }
        return folder;
    }

    public FolderModel getDefaultFolder() {
        FolderModel folder = null;
        Cursor cursor = null;
        try {
            cursor = getDefaultFolderCursor();
            if (cursor != null) {
                cursor.moveToFirst();
                folder = cursorToFolder(cursor);
            }
        } catch (Exception e) {
            Log.e(TAG, e != null && e.getMessage() != null ? e.getMessage() : "");
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }
        return folder;
    }

    public Cursor searchFoldersCursor(String selection) {
        String selString = COLUMN_FOLDER_NAME + " LIKE '%" + selection + "%'";
        return getDatabase().query(DataBaseHelper.TABLE_FOLDERS, allColumns,
                selString, null, null, null, null);
    }

    public void deleteItemsWithRecordId(final long recordId) {
        SQLiteDatabase db = getDatabase();
        try {
            db.beginTransaction();
            db.delete(DataBaseHelper.TABLE_FOLDERS, COLUMN_FOLDER_ID + " = " + recordId, null);
            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.e(TAG, e != null && e.getMessage() != null ? e.getMessage() : "");
        } finally {
            db.endTransaction();
        }
    }

    public long updateFolder(final FolderModel folder) {
        long insertId = -1;
        SQLiteDatabase db = getDatabase();
        try {
            db.beginTransaction();
            ContentValues values = getContentValues(folder);
            insertId = getDatabase().update(DataBaseHelper.TABLE_FOLDERS, values, COLUMN_FOLDER_ID + "=?",
                    new String[]{String.valueOf(folder.getId())});
            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.e(TAG, e != null && e.getMessage() != null ? e.getMessage() : "");
        } finally {
            db.endTransaction();
        }
        return insertId;
    }

    public int deleteFolder(final long folderId) {
        int count = 0;
        SQLiteDatabase db = getDatabase();
        try {
            db.beginTransaction();
            count = db.delete(DataBaseHelper.TABLE_FOLDERS, COLUMN_FOLDER_ID + " = " + folderId, null);
            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.e(TAG, e != null && e.getMessage() != null ? e.getMessage() : "");
        } finally {
            db.endTransaction();
        }
        return count;
    }

//    public FolderModel getFolderByRecordId(final long id) {
//        final Cursor cursor = getFolderByRecordIdCursor(id);
//        final FolderModel record = cursorToFolder(cursor);
//        return record;
//    }

//    public Cursor getFolderByRecordIdCursor(final long recordId) {
//        final Cursor cursor = getDatabase().query(DataBaseHelper.TABLE_FOLDERS, allColumns,
//                COLUMN_BUMP_RECORD_ID + " = ?",
//                new String[]{String.valueOf(recordId)}, null, null, null);
//        cursor.moveToFirst();
//        return cursor;
//    }

    public FolderModel cursorToFolder(final Cursor cursor) {
        final FolderModel data = new FolderModel();
        data.setId(cursor.getLong(cursor.getColumnIndex(FolderDAO.COLUMN_FOLDER_ID)));
        data.setTime(cursor.getLong(cursor.getColumnIndex(FolderDAO.COLUMN_FOLDER_TIME)));
        data.setDate(cursor.getLong(cursor.getColumnIndex(FolderDAO.COLUMN_FOLDER_DATE)));
        data.setRoads(cursor.getLong(cursor.getColumnIndex(FolderDAO.COLUMN_FOLDER_ROADS)));
        data.setOverallDistance(cursor.getDouble(cursor.getColumnIndex(FolderDAO.COLUMN_FOLDER_ALL_DISTANCE)));
        data.setPathDistance(cursor.getDouble(cursor.getColumnIndex(FolderDAO.COLUMN_FOLDER_PATH_DISTANCE)));
        data.setAverageIRI(cursor.getDouble(cursor.getColumnIndex(FolderDAO.COLUMN_FOLDER_AVG_IRI)));
        data.setAverageSpeed(cursor.getDouble(cursor.getColumnIndex(FolderDAO.COLUMN_FOLDER_AVG_SPEED)));
        data.setName(cursor.getString(cursor.getColumnIndex(FolderDAO.COLUMN_FOLDER_NAME)));
        data.setDefaultProject(cursor.getInt(cursor.getColumnIndex(FolderDAO.COLUMN_FOLDER_DEFAULT)) == 1);
        data.setUploaded(cursor.getInt(cursor.getColumnIndex(FolderDAO.COLUMN_FOLDER_UPLOADED)) == 1);
        return data;
    }

    public Cursor getAllFolderCursor() {
        Cursor cursor = null;
        try {
            cursor = getDatabase().query(DataBaseHelper.TABLE_FOLDERS, allColumns, null, null, null, null, null);
        } catch (Exception e) {
            Log.e(TAG, e != null && e.getMessage() != null ? e.getMessage() : "");
        }
        return cursor;
    }

    public Cursor getFolderCursor(long folderId) {
        return getDatabase().query(DataBaseHelper.TABLE_FOLDERS, allColumns, COLUMN_FOLDER_ID + " = " + folderId, null, null, null, null);
    }

    public Cursor getDefaultFolderCursor() {
        return getDatabase().
        query(DataBaseHelper.TABLE_FOLDERS, allColumns, COLUMN_FOLDER_DEFAULT + "=1", null, null, null, null);
    }

//    private Cursor getBumpsForUploadCursor(final String selection) {
//        String sel = COLUMN_FOLDER_UPLOADED + "=0 and " + COLUMN_BUMP_PENDING + "=0";
//        if (selection != null) {
//            sel += " and " + selection;
//        }
//        return getDatabase().query(DataBaseHelper.TABLE_BUMPS, allColumns, sel, null, null, null, null);
//    }

    @Override
    public List<? extends BaseModel> getDataForUpload(final String query) {
        final List<FolderModel> listData = new ArrayList<>();
//        final Cursor cursor = getBumpsForUploadCursor(query);
//        if (cursor != null) {
//            cursor.moveToFirst();
//            while (!cursor.isAfterLast()) {
//                final FolderModel data = cursorToFolder(cursor);
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
        List<FolderModel> castedItems = (List<FolderModel>) items;
        SQLiteDatabase db = getDatabase();
        try {
            db.beginTransaction();
            for (FolderModel item : castedItems) {
                item.setUploaded(flag);
                db.update(DataBaseHelper.TABLE_FOLDERS, getContentValues(item), COLUMN_FOLDER_ID + "=?", new String[]{String.valueOf(item.getId())});
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
        List<FolderModel> castedItems = (List<FolderModel>) items;
        SQLiteDatabase db = getDatabase();
        try {
            db.beginTransaction();
            for (FolderModel item : castedItems) {
                item.setPending(flag);
                db.update(DataBaseHelper.TABLE_FOLDERS, getContentValues(item), COLUMN_FOLDER_ID + "=?", new String[]{String.valueOf(item.getId())});
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
        List<FolderModel> castedItems = (List<FolderModel>) items;
        SQLiteDatabase db = getDatabase();
        try {
            db.beginTransaction();
            for (FolderModel item : castedItems) {
                item.setUploaded(uploaded);
                item.setPending(pending);
                db.update(DataBaseHelper.TABLE_FOLDERS, getContentValues(item), COLUMN_FOLDER_ID + "=?",
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
