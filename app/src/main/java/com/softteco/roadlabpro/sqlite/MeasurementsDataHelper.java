package com.softteco.roadlabpro.sqlite;

import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.util.Log;

import com.softteco.roadlabpro.R;
import com.softteco.roadlabpro.RAApplication;
import com.softteco.roadlabpro.algorithm.RoadConditionDetection;
import com.softteco.roadlabpro.algorithm.RoadQuality;
import com.softteco.roadlabpro.sqlite.dao.BumpDAO;
import com.softteco.roadlabpro.sqlite.dao.FolderDAO;
import com.softteco.roadlabpro.sqlite.dao.GeoTagDAO;
import com.softteco.roadlabpro.sqlite.dao.MeasurementDAO;
import com.softteco.roadlabpro.sqlite.dao.ProcessedDataDAO;
import com.softteco.roadlabpro.sqlite.dao.RoadDAO;
import com.softteco.roadlabpro.sqlite.dao.TagDAO;
import com.softteco.roadlabpro.sqlite.model.BumpModel;
import com.softteco.roadlabpro.sqlite.model.FolderModel;
import com.softteco.roadlabpro.sqlite.model.GeoTagModel;
import com.softteco.roadlabpro.sqlite.model.MeasurementItem;
import com.softteco.roadlabpro.sqlite.model.MeasurementModel;
import com.softteco.roadlabpro.sqlite.model.ProcessedDataModel;
import com.softteco.roadlabpro.sqlite.model.RoadModel;
import com.softteco.roadlabpro.sqlite.model.TagModel;
import com.softteco.roadlabpro.util.ActivityUtil;
import com.softteco.roadlabpro.util.DistanceUtil;
import com.softteco.roadlabpro.util.ExecutorServiceManager;
import com.softteco.roadlabpro.util.PreferencesUtil;

import java.util.ArrayList;
import java.util.List;

public class MeasurementsDataHelper {

    public static final String TAG = MeasurementsDataHelper.class.getName();

    private FolderDAO folderDao;
    private RoadDAO roadDao;
    private GeoTagDAO geoTagDao;
    private MeasurementDAO measurementDao;
    private ProcessedDataDAO intervalsDao;
    private BumpDAO bumpsDao;
    private TagDAO tagsDao;
    private ExecutorServiceManager executor;

    public static MeasurementsDataHelper getInstance() {
        return RAApplication.getInstance().getMeasurementsDataHelper();
    }

    public MeasurementsDataHelper() {
        Context context = RAApplication.getInstance().getBaseContext();
        executor = new ExecutorServiceManager();
        folderDao = new FolderDAO(context);
        roadDao = new RoadDAO(context);
        measurementDao = new MeasurementDAO(context);
        intervalsDao = new ProcessedDataDAO(context);
        bumpsDao = new BumpDAO(context);
        tagsDao = new TagDAO(context);
        geoTagDao = new GeoTagDAO(context);
    }

    public ProcessedDataDAO getIntervalsDao() {
        return intervalsDao;
    }

    public BumpDAO getBumpsDao() {
        return bumpsDao;
    }

    public TagDAO getTagsDao() {
        return tagsDao;
    }

    public GeoTagDAO getGeoTagDao() {
        return geoTagDao;
    }

    public interface MeasurementsDataLoaderListener<T> {
        void onDataLoaded(T data);
    }

    public Context getContext() {
        return RAApplication.getInstance();
    }

    public FolderModel getProject(long projectId) {
        return folderDao.getFolder(projectId);
    }

    public RoadModel getRoad(long folderId, long roadId) {
        return roadDao.getRoad(folderId, roadId);
    }

    public FolderModel getDefaultProject() {
        return folderDao.getDefaultFolder();
    }

    public RoadModel getDefaultRoad() {
        return roadDao.getDefaultRoad();
    }

    public void getData(final FolderModel folder, final MeasurementsDataLoaderListener listener) {
        executor.runOperation(new Runnable() {
            @Override
            public void run() {
                final List<MeasurementItem> items = getData(folder);
                ActivityUtil.runOnMainThread(new Runnable() {
                    @Override
                    public void run() {
                        if (listener != null) {
                            listener.onDataLoaded(items);
                        }
                    }
                });
            }
        });
    }

    public void getData(final RoadModel road, final MeasurementsDataLoaderListener listener) {
        executor.runOperation(new Runnable() {
            @Override
            public void run() {
                final List<MeasurementItem> items = getData(road);
                ActivityUtil.runOnMainThread(new Runnable() {
                    @Override
                    public void run() {
                        if (listener != null) {
                            listener.onDataLoaded(items);
                        }
                    }
                });
            }
        });
    }

    public void getData(final MeasurementModel measurement, final MeasurementsDataLoaderListener listener) {
       getData(measurement, false, listener);
    }

    public void getData(final MeasurementModel measurement, final boolean filter, final MeasurementsDataLoaderListener listener) {
        executor.runOperation(new Runnable() {
            @Override
            public void run() {
                final List<MeasurementItem> items = getData(measurement, filter);
                ActivityUtil.runOnMainThread(new Runnable() {
                    @Override
                    public void run() {
                        if (listener != null) {
                            listener.onDataLoaded(items);
                        }
                    }
                });
            }
        });
    }

    public List<MeasurementItem> getData(FolderModel folder) {
        List<MeasurementItem> measurementsData = new ArrayList<MeasurementItem>();
        if (folder == null) {
            return measurementsData;
        }
        List<RoadModel> roads = roadDao.getAllRoadsByFolderId(folder.getId());
        List<MeasurementItem> items;
        for (RoadModel r : roads) {
            items = getData(r);
            measurementsData.addAll(items);
        }
        return measurementsData;
    }

    public List<MeasurementItem> getData(RoadModel road) {
        List<MeasurementItem> measurementsData = new ArrayList<MeasurementItem>();
        if (road == null) {
            return measurementsData;
        }
        List<MeasurementModel> measurements = measurementDao.getAllMeasurementsByRoadId(road.getId());
        List<MeasurementItem> items = null;
        for (MeasurementModel m : measurements) {
            items = getData(m, false);
            measurementsData.addAll(items);
        }
        return measurementsData;
    }

    public List<MeasurementItem> getData(MeasurementModel measurement, boolean filter) {
        long measurementId = -1;
        if (measurement != null) {
            measurementId = measurement.getId();
        }
        return getMeasurementData(measurementId, filter);
    }

    public ProcessedDataModel getLastInterval(long measurementId) {
        ProcessedDataModel processedData = intervalsDao.getLastInterval(measurementId);
        return processedData;
    }

    public RoadModel getLastRoad(long folderId) {
        RoadModel road = roadDao.getLastRoad(folderId);
        return road;
    }

    public TagModel getLastTag() {
        TagModel tag = tagsDao.getLastTag();
        return tag;
    }

    public void getProjectAsync(final long projectId, final MeasurementsDataLoaderListener<FolderModel> listener) {
        executor.runOperation(new Runnable() {
            @Override
            public void run() {
                final FolderModel project = getProject(projectId);
                ActivityUtil.runOnMainThread(new Runnable() {
                    @Override
                    public void run() {
                        if (listener != null) {
                            listener.onDataLoaded(project);
                        }
                    }
                });
            }
        });
    }

    public List<MeasurementItem> getMeasurementData(long measurementId, boolean filter) {
        List<MeasurementItem> measurementsData = new ArrayList<MeasurementItem>();
        if(measurementId < 0) {
            return measurementsData;
        }
        boolean addIntervals = true;
        boolean addBumps = true;
        if (filter) {
            addIntervals = PreferencesUtil.getInstance().getShowIntervalsFilter();
            addBumps = PreferencesUtil.getInstance().getShowBumpsFilter();
        }
        if (addIntervals) {
            List<ProcessedDataModel> processedData = intervalsDao.getProcessedDataByMeasurementId(measurementId);
            measurementsData.addAll(processedData);
        }
        if (addBumps) {
            List<BumpModel> bumpsData = bumpsDao.getBumpsByMeasurementId(measurementId);
            measurementsData.addAll(bumpsData);
        }
        return measurementsData;
    }

    public List<MeasurementItem> getTagsSync(long roadId) {
        return tagsDao.getTagsByRoadId(roadId);
    }

    public void getTags(final long roadId, final MeasurementsDataLoaderListener<List<MeasurementItem>> listener) {
        executor.runOperation(new Runnable() {
            @Override
            public void run() {
                final List<MeasurementItem> items = getTagsSync(roadId);

                ActivityUtil.runOnMainThread(new Runnable() {
                    @Override
                    public void run() {
                        if (listener != null) {
                            listener.onDataLoaded(items);
                        }
                    }
                });
            }
        });
    }

    public void checkCurrentMeasurement(final MeasurementsDataLoaderListener<Boolean> listener) {
        executor.runOperation(new Runnable() {
            @Override
            public void run() {
                long measurementId = RAApplication.getInstance().getCurrentMeasurementId();
                long count = MeasurementsDataHelper.getInstance().getItemsCountSync(-1, -1, measurementId);
                final boolean emptyMeasurement = count == 0;
                if (emptyMeasurement) {
                    MeasurementsDataHelper.getInstance().deleteMeasurement(measurementId, true);
                }
                ActivityUtil.runOnMainThread(new Runnable() {
                    @Override
                    public void run() {
                        if (listener != null) {
                            listener.onDataLoaded(emptyMeasurement);
                        }
                    }
                });
            }
        });
    }

    public double getAverageSpeedSync(long folderId, long roadId, long measurementId) {
        return intervalsDao.getAverageSpeed(folderId, roadId, measurementId);
    }

    public double getAverageIRISync(long folderId, long roadId, long measurementId) {
        return intervalsDao.getAverageIRI(folderId, roadId, measurementId);
    }

    public double getOverallDistanceSync(long folderId, long roadId, long measurementId) {
        return intervalsDao.getOverallDistance(folderId, roadId, measurementId);
    }

    public double getPathDistanceSync(long folderId, long roadId, long measurementId) {
        return geoTagDao.getOverallDistance(folderId, roadId, measurementId);
    }

    public long getItemsCountSync(long folderId, long roadId, long measurementId) {
        return getItemsCountSync(folderId, roadId, measurementId, true);
    }

    public long getItemsCountSync(long folderId, long roadId, long measurementId, boolean countBumps) {
        long count = 0;
        long intervals = intervalsDao.getAllItemsCount(folderId, roadId, measurementId);
        count += intervals;
        if (countBumps) {
            long bumps = bumpsDao.getAllItemsCount(folderId, roadId, measurementId);
            count += bumps;
        }
        return count;
    }

    public long getBumpsCount(long measurementId) {
        long bumps = bumpsDao.getAllItemsCount(-1, -1, measurementId);
        return bumps;
    }

    public long getRoadsCount(long folderId) {
        return roadDao.getAllItemsCountByFolderId(folderId);
    }

    public long getMeasurementsCount(long roadId) {
        return measurementDao.getAllItemsCountByRoadId(roadId);
    }

    public String getIRIStr(double iri) {
        return getIRIStr(iri, true);
    }

    public String getIRIStr(double iri, boolean addIriPrefix) {
        if (iri <= 0) {
            iri = RoadConditionDetection.IRI_DEFAULT_VALUE;
        }
        String iriValueStr = String.format("%2.2f", Math.abs(iri));
        String iriStr = "";
        if (addIriPrefix) {
            iriStr = getContext().getString(R.string.measurements_item_list_iri, iriValueStr);
        } else {
            iriStr = iriValueStr;
        }
        return iriStr;
    }

    public static String getDistanceStrHtml(Context context, double distance, double realDistance) {
        String distanceStr = DistanceUtil.getDistanceString(context, distance);
        String pathDistanceStr = distance == 0 ?
                context.getString(R.string.overall_path_distance_none) :
                DistanceUtil.getDistanceString(context, realDistance);
        String allDistanceStr = context.getString(R.string.overall_distance_text_color, distanceStr, pathDistanceStr);
        return allDistanceStr;
    }

//    public String getItemTitleStr(MeasurementItem item) {
//        long id = item.getId();
//        long dateMillis = item.getTime();
//        Date date = new Date(dateMillis);
//        String dateStr = DateUtil.format(date, DateUtil.Format.DDMMMMYYYYHHMM);
//        int formatIdRes = R.string.fr_settings_db_template_for_export;
//        if (item.getType() != null && MeasurementItemType.TAG.equals(item.getType())) {
//            formatIdRes = R.string.fr_settings_db_template_for_export_tag;
//        }
//        return getContext().getString(formatIdRes, String.format(Locale.US, "%03d", id), dateStr);
//    }

    public long getTagsNextId() {
        long id = 0;
        TagModel tag = getLastTag();
        if (tag != null) {
            id = tag.getId();
        }
        return id + 1;
    }

    public void refreshFoldersCountersSync() {
        try {
            final Cursor cursor = folderDao.getAllFolderCursor();
            if (cursor != null) {
                cursor.moveToFirst();
                while (!cursor.isAfterLast()) {
                    final FolderModel folderModel = folderDao.cursorToFolder(cursor);
                    refreshFolderCountersSync(folderModel);
                    cursor.moveToNext();
                }
                cursor.close();
            }
        } catch (Exception e) {
            Log.e(TAG, e != null && e.getMessage() != null ? e.getMessage() : "");
        }
    }

    public void refreshRoadsCountersSync() {
        try {
            final Cursor cursor = roadDao.getAllRoadCursor();
            if (cursor != null) {
                cursor.moveToFirst();
                while (!cursor.isAfterLast()) {
                    final RoadModel roadModel = roadDao.cursorToRoad(cursor);
                    refreshRoadCountersSync(roadModel);
                    cursor.moveToNext();
                }
                cursor.close();
            }
        } catch (Exception e) {
            Log.e(TAG, e != null && e.getMessage() != null ? e.getMessage() : "");
        }
    }

    public void refreshMeasurementsCountersSync() {
        try {
            final Cursor cursor = measurementDao.getAllMeasurementCursor();
            if (cursor != null) {
                cursor.moveToFirst();
                while (!cursor.isAfterLast()) {
                    final MeasurementModel measurementModel = measurementDao.cursorToMeasurement(cursor);
                    refreshMeasurementCountersSync(measurementModel);
                    cursor.moveToNext();
                }
                cursor.close();
            }
        } catch (Exception e) {
            Log.e(TAG, e != null && e.getMessage() != null ? e.getMessage() : "");
        }
    }

    public void refreshFolderCountersSync(FolderModel folder) {
       long folderId = folder.getId();
       long roadsCount = getRoadsCount(folderId);
       double distance = getOverallDistanceSync(folderId, -1, -1);
       double pathDistance = getPathDistanceSync(folderId, -1, -1);
       double averageIRI = getAverageIRISync(folderId, -1, -1);
       double averageSpeed = getAverageSpeedSync(folderId, -1, -1);
       folder.setRoads(roadsCount);
       folder.setOverallDistance(distance);
       folder.setPathDistance(pathDistance);
       folder.setAverageIRI(averageIRI);
       folder.setAverageSpeed(averageSpeed);
       updateProjectSync(folder);
    }

    public void refreshRoadCountersSync(RoadModel road) {
        long roadId = road.getId();
        long measCount = getMeasurementsCount(roadId);
        double distance = getOverallDistanceSync(-1, roadId, -1);
        double pathDistance = getPathDistanceSync(-1, roadId, -1);
        double avgIRI = getAverageIRISync(-1, roadId, -1);
        double avgSpeed = getAverageSpeedSync(-1, roadId, -1);
        float[] stats = getRoughnessStatisticsPercent(-1, roadId, -1);
        road.setExperiments(measCount);
        road.setOverallDistance(distance);
        road.setPathDistance(pathDistance);
        road.setAverageIRI(avgIRI);
        road.setAverageSpeed(avgSpeed);
        road.setIssuesStats(stats);
        updateRoadSync(road);
    }

    public void refreshMeasurementCountersSync(MeasurementModel measurement) {
        long measurementId = measurement.getId();
        long intervalsCount = getItemsCountSync(-1, -1, measurementId, false);
        double distance = getOverallDistanceSync(-1, -1, measurementId);
        double avgIRI = getAverageIRISync(-1, -1, measurementId);
        double pathDistance = getPathDistanceSync(-1, -1, measurementId);
        measurement.setIntervalsNumber(intervalsCount);
        measurement.setOverallDistance(distance);
        measurement.setPathDistance(pathDistance);
        measurement.setAvgIRI(avgIRI);
        updateMeasurementSync(measurement);
    }

    public float[] getRoughnessStatisticsPercent(long folderId, long roadId, long measurementId) {
        Cursor c = intervalsDao.getProcessedDataForId(folderId, roadId, measurementId);
        float[] stats = new float[4];
        int badIssues = 0;
        int normalIssues = 0;
        int goodIssues = 0;
        int perfectIssues = 0;
        int count = 0;
        ProcessedDataModel processedData = null;
        boolean hasData = true;
        if (c.getCount() > 0) {
            c.moveToFirst();
            while (hasData) {
                processedData = intervalsDao.cursorToRecord(c);
                RoadQuality quality = processedData.getCategory();
                switch (quality) {
                    case POOR:
                        badIssues++;
                        break;
                    case FAIR:
                        normalIssues++;
                        break;
                    case GOOD:
                        goodIssues++;
                        break;
                    case EXCELLENT:
                        perfectIssues++;
                        break;
                    default:
                        break;
                }
                count++;
                hasData = c.moveToNext();
            }
        }
        c.close();
        stats[0] = Math.round((float) badIssues / ((float) count * 0.01f));
        stats[1] = Math.round((float) normalIssues / ((float) count * 0.01f));
        stats[2] = Math.round((float) goodIssues / ((float) count * 0.01f));
        stats[3] = Math.round((float) perfectIssues / ((float) count * 0.01f));
        return stats;
    }

    public void moveRoadTo(final long folderId, final long roadId,
        final MeasurementsDataLoaderListener listener) {
        executor.runOperation(new Runnable() {
            @Override
            public void run() {
                moveMeasurementItemsToSync(-1, folderId, roadId);
                RoadModel road = roadDao.getRoad(roadId);
                if (road != null) {
                    road.setFolderId(folderId);
                    roadDao.updateRoad(road);
                }
                ActivityUtil.runOnMainThread(new Runnable() {
                    @Override
                    public void run() {
                        if (listener != null) {
                            listener.onDataLoaded(true);
                        }
                    }
                });
            }
        });
    }

    public void moveMeasurementToSync(final long measurementId, final long folderId, final long roadId) {
        MeasurementModel measurement = measurementDao.getMeasurementById(measurementId);
        measurement.setRoadId(roadId);
        moveMeasurementItemsToSync(measurementId, folderId, roadId);
        measurementDao.updateMeasurement(measurement);
    }

    public void moveMeasurementTo(final long measurementId, final long folderId, final long roadId,
        final MeasurementsDataLoaderListener listener) {
        executor.runOperation(new Runnable() {
            @Override
            public void run() {
                moveMeasurementToSync(measurementId, folderId, roadId);
                ActivityUtil.runOnMainThread(new Runnable() {
                    @Override
                    public void run() {
                        if (listener != null) {
                            listener.onDataLoaded(true);
                        }
                    }
                });
            }
        });
    }

    public void moveTagTo(final long tagId, final long folderId, final long roadId,
        final MeasurementsDataLoaderListener listener) {
        executor.runOperation(new Runnable() {
            @Override
            public void run() {
                moveTagToSync(tagId, folderId, roadId);
                ActivityUtil.runOnMainThread(new Runnable() {
                    @Override
                    public void run() {
                        if (listener != null) {
                            listener.onDataLoaded(true);
                        }
                    }
                });
            }
        });
    }

    public void moveMeasurementItemsToSync(final long measurementId, final long folderId, final long roadId) {
        intervalsDao.moveProcessedData(folderId, roadId, measurementId);
        bumpsDao.moveBumps(folderId, roadId, measurementId);
        geoTagDao.moveGeoTags(folderId, roadId, measurementId);
    }

    public void moveTagToSync(final long tagId, final long folderId, final long roadId) {
        TagModel tag = tagsDao.getItemById(tagId);
        tagsDao.moveTag(folderId, roadId, -1, tag);
    }

    public void deleteAllProjectsSync(boolean deleteRelatedData) {
        List<FolderModel> folders = folderDao.getAllFolders();
        for (FolderModel f : folders) {
            deleteProject(f.getId(), deleteRelatedData);
        }
    }

    public void deleteProject(long projectId, boolean deleteRelatedData) {
        folderDao.deleteFolder(projectId);
        if (deleteRelatedData) {
            List<RoadModel> roads = roadDao.getAllRoadsByFolderId(projectId);
            for (RoadModel m : roads) {
                deleteRoad(m.getId(), deleteRelatedData);
            }
        }
    }

    public void deleteRoad(long roadId, boolean deleteRelatedData) {
        roadDao.deleteRoad(roadId);
        if (deleteRelatedData) {
            List<MeasurementModel> measurements = measurementDao.getAllMeasurementsByRoadId(roadId);
            for (MeasurementModel m : measurements) {
                deleteMeasurement(m.getId(), deleteRelatedData);
            }
            tagsDao.deleteItemsWithRoadId(roadId);
        }
    }

    public void deleteMeasurement(long measurementId, boolean deleteRelatedData) {
        measurementDao.deleteMeasurement(measurementId);
        if (deleteRelatedData) {
            intervalsDao.deleteItemsWithMeasurementId(measurementId);
            bumpsDao.deleteItemsWithMeasurementId(measurementId);
            tagsDao.deleteItemsWithMeasurementId(measurementId);
            geoTagDao.deleteItemsWithMeasurementId(measurementId);
        }
    }

    public void deleteItem(MeasurementItem item) {
        if (item != null && item.getType() != null) {
            switch (item.getType()) {
                case INTERVAl:
                    intervalsDao.deleteProcessedData((ProcessedDataModel) item);
                    break;
                case BUMP:
                    bumpsDao.deleteBump((BumpModel) item);
                    break;
                case TAG:
                    tagsDao.delete((TagModel) item);
                case GEO_TAG:
                    geoTagDao.delete((GeoTagModel) item);
                    break;
            }
        }
    }

    public void updateProject(final FolderModel folder,
        final MeasurementsDataLoaderListener listener) {
        executor.runOperation(new Runnable() {
            @Override
            public void run() {
                updateProjectSync(folder);
                ActivityUtil.runOnMainThread(new Runnable() {
                    @Override
                    public void run() {
                        if (listener != null) {
                            listener.onDataLoaded(true);
                        }
                    }
                });
            }
        });
    }

    public void updateRoad(final RoadModel road,
        final MeasurementsDataLoaderListener listener) {
        executor.runOperation(new Runnable() {
            @Override
            public void run() {
                updateRoadSync(road);
                ActivityUtil.runOnMainThread(new Runnable() {
                    @Override
                    public void run() {
                        if (listener != null) {
                            listener.onDataLoaded(true);
                        }
                    }
                });
            }
        });
    }

    public void updateProjectSync(FolderModel folder) {
        folderDao.updateFolder(folder);
    }

    public void updateRoadSync(RoadModel road) {
        roadDao.updateRoad(road);
    }

    public void updateMeasurementSync(MeasurementModel measurement) {
        measurementDao.updateMeasurement(measurement);
    }

    public boolean isRoadAndFolderExists() {
        boolean isRoadAndFolderExists = false;
        long projectsCount = MeasurementsDataHelper.getInstance().getFoldersCount();
        if (projectsCount > 0) {
            long roadsCount = MeasurementsDataHelper.getInstance().getRoadsCount();
            isRoadAndFolderExists = roadsCount > 0;
        }
        return isRoadAndFolderExists;
    }

    public boolean isProjectsExists() {
        long roads = MeasurementsDataHelper.getInstance().getRoadsCount();
        long projects = getFoldersCount();
        if (projects < 1 || roads < 1) {
            return false;
        }
        return true;
    }

    public long getFoldersCount() {
        return folderDao.getAllItemsCount();
    }

    public long getRoadsCount() {
        return roadDao.getAllItemsCount();
    }

    public long createNewProjectSync(FolderModel folder, boolean checkRoad, boolean switchTo, boolean defaultRoad) {
        long folderId = folderDao.putFolder(folder);
        if (checkRoad) {
            long roads = getRoadsCount(folderId);
            if (roads < 1) {
                createDefaultRoad(folderId, true, switchTo, defaultRoad);
            }
        }
        if (switchTo) {
            PreferencesUtil.getInstance().setCurrentFolderId(folderId);
            RAApplication.getInstance().setCurrentFolderId(folderId);
        }
        return folderId;
    }

    public long createNewRoadSync(final RoadModel road, boolean switchTo) {
        long roadId = roadDao.putRoad(road);
        if (switchTo) {
            PreferencesUtil.getInstance().setCurrentRoadId(roadId);
            RAApplication.getInstance().setCurrentRoadId(roadId);
        }
        return roadId;
    }

    public void createNewProject(final FolderModel folder, final MeasurementsDataLoaderListener<Boolean> listener) {
        createNewProject(folder, true, listener);
    }

    public void createNewProject(final FolderModel folder, final boolean switchTo, final MeasurementsDataLoaderListener<Boolean> listener) {
        executor.runOperation(new Runnable() {
            @Override
            public void run() {
                createNewProjectSync(folder, true, switchTo, folder.isDefaultProject());
                ActivityUtil.runOnMainThread(new Runnable() {
                    @Override
                    public void run() {
                        if (listener != null) {
                            listener.onDataLoaded(true);
                        }
                    }
                });
            }
        });
    }

    public void createNewRoad(final RoadModel road, final MeasurementsDataLoaderListener<Boolean> listener) {
        createNewRoad(road, true, listener);
    }

    public void createNewRoad(final RoadModel road, final boolean switchTo, final MeasurementsDataLoaderListener<Boolean> listener) {
        executor.runOperation(new Runnable() {
            @Override
            public void run() {
                createNewRoadSync(road, switchTo);
                ActivityUtil.runOnMainThread(new Runnable() {
                    @Override
                    public void run() {
                        if (listener != null) {
                            listener.onDataLoaded(true);
                        }
                    }
                });
            }
        });
    }

    public void createDefaultProject() {
        createDefaultProject(false, true, true, true);
    }

    public long createDefaultProject(boolean sync) {
        return createDefaultProject(sync, true, true, true);
    }

    public long createDefaultProject(boolean sync, boolean checkRoad, boolean switchTo, boolean defaultProject) {
        FolderModel folderModel = new FolderModel(getContext().getString(R.string.default_folder_name));
        folderModel.setDefaultProject(defaultProject);
        long folderId = -1;
        if (sync) {
            folderId = createNewProjectSync(folderModel, checkRoad, switchTo, defaultProject);
        } else {
            createNewProject(folderModel, null);
        }
        return folderId;
    }

    public void createDefaultRoad(long folderId) {
        createDefaultRoad(folderId, false, true, true);
    }

    public long createDefaultRoad(long folderId, boolean sync) {
        return createDefaultRoad(folderId, sync, true, true);
    }

    public long createDefaultRoad(long folderId, boolean sync, boolean switchTo, boolean defaultRoad) {
        RoadModel roadModel = new RoadModel(getContext().getString(R.string.default_road_name), folderId);
        roadModel.setDefaultRoad(defaultRoad);
        long roadId = -1;
        if (sync) {
            roadId = createNewRoadSync(roadModel, switchTo);
        } else {
            createNewRoad(roadModel, null);
        }
        return roadId;
    }
}
