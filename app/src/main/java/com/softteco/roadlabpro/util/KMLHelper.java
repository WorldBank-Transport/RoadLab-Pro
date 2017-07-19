package com.softteco.roadlabpro.util;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.drm.ProcessedData;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;

import com.ekito.simpleKML.Serializer;
import com.ekito.simpleKML.model.Boundary;
import com.ekito.simpleKML.model.Coordinate;
import com.ekito.simpleKML.model.Coordinates;
import com.ekito.simpleKML.model.Document;
import com.ekito.simpleKML.model.Feature;
import com.ekito.simpleKML.model.Geometry;
import com.ekito.simpleKML.model.Icon;
import com.ekito.simpleKML.model.IconStyle;
import com.ekito.simpleKML.model.Kml;
import com.ekito.simpleKML.model.LineString;
import com.ekito.simpleKML.model.LineStyle;
import com.ekito.simpleKML.model.LinearRing;
import com.ekito.simpleKML.model.Placemark;
import com.ekito.simpleKML.model.Point;
import com.ekito.simpleKML.model.PolyStyle;
import com.ekito.simpleKML.model.Polygon;
import com.ekito.simpleKML.model.Style;
import com.ekito.simpleKML.model.StyleSelector;
import com.softteco.roadlabpro.R;
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
import com.softteco.roadlabpro.sqlite.model.BumpModel;
import com.softteco.roadlabpro.sqlite.model.FolderModel;
import com.softteco.roadlabpro.sqlite.model.GeoTagModel;
import com.softteco.roadlabpro.sqlite.model.MeasurementItem;
import com.softteco.roadlabpro.sqlite.model.MeasurementItemType;
import com.softteco.roadlabpro.sqlite.model.MeasurementModel;
import com.softteco.roadlabpro.sqlite.model.ProcessedDataModel;
import com.softteco.roadlabpro.sqlite.model.RoadModel;
import com.softteco.roadlabpro.sqlite.model.TagModel;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class KMLHelper {

    private static final String TAG = "KMLHelper";
    private static final String ALTITUDE_MODE = "clampToSeaFloor";
    private volatile boolean stopProcess = false;

    public KMLHelper() {
    }

    private Context getContext() {
        return RAApplication.getInstance().getBaseContext();
    }

    public Placemark convertIntervalToKml(ProcessedDataModel data) {
        Placemark mark = new Placemark();
        mark.setName(getTitle(data));
        String iriStr = getIri(data);
        String description = data.getDescription();
        String suspensionStr = getSuspension(data);
        mark.setDescription(
               (TextUtils.isEmpty(description) ? "" : description)
             + (TextUtils.isEmpty(iriStr) ? "" : " " + iriStr)
             + (TextUtils.isEmpty(suspensionStr) ? "" : " " + suspensionStr));
        Style style = getIntervalStyle(data);
        List<StyleSelector> styles = new ArrayList<>();
        styles.add(style);
        mark.setStyleSelector(styles);
        LineString lineString = new LineString();
        Coordinate startCoord = null;
        Coordinate endCoord = null;
        ArrayList<Coordinate> coordList = new ArrayList<Coordinate>();
        if (data.getCoordsStart() != null && data.getCoordsStart().length >= 3) {
            startCoord = new Coordinate(data.getCoordsStart()[1], data.getCoordsStart()[0], 0d /*data.getCoordsStart()[2]*/);
            coordList.add(startCoord);
        }
        if (data.getCoordsEnd() != null && data.getCoordsEnd().length >= 3) {
            endCoord = new Coordinate(data.getCoordsEnd()[1], data.getCoordsEnd()[0], 0d /*data.getCoordsEnd()[2]*/);
            coordList.add(endCoord);
        }
        Coordinates coord = new Coordinates();
        coord.setList(coordList);
        lineString.setCoordinates(coord);
        lineString.setAltitudeMode(ALTITUDE_MODE);
        //lineString.setExtrude(1);
        //lineString.setTessellate(1);
        List<Geometry> geometryList = new ArrayList<>();
        geometryList.add(lineString);
        mark.setGeometryList(geometryList);
        return mark;
    }

    public Placemark convertGeoTagToKml(String measurementName, List<GeoTagModel> geoTagsList) {
        Placemark mark = new Placemark();
        mark.setName(measurementName);
        mark.setDescription(measurementName);
        Style style = getGeoTagsStyle();
        List<StyleSelector> styles = new ArrayList<>();
        styles.add(style);
        mark.setStyleSelector(styles);
        LineString lineString = new LineString();
        Coordinate coord = null;
        ArrayList<Coordinate> coordList = new ArrayList<Coordinate>();
        for (GeoTagModel tag : geoTagsList) {
            coord = new Coordinate(tag.getLongitude(), tag.getLatitude(), 0d /*tag.getAltitude()*/);
            coordList.add(coord);
        }
        Coordinates coords = new Coordinates();
        coords.setList(coordList);
        lineString.setCoordinates(coords);
        lineString.setAltitudeMode(ALTITUDE_MODE);
        List<Geometry> geometryList = new ArrayList<>();
        geometryList.add(lineString);
        mark.setGeometryList(geometryList);
        return mark;
    }

    public Placemark convertBumpToKml(BumpModel data) {
        Placemark mark = new Placemark();
        mark.setName(getTitle(data));
        String description = data.getDescription();
        mark.setDescription(TextUtils.isEmpty(description) ? "" : description);
        Style style = getBumpStyle();
        List<StyleSelector> styles = new ArrayList<>();
        styles.add(style);
        mark.setStyleSelector(styles);
        Point point = new Point();
        Coordinate coord = new Coordinate(data.getLongitude(), data.getLatitude(), 0d/* data.getAltitude()*/);
        point.setCoordinates(coord);
        List<Geometry> geometryList = new ArrayList<>();
        geometryList.add(point);
        mark.setGeometryList(geometryList);
        return mark;
    }

    public Placemark convertTagToKml(TagModel data) {
        Placemark mark = new Placemark();
        mark.setName(getTitle(data));
        String iriStr = getIri(data);
        String description = data.getDescription();
        mark.setDescription(
                (TextUtils.isEmpty(description) ? "" : description)
              + (TextUtils.isEmpty(iriStr) ? "" : " " + iriStr));
        Style style = getTagStyle(data);
        List<StyleSelector> styles = new ArrayList<>();
        styles.add(style);
        mark.setStyleSelector(styles);
        Point point = new Point();
        Coordinate coord = new Coordinate(data.getLongitude(), data.getLatitude(), 0d/*data.getAltitude()*/);
        point.setCoordinates(coord);
        List<Geometry> geometryList = new ArrayList<>();
        geometryList.add(point);
        mark.setGeometryList(geometryList);
        return mark;
    }

    private String getIri(MeasurementItem item) {
        return MeasurementsDataHelper.getInstance().getIRIStr(item.getIri());
    }

    private String getSuspension(ProcessedDataModel item) {
        int suspension = item.getSuspension();
        PreferencesUtil.SUSPENSION_TYPES suspensionType = null;
        String suspensionStr = "";
        if (suspension < PreferencesUtil.SUSPENSION_TYPES.values().length) {
            suspensionType = PreferencesUtil.SUSPENSION_TYPES.values()[suspension];
            suspensionStr = suspensionType.name();
        }
        suspensionStr = getContext().getString(R.string.measurements_item_suspension, suspensionStr);
        return suspensionStr;
    }

    private Style getIntervalStyle(ProcessedDataModel model) {
        Style mapStyle = new Style();
        RoadQuality category = RoadQuality.NONE;
        if (model.getCategory() != null) {
            category = model.getCategory();
        }
        int color = category.getRoadConditionColor(getContext());
        LineStyle lineStyle = new LineStyle();
        String colorStr = getMapColor(color);
        lineStyle.setColor(colorStr);
        lineStyle.setWidth(4f);
        PolyStyle polyStyle = new PolyStyle();
        polyStyle.setColor(colorStr);
        //polyStyle.setFill(1);
        mapStyle.setLineStyle(lineStyle);
        mapStyle.setPolyStyle(polyStyle);
        return mapStyle;
    }

    private String getMapColor(int color) {
        int[] argb = UIUtils.separateColorRgb(color);
        int a = argb[3];
        int r = argb[2];
        int g = argb[1];
        int b = argb[0];
        String AStr = Integer.toHexString(a);
        String RStr = Integer.toHexString(r);
        String GStr = Integer.toHexString(g);
        String BStr = Integer.toHexString(b);
        String colorStr = AStr + BStr + GStr + RStr;
        return colorStr;
    }

    private Style getTagStyle(TagModel tag) {
        Style mapStyle = new Style();
        IconStyle iconStyle = new IconStyle();
        TagModel.RoadCondition condition = tag.getRoadCondition();
        if (condition == null) {
            condition = TagModel.RoadCondition.NONE;
        }
        Icon icon = new Icon();
        switch (condition) {
            case GOOD:
                icon.setHref(Constants.TAG_KML_ICON_PATH + Constants.TAG_KML_ICON_GREEN);
                break;
            case FAIR:
                icon.setHref(Constants.TAG_KML_ICON_PATH + Constants.TAG_KML_ICON_LIGHT_BLUE);
                break;
            case POOR:
                icon.setHref(Constants.TAG_KML_ICON_PATH + Constants.TAG_KML_ICON_ORANGE);
                break;
            case BAD:
                icon.setHref(Constants.TAG_KML_ICON_PATH + Constants.TAG_KML_ICON_RED);
                break;
            default:
                icon.setHref(Constants.TAG_KML_ICON_PATH + Constants.TAG_KML_ICON_YELLOW);
                break;
        }
        iconStyle.setIcon(icon);
        mapStyle.setIconStyle(iconStyle);
        return mapStyle;
    }

    private Style getBumpStyle() {
        Style mapStyle = new Style();
        IconStyle iconStyle = new IconStyle();
        Icon icon = new Icon();
        icon.setHref(Constants.BUMP_KML_ICON);
        iconStyle.setIcon(icon);
        mapStyle.setIconStyle(iconStyle);
        return mapStyle;
    }

    private Style getGeoTagsStyle() {
        Style mapStyle = new Style();
        int color = getContext().getResources().getColor(R.color.type_issue_blue);
        LineStyle lineStyle = new LineStyle();
        String colorStr = getMapColor(color);
        lineStyle.setColor(colorStr);
        lineStyle.setWidth(3f);
        PolyStyle polyStyle = new PolyStyle();
        polyStyle.setColor(colorStr);
        mapStyle.setLineStyle(lineStyle);
        mapStyle.setPolyStyle(polyStyle);
        return mapStyle;
    }

    private String getTitle(MeasurementItem item) {
        long id = item.getId();
        long dateMillis = item.getTime();
        Date date = new Date(dateMillis);
        String dateStr = DateUtil.format(date, DateUtil.Format.HHMMSS);
        return getContext().getString(R.string.measurements_item_list_title, String.valueOf(id), dateStr);
    }

    private Kml getKml(Date date, MeasurementItemType type) {
        Kml kml = new Kml();
        Document doc = new Document();
        kml.setFeature(doc);
        switch (type) {
            case INTERVAl:
                doc.setName(getName(date, MeasurementItemType.INTERVAl));
                doc.setDescription(MeasurementItemType.INTERVAl.getName());
                break;
            case BUMP:
                doc.setName(getName(date, MeasurementItemType.BUMP));
                doc.setDescription(MeasurementItemType.BUMP.getName());
                break;
            case TAG:
                doc.setName(getName(date, MeasurementItemType.TAG));
                doc.setDescription(MeasurementItemType.TAG.getName());
                break;
            case GEO_TAG:
                doc.setName(getName(date, MeasurementItemType.GEO_TAG));
                doc.setDescription(MeasurementItemType.GEO_TAG.getName());
                break;
        }
        return kml;
    }

    public void exportMeasurementData(String folderName, String roadName, MeasurementModel measurement) {
        String measurementName;
        long measurementId;
        String dataDir;
        String projectPath;
        measurementName = FileUtils.getExportItemName(getContext(), measurement.getId(), measurement.getDate());
        measurementId = measurement.getId();
        projectPath = folderName + "/" + roadName + "/" + measurementName + "/";
        dataDir = FileUtils.getDataDir(projectPath, true);
        Date measurementDate = new Date(measurement.getDate());
        writeRoadIntervalsFile(measurementDate, dataDir, measurementId);
        writeBumpsFile(measurementDate, dataDir, measurementId);
        writeGeoTagsFile(measurementDate, dataDir, measurementId);
    }

    private static String getName(Date date, MeasurementItemType type) {
        String name;
        String typeName = type.getName();
        String dateStr = new SimpleDateFormat(TimeUtil.DATE_FILENAME_FORMAT).format(date);
        String deviceName = DeviceUtil.getDeviceName();
        switch (type) {
            case GEO_TAG:
                name = String.format(typeName + "_%s_%s" + Constants.GEO_TAG_SUFFIX, deviceName, dateStr);
                break;
            case INTERVAl:
            case BUMP:
            case TAG:
            default:
                name = String.format(typeName + "_%s_%s", deviceName, dateStr);
                break;
        }
        return name;
    }

    public static File createOutFile(Date date, String destinationDataDir, MeasurementItemType type) {
        File file = new File(destinationDataDir + getName(date, type) + Constants.KML_EXT);
        return file;
    }

    public String writeRoadIntervalsFile(Date date, String dataDir, long measurementId) {
        stopProcess = false;
        File file = createOutFile(date, dataDir, MeasurementItemType.INTERVAl);
        try {
            ProcessedDataDAO dao = MeasurementsDataHelper.getInstance().getIntervalsDao();
            Cursor cur = dao.getProcessedDataByMeasurementIdCursor(measurementId);
            List<Feature> intervalsPlacemarks = new ArrayList<Feature>();
            do {
                if (stopProcess) {
                    break;
                }
                if (cur.getCount() == 0) {
                    Log.d(TAG, "cur.getCount() == 0");
                    continue;
                }
                ProcessedDataModel model = dao.cursorToRecord(cur);
                if (model != null) {
                    Placemark mark = convertIntervalToKml(model);
                    intervalsPlacemarks.add(mark);
                }
            } while (cur.moveToNext());
            cur.close();
            Kml kml = getKml(date, MeasurementItemType.INTERVAl);
            ((Document)kml.getFeature()).setFeatureList(intervalsPlacemarks);
            Serializer serializer = new Serializer();
            serializer.write(kml, file);
            return file.getAbsolutePath();
        } catch (Exception e) {
            Log.e(TAG, "writeRoadIntervalsFile", e);
            return null;
        }
    }

    public String writeTagsFile(Date date, String destinationDataDir, long roadId) {
        File file = createOutFile(date, destinationDataDir, MeasurementItemType.TAG);
        try {
            TagDAO dao = MeasurementsDataHelper.getInstance().getTagsDao();
            Cursor cur = dao.getTagsByRoadIdCursor(roadId);
            List<Feature> tagsPlacemarks = new ArrayList<Feature>();
            do {
                if (stopProcess) {
                    break;
                }
                if (cur.getCount() == 0) {
                    Log.d(TAG, "cur.getCount() == 0");
                    continue;
                }
                TagModel model = dao.cursorToTag(cur);
                if (model != null) {
                    Placemark mark = convertTagToKml(model);
                    tagsPlacemarks.add(mark);
                }
            } while (cur.moveToNext());
            cur.close();
            Kml kml = getKml(date, MeasurementItemType.TAG);
            ((Document)kml.getFeature()).setFeatureList(tagsPlacemarks);
            Serializer serializer = new Serializer();
            serializer.write(kml, file);
            return file.getAbsolutePath();
        } catch (Exception e) {
            Log.e(TAG, "writeTagsFile", e);
            return null;
        }
    }

    public String writeBumpsFile(Date date, String dataDir, long measurementId) {
        File file = createOutFile(date, dataDir, MeasurementItemType.BUMP);
        try {
            BumpDAO dao = MeasurementsDataHelper.getInstance().getBumpsDao();
            Cursor cur = dao.getBumpByMeasurementIdCursor(measurementId);
            List<Feature> bumpsPlacemarks = new ArrayList<Feature>();
            do {
                if (stopProcess) {
                    break;
                }
                if (cur.getCount() == 0) {
                    Log.d(TAG, "cur.getCount() == 0");
                    continue;
                }
                BumpModel model = dao.cursorToRecord(cur);
                if (model != null) {
                    Placemark mark = convertBumpToKml(model);
                    bumpsPlacemarks.add(mark);
                }
            } while (cur.moveToNext());
            cur.close();
            Kml kml = getKml(date, MeasurementItemType.BUMP);
            ((Document)kml.getFeature()).setFeatureList(bumpsPlacemarks);
            Serializer serializer = new Serializer();
            serializer.write(kml, file);
            return file.getAbsolutePath();
        } catch (Exception e) {
            Log.e(TAG, "writeBumpsFile", e);
            return null;
        }
    }

    public String writeGeoTagsFile(Date date, String dataDir, long measurementId) {
        File file = createOutFile(date, dataDir, MeasurementItemType.GEO_TAG);
        String absolutePath = file.getAbsolutePath();
        String measurementName = getName(date, MeasurementItemType.GEO_TAG);
        try {
            GeoTagDAO dao = MeasurementsDataHelper.getInstance().getGeoTagDao();
            List<GeoTagModel> geoTagsList = dao.getGeoTagsByMeasurementId(measurementId);
            List<Feature> geoTagsPlacemarks = new ArrayList<Feature>();
            if (stopProcess) {
                return absolutePath;
            }
            if (geoTagsList != null) {
                Placemark mark = convertGeoTagToKml(measurementName, geoTagsList);
                geoTagsPlacemarks.add(mark);
            }
            Kml kml = getKml(date, MeasurementItemType.GEO_TAG);
            ((Document)kml.getFeature()).setFeatureList(geoTagsPlacemarks);
            Serializer serializer = new Serializer();
            serializer.write(kml, file);
            return absolutePath;
        } catch (Exception e) {
            Log.e(TAG, "writeBumpsFile", e);
            return null;
        }
    }
}
