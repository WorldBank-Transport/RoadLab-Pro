package com.softteco.roadlabpro.fragment;

import android.location.Location;
import android.os.Bundle;
import android.view.View;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.softteco.roadlabpro.R;
import com.softteco.roadlabpro.RAApplication;
import com.softteco.roadlabpro.menu.ScreenItems;
import com.softteco.roadlabpro.sensors.GPSDetector;
import com.softteco.roadlabpro.util.ActivityUtil;
import com.softteco.roadlabpro.util.Constants;
import com.softteco.roadlabpro.util.MeasurementsGoogleMapHelper;

import java.util.List;

public class BaseMapFragment extends AbstractWBFragment {

    private MapView mapView;
    private GoogleMap map;
    private Marker myLocation;
    private boolean goToLocation = false;
    private boolean refreshMyLocation = true;
    private boolean firstLocationRefresh = true;

    private MeasurementsGoogleMapHelper mapHelper;

    /**
     * The method initializes Google map.
     *
     * @param bundle @see {@link GoogleMap}
     * @return instance {@link GoogleMap}
     * @see {@link GoogleMap}
     */
    public void initializeGoogleMap(final Bundle bundle) {
        mapView = (MapView) getView().findViewById(R.id.fr_road_issue_mapview);
        mapView.onCreate(bundle);
        mapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                map = googleMap;
                initMap(googleMap);
            }
        });
    }

    @Override
    public void onViewCreated(final View view, final Bundle bundle) {
        super.onViewCreated(view, bundle);
        mapHelper = new MeasurementsGoogleMapHelper();
        initializeGoogleMap(bundle);
    }

    public MeasurementsGoogleMapHelper getMapHelper() {
        if (mapHelper == null) {
            mapHelper = new MeasurementsGoogleMapHelper();
        }
        return mapHelper;
    }

    protected void initMap(GoogleMap map) {
        goToLocation = true;
        firstLocationRefresh = true;
        if (map != null) {
            MapsInitializer.initialize(getActivity());
            RAApplication.getInstance().getGpsDetector().setGpsMapListener(new GPSDetector.GpsMapListener() {
                @Override
                public void onGpsMapListener(final Location location) {
                    ActivityUtil.runOnMainThread(new Runnable() {
                        @Override
                        public void run() {
                            if (refreshMyLocation) {
                                setCurrentLocation(location);
                            }
                        }
                    });
                }
            });
            clearMap();
            LatLng loc = new LatLng(Constants.LATITUDE_BELARUS, Constants.LONGITUDE_BELARUS);
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(loc, Constants.DEFAULT_CAMERA_ZOOM));
            refreshMyLocation(loc);
            initMapData();
        }
    }

    public void refreshMyLocation() {
        Location loc = RAApplication.getInstance().getGpsDetector().getLocation();
        if (loc != null) {
            refreshMyLocation(new LatLng(loc.getLatitude(), loc.getLongitude()));
        }
    }

    public void refreshMyLocation(LatLng location) {
        if (myLocation != null) {
            myLocation.setPosition(location);
        } else {
            myLocation = map.addMarker(new MarkerOptions().position(location).
                    icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_my_location)));
        }
    }

    protected LatLng getMyLocation(Location loc) {
        if (loc != null) {
            return new LatLng(loc.getLatitude(), loc.getLongitude());
        }
        return null;
    }

    protected LatLng getMyLocation() {
        Location loc = RAApplication.getInstance().getGpsDetector().getLocation();
        return getMyLocation(loc);
    }

    protected void initMapData() {
    }

    public void setRefreshMyLocation(boolean refreshMyLocation) {
        this.refreshMyLocation = refreshMyLocation;
    }

    private void setCurrentLocation(Location location) {
        if (location == null) {
            return;
        }
        final LatLng latLng = getMyLocation(location);
        if (goToLocation) {
            map.animateCamera(CameraUpdateFactory.
                    newLatLngZoom(latLng, Constants.DEFAULT_LOCATION_FOUND_CAMERA_ZOOM));
            if (firstLocationRefresh) {
                goToLocation = false;
                firstLocationRefresh = false;
            }
        }
        refreshMyLocation(latLng);
    }


    public void fitMap(List<LatLng> locations, boolean animate, int padding) {
        if (mapHelper != null) {
            mapHelper.fitMap(getMap(), locations, animate, padding);
        }
    }

    protected int getPadding() {
        float fitPadding = 70;
        if (getAppResouces() != null) {
            fitPadding = getAppResouces().getDimension(R.dimen.map_fit_padding);
        }
        return (int) fitPadding;
    }

    protected void clearMap() {
        myLocation = null;
        if (mapHelper != null) {
            mapHelper.clear();
        }
        if (getMap() != null) {
            getMap().clear();
        }
    }

    public void enableMap(boolean enable) {
        if (getMap() != null) {
            getMap().getUiSettings().setAllGesturesEnabled(enable);
        }
    }

    public GoogleMap getMap() {
        return map;
    }

    public MapView getMapView() {
        return mapView;
    }

    public void setGoToLocation(boolean goToLocation) {
        this.goToLocation = goToLocation;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mapView != null) {
            mapView.onResume();
        }
        if (getMainActivity() != null && getMainActivity().getAddButton() != null) {
            getMainActivity().getAddButton().setVisibility(View.GONE);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mapView != null) {
            mapView.onPause();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mapHelper != null) {
           mapHelper.destroy();
        }
        if (mapView != null) {
            mapView.onDestroy();
        }
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        if (mapView != null) {
            mapView.onLowMemory();
        }
    }

    @Override
    public void onSaveInstanceState(final Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mapView != null) {
            mapView.onSaveInstanceState(outState);
        }
    }

    @Override
    public int getLayoutFragmentResources() {
        return R.layout.fragment_map;
    }

    @Override
    public int getMenuFragmentResources() {
        return -1;
    }

    @Override
    public int getTypeFragment() {
        return ScreenItems.SCREEN_ROADS;
    }

    @Override
    public boolean isHomeIndicatorMenu() {
        return false;
    }
}

