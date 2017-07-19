package com.softteco.roadlabpro.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTabHost;

import com.softteco.roadlabpro.R;
import com.softteco.roadlabpro.menu.ScreenItems;
import com.softteco.roadlabpro.sqlite.model.RoadModel;

/**
 * Created by bogdan on 08.04.2016.
 */
public class MeasurementsFragment extends BaseTabFragment {

    public static final String ARG_MEASUREMENTS_MODEL = "ARG_MEASUREMENTS_MODEL";
    private final String TAG = MeasurementsFragment.class.getName();

    private RoadModel roadModel;

    public static MeasurementsFragment newInstance(Bundle args) {
        MeasurementsFragment fragment = new MeasurementsFragment();
        if (args != null) {
            fragment.setArguments(args);
        }
        return fragment;
    }

    public MeasurementsFragment() {
        /**/
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        checkArgs();
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    protected void initTabs(FragmentTabHost mTabHost) {
        Bundle measurementListBundle = new Bundle();
        measurementListBundle.putSerializable(MeasurementListFragment.ARG_MEASUREMENT_LIST, roadModel);
        mTabHost.addTab(mTabHost.newTabSpec(getResources().getString(R.string.road_list_tab_name))
            .setIndicator(getResources().getString(R.string.road_list_tab_name)), MeasurementListFragment.class, measurementListBundle);
        mTabHost.addTab(mTabHost.newTabSpec(getResources().getString(R.string.road_map_tab_name))
            .setIndicator(getResources().getString(R.string.road_map_tab_name)),MeasurementMapFragment.class, measurementListBundle);
        mTabHost.addTab(mTabHost.newTabSpec(getResources().getString(R.string.road_tags_tab_name))
            .setIndicator(getResources().getString(R.string.road_tags_tab_name)),
            TagsListFragment.class, measurementListBundle);
        mTabHost.setCurrentTab(0);
    }

    @Override
    protected void checkArgs() {
        if (getArguments() != null && getArguments().getSerializable(ARG_MEASUREMENTS_MODEL) != null) {
            roadModel = (RoadModel) getArguments().getSerializable(ARG_MEASUREMENTS_MODEL);
        }
    }

    @Override
    protected void updateTitle() {
        setTitle(roadModel.getName());
    }

    @Override
    public int getLayoutFragmentResources() {
        return R.layout.fragment_road;
    }


    @Override
    public int getMenuFragmentResources() {
        return -1;
    }

    @Override
    public int getTypeFragment() {
        Fragment fragment = getCurrentFragment();
        if (fragment != null && fragment instanceof AbstractWBFragment) {
            return ((AbstractWBFragment) fragment).getTypeFragment();
        }
        return ScreenItems.SCREEN_MEASUREMENT;
    }

    @Override
    public boolean isHomeIndicatorMenu() {
        return false;
    }

}
