package com.softteco.roadlabpro.fragment;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;
import com.softteco.roadlabpro.RAApplication;
import com.softteco.roadlabpro.menu.ScreenItems;
import com.softteco.roadlabpro.sqlite.MeasurementsDataHelper;
import com.softteco.roadlabpro.sqlite.model.MeasurementItem;
import com.softteco.roadlabpro.sqlite.model.TagModel;

import java.util.List;

public class TagsMapFragment extends PopupMeasurementMapFragment {

    @Override
    protected void initMap(GoogleMap map) {
        super.initMap(map);
        map.setOnMarkerClickListener(this);
    }

    @Override
    protected void initMapData() {
        clearMap();
        long roadId = RAApplication.getInstance().getCurrentRoadId();
        MeasurementsDataHelper.getInstance().getTags(roadId,
        new MeasurementsDataHelper.MeasurementsDataLoaderListener<List<MeasurementItem>>() {
            @Override
            public void onDataLoaded(List<MeasurementItem> items) {
                onItemsReady(items, true);
            }
        });
    }

    @Override
    public int getTypeFragment() {
        return ScreenItems.SCREEN_TAGS;
    }

    @Override
    public boolean isHomeIndicatorMenu() {
        return true;
    }

    @Override
    protected void onPopupClicked(MeasurementItem item) {
        if (item instanceof TagModel) {
            openTagDetails((TagModel) item);
        }
    }

    public void openTagDetails(TagModel tag) {
        replaceFragment(TagDetailsFragment.newInstance(tag), true);
    }
}
