package com.softteco.roadlabpro.tasks;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.os.AsyncTask;
import android.util.Log;

import com.opencsv.CSVWriter;
import com.softteco.roadlabpro.RAApplication;
import com.softteco.roadlabpro.algorithm.RoadQuality;
import com.softteco.roadlabpro.sqlite.MeasurementsDataHelper;
import com.softteco.roadlabpro.sqlite.dao.BumpDAO;
import com.softteco.roadlabpro.sqlite.dao.FolderDAO;
import com.softteco.roadlabpro.sqlite.dao.GeoTagDAO;
import com.softteco.roadlabpro.sqlite.dao.MeasurementDAO;
import com.softteco.roadlabpro.sqlite.dao.ProcessedDataDAO;
import com.softteco.roadlabpro.sqlite.dao.RoadDAO;
import com.softteco.roadlabpro.sqlite.dao.TagDAO;
import com.softteco.roadlabpro.sqlite.model.FolderModel;
import com.softteco.roadlabpro.sqlite.model.GeoTagModel;
import com.softteco.roadlabpro.sqlite.model.MeasurementModel;
import com.softteco.roadlabpro.sqlite.model.ProcessedDataModel;
import com.softteco.roadlabpro.sqlite.model.RoadModel;
import com.softteco.roadlabpro.sqlite.model.TagModel;
import com.softteco.roadlabpro.util.Constants;
import com.softteco.roadlabpro.util.DeviceUtil;
import com.softteco.roadlabpro.util.ExportToCSVResult;
import com.softteco.roadlabpro.util.FileUtils;
import com.softteco.roadlabpro.util.KMLHelper;
import com.softteco.roadlabpro.util.PreferencesUtil;
import com.softteco.roadlabpro.util.TimeUtil;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Created by ppp on 17.06.2015.
 */
public class ExportMeasurementDB extends AsyncTask<String, Void, String[]> {

    public static final char SEPARATOR = ',';

    private static final String TAG = "ExportIntoCSV";

    private volatile boolean stopProcess = false;

    protected FolderDAO folderDAO;
    protected RoadDAO roadDAO;
    protected MeasurementDAO measurementDAO;
    protected KMLHelper kmlHelper;

    private String[] roadIntervalsNames = {
            "time",
            "speed",
            "category",
            //"stddev",
            "start_lat",
            "start_lon",
            "end_lat",
            "end_lon",
            "is_fixed",
            "iri",
            "distance",
            "suspension"
    };

    private String[] bumpsNames = {
            "time",
            "speed",
            "lat",
            "lon",
            "Ax",
            "Ay",
            "Az",
            "is_fixed",
    };

    private String[] geoTagNames = {
            "time",
            "latitude",
            "longitude",
    };

    private String[] tagsNames = {
            "tag_id",
            "folder_id",
            "road_id",
            "measurement_id",
            "time",
            "speed",
            "name",
            "description",
            "latitude",
            "longitude",
            "altitude",
            "uploaded",
            "notes",
            "road_condition",
            "iri",
            "media_folder"
    };

    private String[] projectSummaryNames = {
            "date",
            "name",
            "links",
            "distance",
            "avg_speed",
            "iri"
    };

    private ExportToCSVResult listener;
    protected Context context;

    public ExportMeasurementDB(Context context, final ExportToCSVResult listener) {
        this.context = context;
        this.listener = listener;
        folderDAO = new FolderDAO(context);
        roadDAO = new RoadDAO(context);
        measurementDAO = new MeasurementDAO(context);
        kmlHelper = new KMLHelper();
    }

    public void stopProcess() {
        stopProcess = true;
        cancel(true);
    }

    @Override
    protected String[] doInBackground(final String... args) {
        List<FolderModel> folderModels = folderDAO.getAllFolders();
        for (FolderModel folder : folderModels) {
            exportFolderData(folder);
        }
        return null;
    }

    protected void exportFolderData(FolderModel folder) {
        String folderName = folder.getName();
        String dataDir = FileUtils.getDataDir(folderName, false);
        FileUtils.deleteFile(dataDir, true);
        exportProjectSummaryData(folder);
        List<RoadModel> roadModels = roadDAO.getAllRoadsByFolderId(folder.getId());
        for (RoadModel road : roadModels) {
            exportRoadData(folderName, road);
        }
    }

    protected void exportRoadData(String folderName, RoadModel road) {
        String roadName = road.getName();
        String dataDir = FileUtils.getDataDir(folderName + "/" + roadName, false);
        FileUtils.deleteFile(dataDir, true);
        List<MeasurementModel> measurementModels = measurementDAO.getAllMeasurementsByRoadId(road.getId());
        for (MeasurementModel measurement : measurementModels) {
            exportMeasurementData(folderName, roadName, measurement);
            kmlHelper.exportMeasurementData(folderName, roadName, measurement);
        }
        long roadId = road.getId();
        String tagsDestDataDir = FileUtils.getDataDir(folderName + "/" + roadName + "/tags/", true);
        Date tagsDate = new Date(road.getDate());
        writeTagsFile(folderName, roadName, tagsDate, tagsDestDataDir, roadId);
        kmlHelper.writeTagsFile(tagsDate, tagsDestDataDir, roadId);
    }

    private String exportProjectSummaryData(FolderModel folder) {
        String projectName = folder.getName();
        String dataDir = FileUtils.getDataDir(projectName, true);
        Date projectDate = new Date(folder.getDate());
        long folderId = folder.getId();
        File file = new File(dataDir + "/" + String.format(Constants.PROJECT_SUMMARY + "_%s_%s%s",
        DeviceUtil.getDeviceName(), new SimpleDateFormat(TimeUtil.DATE_FILENAME_FORMAT, Locale.US).format(projectDate), Constants.CSV_EXT));
        try {
            CSVWriter csvWrite = new CSVWriter(new FileWriter(file), SEPARATOR);
            Cursor curCSV = roadDAO.getAllRoadByFolderIdCursor(folderId);
            RoadModel roadModel = null;
            csvWrite.writeNext(projectSummaryNames);
            SimpleDateFormat timestampDate = new SimpleDateFormat(TimeUtil.DATE_TIMESTAMP, Locale.US);
            do {
                if (stopProcess) {
                    break;
                }
                if (curCSV.getCount() == 0) {
                    Log.d(TAG, "TAGS - curCSV.getCount() == 0");
                    continue;
                }
                roadModel = roadDAO.cursorToRoad(curCSV);
                MeasurementsDataHelper.getInstance().refreshRoadCountersSync(roadModel);
                long date = roadModel.getDate();
                Date roadDate = new Date(date);
                String dateStr = timestampDate.format(roadDate);
                String name = roadModel.getName();
                long links = roadModel.getExperiments();
                double distance = roadModel.getOverallDistance();
                double iri = roadModel.getAverageIRI();
                double avgSpeed = roadModel.getAverageSpeed();
                String[] arrStr = {
                    dateStr,
                    name,
                    String.valueOf(links),
                    String.format(Locale.US, "%3.2f", distance),
                    String.format(Locale.US, "%3.2f", avgSpeed),
                    String.format(Locale.US, "%3.2f", iri)
                };
                csvWrite.writeNext(arrStr);
            } while (curCSV.moveToNext());
            csvWrite.close();
            curCSV.close();
            return file.getAbsolutePath();
        } catch (Exception e) {
            Log.e(TAG, "exportProjectSummaryData", e);
            return null;
        }
    }

    protected void exportMeasurementData(String folderName, String roadName, MeasurementModel measurement) {
        String measurementName;
        long measurementId;
        String dataDir;
        String projectPath;
        measurementName = FileUtils.getExportItemName(context, measurement.getId(), measurement.getDate());
        measurementId = measurement.getId();
        projectPath = folderName + "/" + roadName + "/" + measurementName + "/";
        dataDir = FileUtils.getDataDir(projectPath, true);
        Date measurementDate = new Date(measurement.getDate());
        writeRoadIntervalsFile(measurementDate, dataDir, measurementId);
        writeBumpsFile(measurementDate, dataDir, measurementId);
        writeGeoTagsFile(measurementDate, dataDir, measurementId);
    }

    public String writeTagsFile(String folderName, String roadName, Date date, String destinationDataDir, long roadId) {
        File file = new File(destinationDataDir + String.format(Constants.TAGS + "_%s_%s%s",
                DeviceUtil.getDeviceName(), new SimpleDateFormat(TimeUtil.DATE_FILENAME_FORMAT, Locale.US).format(date), Constants.CSV_EXT));
        TagDAO dao = new TagDAO(RAApplication.getInstance());
        try {
            CSVWriter csvWrite = new CSVWriter(new FileWriter(file), SEPARATOR);
            Cursor curCSV = dao.getTagsByRoadIdCursor(roadId);
//          csvWrite.writeNext(new String[]{"sep=" + SEPARATOR});
            TagModel tagModel = null;
            csvWrite.writeNext(tagsNames);
            SimpleDateFormat timestampDate = new SimpleDateFormat(TimeUtil.DATE_TIMESTAMP, Locale.US);
            do {
                if (stopProcess) {
                    break;
                } 
                if (curCSV.getCount() == 0) {
                    Log.d(TAG, "TAGS - curCSV.getCount() == 0");
                    continue;
                }
                tagModel = dao.cursorToTag(curCSV);
                long tagId = tagModel.getId();
                long folderId = tagModel.getFolderId();
                long road_id = tagModel.getRoadId();
                long measurementId = tagModel.getMeasurementId();
                long time = tagModel.getDate();
                double speed = tagModel.getSpeed();
                String name = tagModel.getName();
                String description = tagModel.getDescription();
                double latitude = tagModel.getLatitude();
                double longitude = tagModel.getLongitude();
                boolean isUploaded = tagModel.isUploaded();
                String firstImage = "";
                String secondImage = "";
                String thirdImage = "";
                int length = 0;
                if (tagModel.getImages() != null) {
                    length = tagModel.getImages().length;
                    if (length > 0) {
                        firstImage = tagModel.getImages()[0];
                    }
                    if (length > 1) {
                        secondImage = tagModel.getImages()[1];
                    }
                    if (length > 2) {
                        thirdImage = tagModel.getImages()[2];
                    }
                }
                String audioFile = tagModel.getAudioFile();
                String notes = tagModel.getNotes();
                TagModel.RoadCondition condition = tagModel.getRoadCondition();
                String roadConditionStr = "";
                if (condition != null) {
                    roadConditionStr = condition.name();
                }
                float iri = tagModel.getIri();
                Date tagDate = new Date(time);
                String dateStr = timestampDate.format(tagDate);
                //String exportName = FileUtils.getExportItemName(context, tagId, time);
                String exportName = FileUtils.getExportItemNameTag(tagModel, folderName, roadName);
                String[] arrStr = {
                        String.valueOf(tagId),
                        String.valueOf(folderId),
                        String.valueOf(road_id),
                        String.valueOf(measurementId),
                        dateStr,
                        String.format(Locale.US, "%3.2f", speed),
                        name,
                        description,
                        String.format(Locale.US, "%3.8f", latitude),
                        String.format(Locale.US, "%3.8f", longitude),
                        String.valueOf(isUploaded),
                        notes,
                        roadConditionStr,
                        String.valueOf(iri),
                        exportName
                };
                csvWrite.writeNext(arrStr);
                String mediaDataDir = FileUtils.checkDir(destinationDataDir + exportName + "/", true);
                FileUtils.copyFile(FileUtils.getAudioDir(true), audioFile, mediaDataDir, exportName + Constants.MP3_EXT);
                String imageFilesDir = FileUtils.getImagesDir(true);
                if (!firstImage.isEmpty())
                    FileUtils.copyFile(imageFilesDir, firstImage, mediaDataDir, exportName + "_1" + Constants.JPG_EXT);
                if (!secondImage.isEmpty())
                    FileUtils.copyFile(imageFilesDir, secondImage, mediaDataDir, exportName + "_2" + Constants.JPG_EXT);
                if (!thirdImage.isEmpty())
                    FileUtils.copyFile(imageFilesDir, thirdImage, mediaDataDir, exportName + "_3" + Constants.JPG_EXT);
            } while (curCSV.moveToNext());
            csvWrite.close();
            curCSV.close();
            return file.getAbsolutePath();
        } catch (Exception e) {
            Log.e(TAG, "writeTagsFile", e);
            return null;
        }
    }

    public String writeRoadIntervalsFile(Date date, String dataDir, long measurementId) {
        stopProcess = false;
        File file = new File(dataDir + String.format(Constants.ROAD_INTERVALS + "_%s_%s%s",
        DeviceUtil.getDeviceName(), new SimpleDateFormat(TimeUtil.DATE_FILENAME_FORMAT, Locale.US).format(date), Constants.CSV_EXT));
        ProcessedDataDAO dao = new ProcessedDataDAO(RAApplication.getInstance());
        try {
            CSVWriter csvWrite = new CSVWriter(new FileWriter(file), SEPARATOR);
            Cursor curCSV = dao.getProcessedDataByMeasurementIdCursor(measurementId);
//            csvWrite.writeNext(new String[]{"sep=" + SEPARATOR});
            csvWrite.writeNext(roadIntervalsNames);
            SimpleDateFormat timestampDate = new SimpleDateFormat(TimeUtil.DATE_TIMESTAMP, Locale.US);
            ProcessedDataModel data = null;
            do {
                if (stopProcess) {
                    break;
                }
                if (curCSV.getCount() == 0) {
                    Log.d(TAG, "curCSV.getCount() == 0");
                    continue;
                }
                data = dao.cursorToRecord(curCSV);
                long time = data.getTime();
                String dateStr = timestampDate.format(new Date(time));
                double speed = data.getSpeed();
                RoadQuality quality = data.getCategory();
                String categoryStr = "";
                if (quality != null) {
                    categoryStr = quality.name();
                }
                //double stddev = data.getStdDeviation();
                double start_latitude = 0;
                double start_longitude = 0;
                if (data.getCoordsStart() != null && data.getCoordsStart().length >= 2) {
                    start_latitude = data.getCoordsStart()[0];
                    start_longitude = data.getCoordsStart()[1];
                }
                double end_latitude = 0;
                double end_longitude = 0;
                if (data.getCoordsEnd() != null && data.getCoordsEnd().length >= 2) {
                    end_latitude = data.getCoordsEnd()[0];
                    end_longitude = data.getCoordsEnd()[1];
                }
                boolean isFixed = data.isFixed();
                double iri = data.getIri();
                double distance = data.getDistance();
                int suspension = data.getSuspension();
                PreferencesUtil.SUSPENSION_TYPES suspensionType = null;
                String suspensionStr = "";
                if (suspension < PreferencesUtil.SUSPENSION_TYPES.values().length) {
                    suspensionType = PreferencesUtil.SUSPENSION_TYPES.values()[suspension];
                    suspensionStr = suspensionType.name();
                }
                String[] arrStr = {
                        dateStr,
                        String.format(Locale.US, "%3.2f", speed),
                        categoryStr,
                        //String.format(Locale.US, "%3.8f", stddev),
                        String.format(Locale.US, "%3.8f", start_latitude),
                        String.format(Locale.US, "%3.8f", start_longitude),
                        String.format(Locale.US, "%3.8f", end_latitude),
                        String.format(Locale.US, "%3.8f", end_longitude),
                        String.valueOf(isFixed),
                        String.format(Locale.US, "%3.2f", iri),
                        String.format(Locale.US, "%3.2f", distance),
                        String.format(Locale.US, "%s", suspensionStr)
                };
                csvWrite.writeNext(arrStr);
            } while (curCSV.moveToNext());
            csvWrite.close();
            curCSV.close();
            return file.getAbsolutePath();
        } catch (Exception e) {
            Log.e(TAG, "writeRoadIntervalsFile", e);
            return null;
        }
    }

    public String writeGeoTagsFile(Date date, String dataDir, long measurementId) {
        File file = new File(dataDir + String.format(Constants.GEO_TAGS + "_%s_%s%s",
        DeviceUtil.getDeviceName(), new SimpleDateFormat(TimeUtil.DATE_FILENAME_FORMAT, Locale.US).format(date) + Constants.GEO_TAG_SUFFIX, Constants.CSV_EXT));
        GeoTagDAO dao = new GeoTagDAO(RAApplication.getInstance());
        try {
            CSVWriter csvWrite = new CSVWriter(new FileWriter(file), SEPARATOR);
            Cursor curCSV = dao.getGeoTagsByMeasurementIdCursor(measurementId);
            csvWrite.writeNext(geoTagNames);
            SimpleDateFormat timestampDate = new SimpleDateFormat(TimeUtil.DATE_TIMESTAMP, Locale.US);
            do {
                if (stopProcess) {
                    break;
                }
                if (curCSV.getCount() == 0) {
                    Log.d(TAG, "curCSV.getCount() == 0");
                    continue;
                }
                GeoTagModel geoTag = dao.cursorToRecord(curCSV);
                long time = geoTag.getTime();
                String dateStr = timestampDate.format(new Date(time));
                double latitude = geoTag.getLatitude();
                double longitude = geoTag.getLongitude();
                String[] arrStr = {
                    dateStr,
                    String.format(Locale.US, "%3.8f", latitude),
                    String.format(Locale.US, "%3.8f", longitude),
                };
                csvWrite.writeNext(arrStr);
            } while (curCSV.moveToNext());
            csvWrite.close();
            curCSV.close();
            return file.getAbsolutePath();
        } catch (Exception e) {
            Log.e(TAG, "writeGeoTagsFile", e);
            return null;
        }
    }

    public String writeBumpsFile(Date date, String dataDir, long measurementId) {
        File file = new File(dataDir + String.format(Constants.BUMPS + "_%s_%s%s",
                DeviceUtil.getDeviceName(), new SimpleDateFormat(TimeUtil.DATE_FILENAME_FORMAT, Locale.US).format(date), Constants.CSV_EXT));
        BumpDAO dao = new BumpDAO(RAApplication.getInstance());
        try {
            CSVWriter csvWrite = new CSVWriter(new FileWriter(file), SEPARATOR);
            Cursor curCSV = dao.getBumpByMeasurementIdCursor(measurementId);
//          csvWrite.writeNext(new String[]{"sep=" + SEPARATOR});
            csvWrite.writeNext(bumpsNames);
            SimpleDateFormat timestampDate = new SimpleDateFormat(TimeUtil.DATE_TIMESTAMP, Locale.US);
            do {
                if (stopProcess) {
                    break;
                }
                if (curCSV.getCount() == 0) {
                    Log.d(TAG, "curCSV.getCount() == 0");
                    continue;
                }
                long time = curCSV.getLong(4);
                String dateStr = timestampDate.format(new Date(time));
                double speed = curCSV.getDouble(5);
                double latitude = curCSV.getDouble(6);
                double longitude = curCSV.getDouble(7);
                double accelerationX = curCSV.getDouble(9);
                double accelerationY = curCSV.getDouble(10);
                double accelerationZ = curCSV.getDouble(11);
                boolean isFixed = curCSV.getInt(15) == 1;
                String[] arrStr = {
                        dateStr,
                        String.format(Locale.US, "%3.2f", speed),
                        String.format(Locale.US, "%3.8f", latitude),
                        String.format(Locale.US, "%3.8f", longitude),
                        String.format(Locale.US, "%3.8f", accelerationX),
                        String.format(Locale.US, "%3.8f", accelerationY),
                        String.format(Locale.US, "%3.8f", accelerationZ),
                        String.valueOf(isFixed)
                };
                csvWrite.writeNext(arrStr);
            } while (curCSV.moveToNext());
            csvWrite.close();
            curCSV.close();
            return file.getAbsolutePath();
        } catch (Exception e) {
            Log.e(TAG, "writeBumpsFile", e);
            return null;
        }
    }

    protected void onPostExecute(final String[] success) {
        if (listener != null) {
            listener.onResultsAfterExporting(success);
        }
    }
}

