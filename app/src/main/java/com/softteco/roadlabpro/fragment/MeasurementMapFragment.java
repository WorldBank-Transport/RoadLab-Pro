package com.softteco.roadlabpro.fragment;

import com.google.android.gms.maps.model.Marker;
import com.softteco.roadlabpro.menu.ScreenItems;
import com.softteco.roadlabpro.sqlite.model.BumpModel;
import com.softteco.roadlabpro.sqlite.model.MeasurementItem;
import com.softteco.roadlabpro.sqlite.model.RoadModel;
import com.softteco.roadlabpro.sqlite.MeasurementsDataHelper;
import com.softteco.roadlabpro.sqlite.model.TagModel;

import java.util.List;

public class MeasurementMapFragment extends PopupMeasurementMapFragment {

    private RoadModel roadModel;

    @Override
    protected void checkArgs() {
        if (getArguments() != null && getArguments().getSerializable(MeasurementListFragment.ARG_MEASUREMENT_LIST) != null) {
            roadModel = (RoadModel) getArguments().getSerializable(MeasurementListFragment.ARG_MEASUREMENT_LIST);
        }
    }

    @Override
    protected void updateTitle() {
        setTitle(roadModel.getName());
    }

    @Override
    protected void initMapData() {
        MeasurementsDataHelper.getInstance().
                getData(roadModel, new MeasurementsDataHelper.MeasurementsDataLoaderListener<List<MeasurementItem>>() {
                    @Override
                    public void onDataLoaded(List<MeasurementItem> items) {
                        onItemsReady(items, true);
                    }
                });
    }

    @Override
    public int getTypeFragment() {
        return ScreenItems.SCREEN_MEASUREMENT;
    }

    @Override
    public boolean isHomeIndicatorMenu() {
        return false;
    }
}

