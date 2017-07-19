package com.softteco.roadlabpro.util;

import android.content.Context;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.softteco.roadlabpro.R;
import com.softteco.roadlabpro.algorithm.RoadQuality;
import com.softteco.roadlabpro.fragment.StartMeasurementFragment;
import com.softteco.roadlabpro.sqlite.model.BumpModel;
import com.softteco.roadlabpro.sqlite.model.MeasurementItem;
import com.softteco.roadlabpro.sqlite.model.ProcessedDataModel;
import com.softteco.roadlabpro.sqlite.model.TagModel;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MeasurementsGoogleMapHelper {

    protected static final String TAG = MeasurementsGoogleMapHelper.class.getSimpleName();

    private BitmapDescriptor bumpIcon;

    private Map<Marker, MeasurementItem> markersCache;
    private Map<Polyline, MeasurementItem> linesCache;

    public MeasurementsGoogleMapHelper() {
        markersCache = new HashMap<Marker, MeasurementItem>();
        linesCache = new HashMap<Polyline, MeasurementItem>();
    }

    public void addMarker(Context context, GoogleMap map, MeasurementItem item, boolean addCache) {
        switch(item.getType()) {
            case INTERVAl:
                if (item instanceof ProcessedDataModel) {
                    addInterval(context, map, (ProcessedDataModel) item, addCache);
                }
                break;
            case TAG:
                if (item instanceof TagModel) {
                    addTag(context, map, (TagModel) item, addCache);
                }
                break;
            case BUMP:
                if (item instanceof BumpModel) {
                    addBump(context, map, (BumpModel) item, addCache);
                }
                break;
        }
    }

    private void addInterval(Context context, GoogleMap map, ProcessedDataModel item, boolean addCache) {
        double[] coordsStart = item.getCoordsStart();
        double[] coordsEnd = item.getCoordsStart();
        if (coordsStart == null || coordsEnd == null
                || coordsStart.length < 2 || coordsEnd.length < 2) {
            return;
        }
        LatLng startLine = new LatLng(item.getCoordsStart()[0], item.getCoordsStart()[1]);
        LatLng endLine = new LatLng(item.getCoordsEnd()[0], item.getCoordsEnd()[1]);
        Polyline line = map.addPolyline(new PolylineOptions()
                .add(startLine, endLine)
                .geodesic(true)
                .width(context.getResources().getDimension(R.dimen.map_polyline_width))
                .color(getIriColor(context, item.getCategory())));
        if (addCache && linesCache != null) {
            linesCache.put(line, item);
        }
    }

    private int getIriColor(Context context, RoadQuality category) {
        if (category == null) {
            category = RoadQuality.NONE;
        }
        switch (category) {
            case EXCELLENT:
                return context.getResources().getColor(R.color.chart_perfect_roads_color);
            case GOOD:
                return context.getResources().getColor(R.color.chart_good_roads_color);
            case FAIR:
                return context.getResources().getColor(R.color.chart_normal_roads_color);
            case POOR:
                return context.getResources().getColor(R.color.chart_bad_roads_color);
            default:
                return context.getResources().getColor(R.color.chart_none_roads_color);
        }
    }

    private void addTag(Context context, GoogleMap map, TagModel item, boolean addCache) {
        LatLng location = new LatLng(item.getLatitude(), item.getLongitude());
        BitmapDescriptor tagIcon = getTagIcon(item.getRoadCondition());
        Marker marker = map.addMarker(new MarkerOptions()
                .position(location)
                .icon(tagIcon)
                .anchor(0.5f, 0.5f)
                .title(item.getName())
                .snippet(item.getDescription()));
        if (addCache && markersCache != null) {
            markersCache.put(marker, item);
        }
    }

    public BitmapDescriptor getTagIcon(TagModel.RoadCondition condition) {
        BitmapDescriptor tagIcon = null;
        int resourceId = getTagIconResId(condition);
        tagIcon = BitmapDescriptorFactory.fromResource(resourceId);
        return tagIcon;
    }

    public int getTagIconResId(TagModel.RoadCondition condition) {
        int resourceId = R.drawable.ic_map_tag_black;
        if (condition != null) {
            switch (condition) {
                case GOOD:
                    resourceId = R.drawable.ic_map_tag_green;
                    break;
                case FAIR:
                    resourceId = R.drawable.ic_map_tag_light_green;
                    break;
                case POOR:
                    resourceId = R.drawable.ic_map_tag_orange;
                    break;
                case BAD:
                    resourceId = R.drawable.ic_map_tag_red;
                    break;
            }
        }
        return resourceId;
    }

    private void addBump(Context context, GoogleMap map, BumpModel item, boolean addCache) {
        LatLng location = new LatLng(item.getLatitude(), item.getLongitude());
        if (bumpIcon == null) {
            bumpIcon = BitmapDescriptorFactory.fromResource(R.drawable.ic_map_bump_red);
        }
        Marker marker = map.addMarker(new MarkerOptions()
                .position(location)
                .icon(bumpIcon)
                .anchor(0.5f, 0.5f)
                .title(item.getName())
                .snippet(item.getDescription()));
        if (addCache && markersCache != null) {
            markersCache.put(marker, item);
        }
    }

    public boolean isMarkerItemAlreadyAdded(MeasurementItem item) {
        if (item != null && item.getType() != null) {
            switch (item.getType()) {
                case BUMP:
                case TAG:
                    if (markersCache != null) {
                        return markersCache.containsValue(item);
                    }
                case INTERVAl:
                    if (linesCache != null) {
                        return linesCache.containsValue(item);
                    }
            }
        }
        return false;
    }

    public MeasurementItem getItemByMarker(Marker marker) {
        if (markersCache != null) {
            return markersCache.get(marker);
        }
        return null;
    }

    public MeasurementItem getItemByLine(Polyline line) {
        if (linesCache != null) {
            return linesCache.get(line);
        }
        return null;
    }

    public void fitMap(GoogleMap map, List<LatLng> locations, boolean animate, int padding) {
        if (map == null) {
            return;
        }
        LatLngBounds bounds = getLatLngBounds(locations);
        if (bounds == null ) {
            return;
        }
        CameraUpdate cUpdate = null;
        try {
            cUpdate = CameraUpdateFactory.newLatLngBounds(bounds, padding);
            if (animate) {
                map.animateCamera(cUpdate);
            } else {
                map.moveCamera(cUpdate);
            }
        } catch (Exception e) {
            Log.e(TAG, e != null && e.getMessage() != null ? e.getMessage() : "");
        }
    }

    public LatLngBounds getLatLngBounds(List<LatLng> locations) {
        LatLngBounds bounds = null;
        if (locations == null || locations.size() == 0) {
            return bounds;
        }
        LatLngBounds.Builder b = new LatLngBounds.Builder();
        for (LatLng p: locations) {
            b.include(p);
        }
        bounds = b.build();
        if (bounds.northeast == null || bounds.southwest == null) {
            return null;
        }
        return bounds;
    }

    public void clear() {
        if (markersCache != null) {
            markersCache.clear();
        }
        if (linesCache != null) {
            linesCache.clear();
        }
    }

    public void destroy() {
        clear();
        markersCache = null;
        linesCache = null;
    }
}
