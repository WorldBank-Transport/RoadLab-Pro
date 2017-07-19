package com.softteco.roadlabpro.sqlite.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.softteco.roadlabpro.sqlite.DataBaseHelper;
import com.softteco.roadlabpro.sqlite.model.BaseModel;
import com.softteco.roadlabpro.sqlite.model.IssuesModel;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Vadim Alenin on 5/5/2015.
 */
public class IssuesDAO extends BaseDAO implements UploadDAO {

    public static final String COLUMN_ISSUE_ID = "_id";
    public static final String COLUMN_FIRST_IMAGE = "first_image";
    public static final String COLUMN_SECOND_IMAGE = "second_image";
    public static final String COLUMN_THIRD_IMAGE = "third_image";
    public static final String COLUMN_TYPE = "type";
    public static final String COLUMN_LAT = "lat";
    public static final String COLUMN_LNG = "lng";
    public static final String COLUMN_DATE = "date";
    public static final String COLUMN_TIME = "time";
    public static final String COLUMN_NOTES = "notes";
    public static final String COLUMN_UPLOADED = "uploaded";
    public static final String COLUMN_OWN_REQUEST = "is_own_request";
    public static final String COLUMN_UNIQUE = "unique_id";

    public static final String[] allColumns = {
            COLUMN_ISSUE_ID,
            COLUMN_FIRST_IMAGE,
            COLUMN_SECOND_IMAGE,
            COLUMN_THIRD_IMAGE,
            COLUMN_TYPE,
            COLUMN_LAT,
            COLUMN_LNG,
            COLUMN_DATE,
            COLUMN_TIME,
            COLUMN_UPLOADED,
            COLUMN_OWN_REQUEST,
            COLUMN_NOTES,
            COLUMN_UNIQUE};

    public static final String SQL_CREATE_TABLE_ISSUE = "CREATE TABLE " + DataBaseHelper.TABLE_ISSUES + "("
            + COLUMN_ISSUE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + COLUMN_FIRST_IMAGE + " TEXT, "
            + COLUMN_SECOND_IMAGE + " TEXT, "
            + COLUMN_THIRD_IMAGE + " TEXT, "
            + COLUMN_TYPE + " TEXT, "
            + COLUMN_LAT + " REAL, "
            + COLUMN_LNG + " REAL, "
            + COLUMN_DATE + " INTEGER, "
            + COLUMN_TIME + " INTEGER, "
            + COLUMN_UPLOADED + " INTEGER NOT NULL DEFAULT (0), "
            + COLUMN_OWN_REQUEST + " INTEGER NOT NULL DEFAULT (1), "
            + COLUMN_NOTES + " TEXT, "
            + COLUMN_UNIQUE + " TEXT "
            + ");";

    public static final String SQL_DROP_TABLE_ISSUE = "DROP TABLE IF EXISTS " + DataBaseHelper.TABLE_ISSUES;

    public IssuesDAO(Context context) {
        super(context);
    }


    ContentValues getContentValues(final IssuesModel issues) {
        final ContentValues values = new ContentValues();
        values.put(COLUMN_FIRST_IMAGE, issues.getImages()[0]);
        values.put(COLUMN_SECOND_IMAGE, issues.getImages()[1]);
        values.put(COLUMN_THIRD_IMAGE, issues.getImages()[2]);
        values.put(COLUMN_TYPE, issues.getTypeIssues().name());
        values.put(COLUMN_LAT, issues.getLatitude());
        values.put(COLUMN_LNG, issues.getLongitude());
        values.put(COLUMN_DATE, issues.getDate());
        values.put(COLUMN_TIME, issues.getTime());
        values.put(COLUMN_UPLOADED, issues.isUploaded() ? 1 : 0);
        values.put(COLUMN_OWN_REQUEST, issues.isOwn() ? 1 : 0);
        values.put(COLUMN_NOTES, issues.getNotes());
        values.put(COLUMN_UNIQUE, issues.getUniqueId());
        return values;
    }

    public long putIssue(final IssuesModel issues) {
        long insertId = -1;
        SQLiteDatabase db = getDatabase();
        try {
            db.beginTransaction();
            ContentValues values = getContentValues(issues);
            insertId = getDatabase().insert(DataBaseHelper.TABLE_ISSUES, null, values);
            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.e(TAG, e != null && e.getMessage() != null ? e.getMessage() : "");
        } finally {
            db.endTransaction();
        }
        return insertId;
    }

    public long updateIssue(final IssuesModel issues) {
        long insertId = -1;
        SQLiteDatabase db = getDatabase();
        try {
            db.beginTransaction();
            ContentValues values = getContentValues(issues);
            insertId = getDatabase().update(DataBaseHelper.TABLE_ISSUES, values, COLUMN_ISSUE_ID + "=?",
                    new String[]{String.valueOf(issues.getId())});
            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.e(TAG, e != null && e.getMessage() != null ? e.getMessage() : "");
        } finally {
            db.endTransaction();
        }
        return insertId;
    }

    public void deleteIssues(final IssuesModel issuesModel) {
        long issuesId = issuesModel.getId();
        SQLiteDatabase db = getDatabase();
        try {
            db.beginTransaction();
            db.delete(DataBaseHelper.TABLE_ISSUES, COLUMN_ISSUE_ID + " = " + issuesId, null);
            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.e(TAG, e != null && e.getMessage() != null ? e.getMessage() : "");
        } finally {
            db.endTransaction();
        }
    }

    public List<IssuesModel> getAllIssues() {
        final List<IssuesModel> listIssues = new ArrayList<>();
        final Cursor cursor = getAllIssuesCursor();
        if (cursor != null) {
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                final IssuesModel issuesModel = cursorToIssues(cursor);
                listIssues.add(issuesModel);
                cursor.moveToNext();
            }
            cursor.close();
        }
        return listIssues;
    }

    public Cursor getAllIssuesCursor() {
        return getDatabase().query(DataBaseHelper.TABLE_ISSUES, allColumns,
                null, null, null, null, null);
    }

    public Cursor getAllOwnIssuesCursor() {
        String sel = COLUMN_OWN_REQUEST + "=1";
        return getDatabase().query(DataBaseHelper.TABLE_ISSUES, allColumns, sel, null, null, null, null);
    }

    public Cursor searchIssuesCursor(String selection) {
        String selString = COLUMN_NOTES + " LIKE '%" + selection + "%'";
        return getDatabase().query(DataBaseHelper.TABLE_ISSUES, allColumns,
                selString, null, null, null, null);
    }

    public IssuesModel getIssuesModelById(final long id) {
        final Cursor cursor = getDatabase().query(DataBaseHelper.TABLE_ISSUES, allColumns,
                COLUMN_ISSUE_ID + " = ?",
                new String[]{String.valueOf(id)}, null, null, null);
        if (cursor != null) {
            cursor.moveToFirst();
        }

        final IssuesModel issues = cursorToIssues(cursor);
        return issues;
    }

    public IssuesModel cursorToIssues(final Cursor cursor) {
        final IssuesModel issueModel = new IssuesModel();
        issueModel.setId(cursor.getLong(cursor.getColumnIndex(COLUMN_ISSUE_ID)));
        issueModel.setDate(cursor.getLong(cursor.getColumnIndex(COLUMN_DATE)));
        issueModel.setTime(cursor.getLong(cursor.getColumnIndex(COLUMN_TIME)));
        issueModel.setUploaded(cursor.getInt(cursor.getColumnIndex(COLUMN_UPLOADED)) == 1);
        issueModel.setOwn(cursor.getInt(cursor.getColumnIndex(COLUMN_OWN_REQUEST)) == 1);
        issueModel.setLatitude(cursor.getDouble(cursor.getColumnIndex(COLUMN_LAT)));
        issueModel.setLongitude(cursor.getDouble(cursor.getColumnIndex(COLUMN_LNG)));
        issueModel.setNotes(cursor.getString(cursor.getColumnIndex(COLUMN_NOTES)));
        issueModel.setUniqueId(cursor.getString(cursor.getColumnIndex(COLUMN_UNIQUE)));

        final String typeIssues = cursor.getString(cursor.getColumnIndex(COLUMN_TYPE));
        if (IssuesModel.TypeIssues.BROKEN.name().equalsIgnoreCase(typeIssues)) {
            issueModel.setTypeIssues(IssuesModel.TypeIssues.BROKEN);
        } else {
            if (IssuesModel.TypeIssues.PIT_ON_THE_ROAD.name().equalsIgnoreCase(typeIssues)) {
                issueModel.setTypeIssues(IssuesModel.TypeIssues.PIT_ON_THE_ROAD);
            } else {
                if (IssuesModel.TypeIssues.LUKE.name().equalsIgnoreCase(typeIssues)) {
                    issueModel.setTypeIssues(IssuesModel.TypeIssues.LUKE);
                } else {
                    if (IssuesModel.TypeIssues.PIT_IN_THE_YARD.name().equalsIgnoreCase(typeIssues)) {
                        issueModel.setTypeIssues(IssuesModel.TypeIssues.PIT_IN_THE_YARD);
                    } else {
                        if (IssuesModel.TypeIssues.RAILS.name().equalsIgnoreCase(typeIssues)) {
                            issueModel.setTypeIssues(IssuesModel.TypeIssues.RAILS);
                        } else {
                            if (IssuesModel.TypeIssues.ACCIDENT.name().equalsIgnoreCase(typeIssues)) {
                                issueModel.setTypeIssues(IssuesModel.TypeIssues.ACCIDENT);
                            } else {
                                issueModel.setTypeIssues(IssuesModel.TypeIssues.OTHER);
                            }
                        }
                    }
                }
            }
        }

        final String firstURL = cursor.getString(cursor.getColumnIndex(COLUMN_FIRST_IMAGE));
        final String secondURL = cursor.getString(cursor.getColumnIndex(COLUMN_SECOND_IMAGE));
        final String thirdURL = cursor.getString(cursor.getColumnIndex(COLUMN_THIRD_IMAGE));

        issueModel.setImages(new String[]{firstURL, secondURL, thirdURL});

        return issueModel;
    }

    @Override
    public List<IssuesModel> getDataForUpload(final String query) {
        final List<IssuesModel> listData = new ArrayList<>();
        final Cursor cursor = getProcessedDataForUploadCursor(query);
        if (cursor != null) {
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                final IssuesModel data = cursorToIssues(cursor);
                listData.add(data);
                cursor.moveToNext();
            }
            cursor.close();
        }
        return listData;
    }

    @Override
    public List<? extends BaseModel> getDataForUpload() {
        return getAllIssues();
    }

    @Override
    public boolean updateUploadedDB(List<? extends BaseModel> items, boolean flag) {
        Boolean result = false;
        List<IssuesModel> castedItems = (List<IssuesModel>) items;
        SQLiteDatabase db = getDatabase();
        try {
            db.beginTransaction();
            for (IssuesModel item : castedItems) {
                item.setUploaded(flag);
                db.update(DataBaseHelper.TABLE_ISSUES, getContentValues(item), COLUMN_ISSUE_ID + "=?", new String[]{String.valueOf(item.getId())});
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
        return false;
    }

    @Override
    public boolean updateUploadedPendingDB(List<? extends BaseModel> items, boolean uploaded, boolean pending) {
        return false;
    }

    @Override
    public boolean putDB(List<? extends BaseModel> items) {
        Boolean result = false;
        List<IssuesModel> castedItems = (List<IssuesModel>) items;
        SQLiteDatabase db = getDatabase();
        try {
            db.beginTransaction();
            for (IssuesModel item : castedItems) {
                item.setUploaded(true);
                ContentValues values = getContentValues(item);
                db.insert(DataBaseHelper.TABLE_ISSUES, null, values);
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

    private Cursor getProcessedDataForUploadCursor(final String selection) {
        String sel = COLUMN_UPLOADED + "=0";
        if (selection != null) {
            sel += " and " + selection;
        }
        return getDatabase().query(DataBaseHelper.TABLE_ISSUES, allColumns, sel, null, null, null, null);
    }
}
