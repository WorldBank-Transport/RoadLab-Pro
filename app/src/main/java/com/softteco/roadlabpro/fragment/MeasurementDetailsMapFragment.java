package com.softteco.roadlabpro.fragment;

import android.support.v4.app.Fragment;
import android.view.View;

import com.softteco.roadlabpro.menu.ScreenItems;
import com.softteco.roadlabpro.sqlite.model.MeasurementItem;
import com.softteco.roadlabpro.sqlite.model.MeasurementModel;
import com.softteco.roadlabpro.sqlite.MeasurementsDataHelper;
import com.softteco.roadlabpro.util.FileUtils;

import java.util.List;

public class MeasurementDetailsMapFragment extends PopupMeasurementMapFragment {

    private MeasurementModel measurement;

    @Override
    protected void checkArgs() {
        if (getArguments() != null
         && getArguments().getSerializable(MeasurementDetailsListFragment.ARG_MEASUREMENT) != null) {
            measurement = (MeasurementModel) getArguments().
            getSerializable(MeasurementDetailsListFragment.ARG_MEASUREMENT);
        }
    }

    @Override
    protected void updateTitle() {
        String title = FileUtils.getExportItemName(getContext(),
        measurement.getId(), measurement.getDate());
        setTitle(title);
    }

    private void showFilterBtn(boolean show) {
        Fragment fragment = getParentFragment();
        if (fragment != null && fragment instanceof MeasurementDetailsFragment) {
            ((MeasurementDetailsFragment) fragment).showFilterBtn(show);
        }
    }

    @Override
    protected void initMapData() {
        MeasurementsDataHelper.getInstance().
        getData(measurement, true, new MeasurementsDataHelper.MeasurementsDataLoaderListener<List<MeasurementItem>>() {
            @Override
            public void onDataLoaded(List<MeasurementItem> items) {
                onItemsReady(items, true);
            }
        });
    }

    @Override
    public void refresh() {
        if (getMap() != null) {
            initMap(getMap());
        }
    }

    @Override
    public void showPopup(MeasurementItem item, boolean show) {
        super.showPopup(item, show);
        showFilterBtn(!show);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getMainActivity() != null && getMainActivity().getAddButton() != null) {
            getMainActivity().getAddButton().setVisibility(View.GONE);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        showFilterBtn(true);
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

