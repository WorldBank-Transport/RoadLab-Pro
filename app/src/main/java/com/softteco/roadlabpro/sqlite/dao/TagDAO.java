package com.softteco.roadlabpro.sqlite.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.softteco.roadlabpro.sqlite.DataBaseHelper;
import com.softteco.roadlabpro.sqlite.model.BaseModel;
import com.softteco.roadlabpro.sqlite.model.MeasurementItem;
import com.softteco.roadlabpro.sqlite.model.RoadModel;
import com.softteco.roadlabpro.sqlite.model.TagModel;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Aleksey on 17.04.2015.
 */
public class TagDAO extends BaseDAO implements UploadDAO {

    public TagDAO(final Context context) {
        super(context);
    }

    // columns of the bump table
    public static final String COLUMN_TAG_ID = RecordDAO.COLUMN_RECORD_ID;
    public static final String COLUMN_TAG_FOLDER_ID = "folder_id";
    public static final String COLUMN_TAG_ROAD_ID = "road_id";
    public static final String COLUMN_TAG_MEASUREMENT_ID = "measurement_id";
    public static final String COLUMN_TAG_TIME = "time";
    public static final String COLUMN_TAG_SPEED = "speed";
    public static final String COLUMN_TAG_NAME = "name";
    public static final String COLUMN_TAG_DESCRIPTION = "description";
    public static final String COLUMN_TAG_LATITUDE = "latitude";
    public static final String COLUMN_TAG_LONGITUDE = "longitude";
    public static final String COLUMN_TAG_ALTITUDE = "altitude";
    public static final String COLUMN_TAG_UPLOADED = "uploaded";
    public static final String COLUMN_TAG_PENDING = "pending";
    public static final String COLUMN_TAG_FIRST_IMAGE = "first_image";
    public static final String COLUMN_TAG_SECOND_IMAGE = "second_image";
    public static final String COLUMN_TAG_THIRD_IMAGE = "third_image";
    public static final String COLUMN_TAG_AUDIO = "tag_audio";
    public static final String COLUMN_TAG_NOTES = "notes";
    public static final String COLUMN_TAG_SINGLE = "single"; //without measurement
    public static final String COLUMN_TAG_ROAD_CONDITION = "road_condition";
    public static final String COLUMN_TAG_IRI = "iri";

    private String[] allColumns = {
            COLUMN_TAG_ID,
            COLUMN_TAG_FOLDER_ID,
            COLUMN_TAG_ROAD_ID,
            COLUMN_TAG_MEASUREMENT_ID,
            COLUMN_TAG_TIME,
            COLUMN_TAG_SPEED,
            COLUMN_TAG_NAME,
            COLUMN_TAG_DESCRIPTION,
            COLUMN_TAG_LATITUDE,
            COLUMN_TAG_LONGITUDE,
            COLUMN_TAG_ALTITUDE,
            COLUMN_TAG_UPLOADED,
            COLUMN_TAG_PENDING,
            COLUMN_TAG_FIRST_IMAGE,
            COLUMN_TAG_SECOND_IMAGE,
            COLUMN_TAG_THIRD_IMAGE,
            COLUMN_TAG_AUDIO,
            COLUMN_TAG_NOTES,
            COLUMN_TAG_SINGLE,
            COLUMN_TAG_ROAD_CONDITION,
            COLUMN_TAG_IRI};

    public static final String SQL_CREATE_TABLE_TAGS = "CREATE TABLE " + DataBaseHelper.TABLE_TAGS + "("
            + COLUMN_TAG_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + COLUMN_TAG_FOLDER_ID + " INTEGER NOT NULL, "
            + COLUMN_TAG_ROAD_ID + " INTEGER NOT NULL, "
            + COLUMN_TAG_MEASUREMENT_ID + " INTEGER NOT NULL, "
            + COLUMN_TAG_TIME + " INTEGER NOT NULL, "
            + COLUMN_TAG_SPEED + " REAL NOT NULL, "
            + COLUMN_TAG_NAME + " TEXT, "
            + COLUMN_TAG_DESCRIPTION + " TEXT, "
            + COLUMN_TAG_LATITUDE + " REAL NOT NULL, "
            + COLUMN_TAG_LONGITUDE + " REAL NOT NULL, "
            + COLUMN_TAG_ALTITUDE + " REAL NOT NULL, "
            + COLUMN_TAG_UPLOADED + " INTEGER NOT NULL DEFAULT (0), "
            + COLUMN_TAG_PENDING + " INTEGER NOT NULL DEFAULT (0), "
            + COLUMN_TAG_FIRST_IMAGE + " TEXT, "
            + COLUMN_TAG_SECOND_IMAGE + " TEXT, "
            + COLUMN_TAG_THIRD_IMAGE + " TEXT, "
            + COLUMN_TAG_AUDIO + " TEXT, "
            + COLUMN_TAG_NOTES + " TEXT, "
            + COLUMN_TAG_SINGLE + " INTEGER NOT NULL DEFAULT (0), "
            + COLUMN_TAG_ROAD_CONDITION + " INTEGER, "
            + COLUMN_TAG_IRI + " REAL NOT NULL "
            + ");";

    public static final String SQL_DROP_TABLE_TAGS = "DROP TABLE IF EXISTS " + DataBaseHelper.TABLE_TAGS;

    public long put(final TagModel data) {
        long insertId = -1;
        SQLiteDatabase db = getDatabase();
        try {
            db.beginTransaction();
            final ContentValues values = getContentValues(data);
            insertId = db.insert(DataBaseHelper.TABLE_TAGS, null, values);
            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.e(TAG, e != null && e.getMessage() != null ? e.getMessage() : "");
        } finally {
            db.endTransaction();
        }
        return insertId;
    }

    private ContentValues getContentValues(final TagModel data) {
        final ContentValues values = new ContentValues();
        values.put(COLUMN_TAG_FOLDER_ID, data.getFolderId());
        values.put(COLUMN_TAG_ROAD_ID, data.getRoadId());
        values.put(COLUMN_TAG_MEASUREMENT_ID, data.getMeasurementId());
        values.put(COLUMN_TAG_TIME, data.getTime());
        values.put(COLUMN_TAG_SPEED, data.getSpeed());
        values.put(COLUMN_TAG_NAME, data.getName());
        values.put(COLUMN_TAG_DESCRIPTION, data.getDescription());
        values.put(COLUMN_TAG_LATITUDE, data.getLatitude());
        values.put(COLUMN_TAG_LONGITUDE, data.getLongitude());
        values.put(COLUMN_TAG_ALTITUDE, data.getAltitude());
        values.put(COLUMN_TAG_UPLOADED, data.isUploaded() ? 1 : 0);
        values.put(COLUMN_TAG_PENDING, data.isPending() ? 1 : 0);
        values.put(COLUMN_TAG_FIRST_IMAGE, data.getImages()[0]);
        values.put(COLUMN_TAG_SECOND_IMAGE, data.getImages()[1]);
        values.put(COLUMN_TAG_THIRD_IMAGE, data.getImages()[2]);
        values.put(COLUMN_TAG_AUDIO, data.getAudioFile());
        values.put(COLUMN_TAG_NOTES, data.getNotes());
        values.put(COLUMN_TAG_SINGLE, data.isSingle() ? 1 : 0);
        values.put(COLUMN_TAG_ROAD_CONDITION, data.getRoadCondition().getId());
        values.put(COLUMN_TAG_IRI, data.getIri());
        return values;
    }

    public void putList(final List<TagModel> items) {
        putList(items, -1);
    }

    public void putList(final List<TagModel> items, final long measurementId) {
        if (measurementId >= 0) {
            deleteItemsWithMeasurementId(measurementId);
        }
        for (TagModel d : items) {
            if (measurementId >= 0) {
                d.setMeasurementId(measurementId);
            }
            put(d);
        }
    }

    public long getAllItemsCount(long folderId, long roadId, long measurementId) {
        String queryStr = getQueryStr(folderId, roadId, measurementId);
        return getAllItemsCountForId(DataBaseHelper.TABLE_TAGS, queryStr);
    }

    public TagModel getLastTag() {
        Cursor c = getAllTagsCursor();
        TagModel tag = null;
        try {
            if (c != null) {
                if (c.getCount() > 0) {
                    c.moveToLast();
                    tag = cursorToTag(c);
                }
                c.close();
            }
        } catch (Exception e) {
            Log.e(TAG, e != null && e.getMessage() != null ? e.getMessage() : "");
        }
        return tag;
    }

    public void deleteItemsWithMeasurementId(final long measurementId) {
        SQLiteDatabase db = getDatabase();
        try {
            db.beginTransaction();
            db.delete(DataBaseHelper.TABLE_TAGS, COLUMN_TAG_MEASUREMENT_ID + " = " + measurementId, null);
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
            db.delete(DataBaseHelper.TABLE_TAGS, COLUMN_TAG_ROAD_ID + " = " + roadId, null);
            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.e(TAG, e != null && e.getMessage() != null ? e.getMessage() : "");
        } finally {
            db.endTransaction();
        }
    }

    public int delete(final TagModel data) {
        long id = data.getId();
        int count = 0;
        SQLiteDatabase db = getDatabase();
        try {
            db.beginTransaction();
            count = db.delete(DataBaseHelper.TABLE_TAGS, COLUMN_TAG_ID + " = " + id, null);
            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.e(TAG, e != null && e.getMessage() != null ? e.getMessage() : "");
        } finally {
            db.endTransaction();
        }
        return count;
    }

    public Cursor getItemsCursor(long folderId, long roadId, long measurementId) {
        String queryStr = getQueryStr(folderId, roadId, measurementId);
        Cursor cursor = null;
        try {
            cursor = getDatabase().query(DataBaseHelper.TABLE_TAGS,
            allColumns, queryStr, null, null, null, null);
            cursor.moveToFirst();
        } catch (Exception e) {
            Log.e(TAG, e != null && e.getMessage() != null ? e.getMessage() : "");
        }
        return cursor;
    }

    public void moveTags(long folderId, long roadId, long measurementId) {
        Cursor cursor = getItemsCursor(-1, -1, measurementId);
        if (cursor != null) {
            boolean moveToNext = cursor.getCount() > 0;
            TagModel tag = null;
            while(moveToNext) {
                tag = cursorToTag(cursor);
                moveTag(folderId, roadId, measurementId, tag);
                moveToNext = cursor.moveToNext();
            }
            cursor.close();
        }
    }

    public void moveTag(long folderId, long roadId, long measurementId, TagModel tag) {
        if (folderId >= 0) {
            tag.setFolderId(folderId);
        }
        if (roadId >= 0) {
            tag.setRoadId(roadId);
        }
        if (measurementId >= 0) {
            tag.setMeasurementId(measurementId);
        }
        updateItem(tag);
    }

    public TagModel getItemById(final long id) {
        TagModel tag = null;
        try {
            final Cursor cursor = getTagByIdCursor(id);
            tag = cursorToTag(cursor);
        } catch (Exception e) {
            Log.e(TAG, e != null && e.getMessage() != null ? e.getMessage() : "");
        }
        return tag;
    }

    public Cursor getTagByIdCursor(final long tagId) {
        final Cursor cursor = getDatabase().query(DataBaseHelper.TABLE_TAGS, allColumns,
                COLUMN_TAG_ID + " = ?",
                new String[]{String.valueOf(tagId)}, null, null, null);
        cursor.moveToFirst();
        return cursor;
    }

    public int updateItem(final TagModel tag) {
        int rows = 0;
        SQLiteDatabase db = getDatabase();
        try {
            db.beginTransaction();
            ContentValues values = getContentValues(tag);
            rows = getDatabase().update(DataBaseHelper.TABLE_TAGS, values,
            COLUMN_TAG_ID + "=?", new String[]{String.valueOf(tag.getId())});
            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.e(TAG, e != null && e.getMessage() != null ? e.getMessage() : "");
        } finally {
            db.endTransaction();
        }
        return rows;
    }

    public TagModel cursorToTag(final Cursor cursor) {
        final TagModel data = new TagModel();
        data.setId(cursor.getLong(cursor.getColumnIndex(RecordDAO.COLUMN_RECORD_ID)));
        data.setFolderId(cursor.getLong(cursor.getColumnIndex(COLUMN_TAG_FOLDER_ID)));
        data.setRoadId(cursor.getLong(cursor.getColumnIndex(COLUMN_TAG_ROAD_ID)));
        data.setMeasurementId(cursor.getLong(cursor.getColumnIndex(COLUMN_TAG_MEASUREMENT_ID)));
        data.setTime(cursor.getLong(cursor.getColumnIndex(COLUMN_TAG_TIME)));
        data.setDate(cursor.getLong(cursor.getColumnIndex(COLUMN_TAG_TIME)));
        data.setSpeed(cursor.getFloat(cursor.getColumnIndex(COLUMN_TAG_SPEED)));
        data.setName(cursor.getString(cursor.getColumnIndex(COLUMN_TAG_NAME)));
        data.setDescription(cursor.getString(cursor.getColumnIndex(COLUMN_TAG_DESCRIPTION)));
        data.setLatitude(cursor.getDouble(cursor.getColumnIndex(COLUMN_TAG_LATITUDE)));
        data.setLongitude(cursor.getDouble(cursor.getColumnIndex(COLUMN_TAG_LONGITUDE)));
        data.setAltitude(cursor.getDouble(cursor.getColumnIndex(COLUMN_TAG_ALTITUDE)));
        data.setUploaded(cursor.getInt(cursor.getColumnIndex(COLUMN_TAG_UPLOADED)) == 1);
        data.setPending(cursor.getInt(cursor.getColumnIndex(COLUMN_TAG_PENDING)) == 1);
        data.setImages(
                new String[] {
                cursor.getString(cursor.getColumnIndex(COLUMN_TAG_FIRST_IMAGE)),
                cursor.getString(cursor.getColumnIndex(COLUMN_TAG_SECOND_IMAGE)),
                cursor.getString(cursor.getColumnIndex(COLUMN_TAG_THIRD_IMAGE))});
        data.setNotes(cursor.getString(cursor.getColumnIndex(COLUMN_TAG_NOTES)));
        data.setAudioFile(cursor.getString(cursor.getColumnIndex(COLUMN_TAG_AUDIO)));
        data.setSingle(cursor.getInt(cursor.getColumnIndex(COLUMN_TAG_SINGLE)) == 1);
        final int roadConditionId = cursor.getInt(cursor.getColumnIndex(COLUMN_TAG_ROAD_CONDITION));
        setRoadCondition(data, roadConditionId);
        data.setIri(cursor.getFloat(cursor.getColumnIndex(COLUMN_TAG_IRI)));
        return data;
    }

    private void setRoadCondition(final TagModel data, final int roadConditionId) {
        switch (roadConditionId) {
            case 0:
                data.setRoadCondition(TagModel.RoadCondition.GOOD);
                break;
            case 1:
                data.setRoadCondition(TagModel.RoadCondition.FAIR);
                break;
            case 2:
                data.setRoadCondition(TagModel.RoadCondition.POOR);
                break;
            case 3:
                data.setRoadCondition(TagModel.RoadCondition.BAD);
                break;
            default:
                data.setRoadCondition(TagModel.RoadCondition.NONE);
                break;
        }
    }

    public Cursor getAllTagsCursor() {
        return getDatabase().query(DataBaseHelper.TABLE_TAGS, allColumns, null, null, null, null, null);
    }

    public Cursor getTagsByRoadIdCursor(final long roadId) {
        final Cursor cursor = getDatabase().query(DataBaseHelper.TABLE_TAGS, allColumns,
                COLUMN_TAG_ROAD_ID + " = ?",
                new String[]{String.valueOf(roadId)}, null, null, null);
        cursor.moveToFirst();
        return cursor;
    }

    private Cursor getBumpsForUploadCursor(final String selection) {
        String sel = COLUMN_TAG_UPLOADED + "=0 and " + COLUMN_TAG_PENDING + "=0";
        if (selection != null) {
            sel += " and " + selection;
        }
        return getDatabase().query(DataBaseHelper.TABLE_TAGS, allColumns, sel, null, null, null, null);
    }

    public Cursor getTagsByMeasurementIdCursor(final long measurementId) {
        final Cursor cursor = getDatabase().query(DataBaseHelper.TABLE_TAGS, allColumns,
                COLUMN_TAG_MEASUREMENT_ID + " = ?",
                new String[]{String.valueOf(measurementId)}, null, null, null);
        cursor.moveToFirst();
        return cursor;
    }

    public Cursor getTagsByFolderIdCursor(final long folderId) {
        final Cursor cursor = getDatabase().query(DataBaseHelper.TABLE_TAGS, allColumns,
                COLUMN_TAG_FOLDER_ID + " = ?",
                new String[]{String.valueOf(folderId)}, null, null, null);
        cursor.moveToFirst();
        return cursor;
    }

    public Cursor getTagsWithoutMeasurement() {
        final Cursor cursor = getDatabase().query(DataBaseHelper.TABLE_TAGS, allColumns,
                COLUMN_TAG_SINGLE + " = ?",
                new String[]{String.valueOf(1)}, null, null, null);
        cursor.moveToFirst();
        return cursor;
    }

    public List<MeasurementItem> getTagsByRoadId(final long roadId) {
        final Cursor cursor = getTagsByRoadIdCursor(roadId);
        List<MeasurementItem> items = new ArrayList<MeasurementItem>();
        TagModel record = null;
        boolean moveToNext = cursor.getCount() > 0;
        while(moveToNext) {
            record = cursorToTag(cursor);
            items.add(record);
            moveToNext = cursor.moveToNext();
        }
        return items;
    }

    public List<TagModel> getTagsListByRoadId(final long roadId) {
        final Cursor cursor = getTagsByRoadIdCursor(roadId);
        List<TagModel> items = new ArrayList<TagModel>();
        TagModel record = null;
        boolean moveToNext = cursor.getCount() > 0;
        while(moveToNext) {
            record = cursorToTag(cursor);
            items.add(record);
            moveToNext = cursor.moveToNext();
        }
        return items;
    }

    public List<TagModel> getTagsByMeasurementId(final long id) {
        final Cursor cursor = getTagsByMeasurementIdCursor(id);
        List<TagModel> items = new ArrayList<TagModel>();
        TagModel record = null;
        boolean moveToNext = cursor.getCount() > 0;
        while(moveToNext) {
            record = cursorToTag(cursor);
            items.add(record);
            moveToNext = cursor.moveToNext();
        }
        return items;
    }

    @Override
    public List<? extends BaseModel> getDataForUpload(final String query) {
        final List<TagModel> listData = new ArrayList<>();
        final Cursor cursor = getBumpsForUploadCursor(query);
        if (cursor != null) {
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                final TagModel data = cursorToTag(cursor);
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
        List<TagModel> castedItems = (List<TagModel>) items;
        SQLiteDatabase db = getDatabase();
        try {
            db.beginTransaction();
            for (TagModel item : castedItems) {
                item.setUploaded(flag);
                db.update(DataBaseHelper.TABLE_TAGS, getContentValues(item), COLUMN_TAG_ID + "=?", new String[]{String.valueOf(item.getId())});
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
        List<TagModel> castedItems = (List<TagModel>) items;
        SQLiteDatabase db = getDatabase();
        try {
            db.beginTransaction();
            for (TagModel item : castedItems) {
                item.setPending(flag);
                db.update(DataBaseHelper.TABLE_TAGS, getContentValues(item), COLUMN_TAG_ID + "=?", new String[]{String.valueOf(item.getId())});
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
        List<TagModel> castedItems = (List<TagModel>) items;
        SQLiteDatabase db = getDatabase();
        try {
            db.beginTransaction();
            for (TagModel item : castedItems) {
                item.setUploaded(uploaded);
                item.setPending(pending);
                db.update(DataBaseHelper.TABLE_TAGS, getContentValues(item), COLUMN_TAG_ID + "=?",
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
