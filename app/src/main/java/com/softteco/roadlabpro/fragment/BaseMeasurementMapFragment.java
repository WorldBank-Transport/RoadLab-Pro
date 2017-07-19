package com.softteco.roadlabpro.fragment;

import android.os.Bundle;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.Polyline;
import com.softteco.roadlabpro.sqlite.model.MeasurementItem;

import java.util.ArrayList;
import java.util.List;

public abstract class BaseMeasurementMapFragment extends BaseMapFragment implements GoogleMap.OnMarkerClickListener, GoogleMap.OnPolylineClickListener {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        checkArgs();
    }

    protected void checkArgs() {
    }

    protected void onItemsReady(List<MeasurementItem> items) {
        onItemsReady(items, false);
    }

    protected void onItemsReady(List<MeasurementItem> items, boolean addToCache) {
        setRefreshMyLocation(false);
        setGoToLocation(false);
        List<LatLng> locations = new ArrayList<>();
        if (items != null) {
            for (MeasurementItem item : items) {
                getMapHelper().addMarker(getContext(), getMap(), item, addToCache);
                locations.add(new LatLng(item.getLatitude(), item.getLongitude()));
            }
            LatLng loc = getMyLocation();
            if (loc != null) {
                locations.add(loc);
            }
            fitMap(locations, true, getPadding());
        }
        refreshMyLocation();
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        return false;
    }

    @Override
    public void onPolylineClick(Polyline line) {
    }
}

