package com.softteco.roadlabpro.sqlite;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.softteco.roadlabpro.RAApplication;
import com.softteco.roadlabpro.sqlite.dao.BaseDAO;
import com.softteco.roadlabpro.sqlite.dao.BumpDAO;
import com.softteco.roadlabpro.sqlite.dao.FolderDAO;
import com.softteco.roadlabpro.sqlite.dao.GeoTagDAO;
import com.softteco.roadlabpro.sqlite.dao.IssuesDAO;
import com.softteco.roadlabpro.sqlite.dao.MeasurementDAO;
import com.softteco.roadlabpro.sqlite.dao.ProcessedDataDAO;
import com.softteco.roadlabpro.sqlite.dao.RecordDAO;
import com.softteco.roadlabpro.sqlite.dao.RecordDetailsDAO;
import com.softteco.roadlabpro.sqlite.dao.RoadDAO;
import com.softteco.roadlabpro.sqlite.dao.TagDAO;
import com.softteco.roadlabpro.sqlite.model.GeoTagModel;


/**
 * Created by Vadim Alenin on 4/6/2015.
 */
public class DataBaseHelper extends SQLiteOpenHelper {

    public static final String TAG = DataBaseHelper.class.getName();
    public static DataBaseHelper instance;

    public static final String TABLE_ISSUES = "issues";
    public static final String TABLE_MEASUREMENTS = "measurements";
    public static final String TABLE_RECORDS = "record";
    public static final String TABLE_RECORDS_DETAILS = "record_detail";
    public static final String TABLE_PROCESSED_DATA = "processed_data";
    public static final String TABLE_BUMPS = "bumps";
    public static final String TABLE_FOLDERS = "folders";
    public static final String TABLE_ROADS = "roads";
    public static final String TABLE_TAGS = "tags";
    public static final String TABLE_GEO_TAGS = "geo_tags";

    public static final String ALTER_TABLE = "ALTER TABLE ";
    public static final String ADD_COLUMN = " ADD COLUMN ";

    private static final String DATABASE_NAME = "road.db";
    private static final int DATABASE_VERSION = 42;

    public DataBaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public static DataBaseHelper getInstance() {
        if (instance == null) {
            instance = new DataBaseHelper(RAApplication.getInstance());
        }
        return instance;
    }

    @Override
    public synchronized void close() {
        super.close();
    }

    @Override
    public void onCreate(final SQLiteDatabase database) {
        new Thread (new Runnable() {
            @Override
            public void run() {
                createDbTables(database);
            }
        }).start();
    }

    @Override
    public void onUpgrade(final SQLiteDatabase db, final int oldVersion, final int newVersion) {
        new Thread (new Runnable() {
            @Override
            public void run() {
                if (oldVersion == 40 && newVersion == 42) {
                    updateDbFrom40To42Version(db);
                } else {
                    dropDbTables(db);
                    createDbTables(db);
                }
            }
        }).start();
    }

    private void updateDbFrom40To42Version(final SQLiteDatabase db) {
        db.execSQL(ALTER_TABLE + TABLE_GEO_TAGS + ADD_COLUMN + GeoTagDAO.COLUMN_GEO_TAG_DISTANCE + " " + BaseDAO.REAL_NOT_NULL + " DEFAULT (0)");
        db.execSQL(ALTER_TABLE + TABLE_FOLDERS + ADD_COLUMN + FolderDAO.COLUMN_FOLDER_PATH_DISTANCE + " " + BaseDAO.REAL_NOT_NULL + " DEFAULT (0)");
        db.execSQL(ALTER_TABLE + TABLE_ROADS + ADD_COLUMN + RoadDAO.COLUMN_ROAD_PATH_DISTANCE + " " + BaseDAO.REAL_NOT_NULL + " DEFAULT (0)");
        db.execSQL(ALTER_TABLE + TABLE_MEASUREMENTS + ADD_COLUMN + MeasurementDAO.COLUMN_MEASUREMENT_PATH_DISTANCE + " " + BaseDAO.REAL_NOT_NULL + " DEFAULT (0)");
    }

    private synchronized void createDbTables(final SQLiteDatabase database) {
        database.execSQL(RecordDAO.SQL_CREATE_TABLE_RECORD);
        database.execSQL(RecordDetailsDAO.SQL_CREATE_TABLE_RECORD_DETAILS);
        database.execSQL(ProcessedDataDAO.SQL_CREATE_TABLE_PROCESSED_DATA);
        database.execSQL(BumpDAO.SQL_CREATE_TABLE_BUMPS);
        database.execSQL(IssuesDAO.SQL_CREATE_TABLE_ISSUE);
        database.execSQL(FolderDAO.SQL_CREATE_TABLE_FOLDERS);
        database.execSQL(RoadDAO.SQL_CREATE_TABLE_ROADS);
        database.execSQL(MeasurementDAO.SQL_CREATE_TABLE_MEASUREMENTS);
        database.execSQL(TagDAO.SQL_CREATE_TABLE_TAGS);
        database.execSQL(GeoTagDAO.SQL_CREATE_TABLE_GEO_TAGS);
    }

    private synchronized void dropDbTables(final SQLiteDatabase db) {
        db.execSQL(RecordDetailsDAO.SQL_DROP_TABLE_RECORD_DETAILS);
        db.execSQL(RecordDAO.SQL_DROP_TABLE_RECORD);
        db.execSQL(ProcessedDataDAO.SQL_DROP_TABLE_PROCESSED_DATA);
        db.execSQL(BumpDAO.SQL_DROP_TABLE_BUMPS);
        db.execSQL(IssuesDAO.SQL_DROP_TABLE_ISSUE);
        db.execSQL(FolderDAO.SQL_DROP_TABLE_FOLDERS);
        db.execSQL(RoadDAO.SQL_DROP_TABLE_ROADS);
        db.execSQL(MeasurementDAO.SQL_DROP_TABLE_MEASUREMENTS);
        db.execSQL(TagDAO.SQL_DROP_TABLE_TAGS);
        db.execSQL(GeoTagDAO.SQL_DROP_TABLE_GEO_TAGS);
    }

    public DataBaseHelper(Context context, String name, SQLiteDatabase.CursorFactory factory,
                          int version) {
        super(context, DATABASE_NAME, factory, DATABASE_VERSION);
    }
}