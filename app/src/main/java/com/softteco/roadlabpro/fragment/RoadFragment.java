package com.softteco.roadlabpro.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTabHost;

import com.softteco.roadlabpro.R;
import com.softteco.roadlabpro.menu.ScreenItems;
import com.softteco.roadlabpro.sqlite.dao.RoadDAO;
import com.softteco.roadlabpro.sqlite.model.FolderModel;

/**
 * MyIssueListFragment is an extends of {@link android.support.v4.app.ListFragment}.
 */
public class RoadFragment extends BaseTabFragment {

    public static final String ARG_ROADS_MODEL = "ARG_ROADS_MODEL";
    private final String TAG = RoadFragment.class.getName();

    private RoadDAO roadDAO;
    private FolderModel folder;

    public static RoadFragment newInstance(Bundle args) {
        RoadFragment fragment = new RoadFragment();
        if (args != null) {
            fragment.setArguments(args);
        }
        return fragment;
    }

    public RoadFragment() {
        /**/
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        checkArgs();
        roadDAO = new RoadDAO(getActivity());
    }

    @Override
    protected void checkArgs() {
        if (getArguments() != null && getArguments().getSerializable(ARG_ROADS_MODEL) != null) {
            folder = (FolderModel) getArguments().getSerializable(ARG_ROADS_MODEL);
        }
    }

    @Override
    protected void initTabs(FragmentTabHost mTabHost) {
        Bundle roadListBundle = new Bundle();
        roadListBundle.putSerializable(RoadListFragment.ARG_ROAD_LIST, folder);
        mTabHost.addTab(mTabHost.newTabSpec(getResources().getString(R.string.road_list_tab_name))
                .setIndicator(getResources().getString(R.string.road_list_tab_name)), RoadListFragment.class, roadListBundle);
        mTabHost.addTab(mTabHost.newTabSpec(getResources().getString(R.string.road_map_tab_name))
                .setIndicator(getResources().getString(R.string.road_map_tab_name)),RoadMapFragment.class, roadListBundle);
        mTabHost.addTab(mTabHost.newTabSpec(getResources().getString(R.string.road_summary_tab_name))
                .setIndicator(getResources().getString(R.string.road_summary_tab_name)),RoadSummaryFragment.class, roadListBundle);
        mTabHost.setCurrentTab(0);
    }

    @Override
    protected void updateTitle() {
        setTitle(folder.getName());
    }

    @Override
    public int getLayoutFragmentResources() {
        return R.layout.fragment_road;
    }

    private void refreshRoads() {
        final Fragment fragment = getCurrentFragment();
        if (fragment instanceof RoadListFragment) {
            RoadListFragment roadListFragment = ((RoadListFragment) fragment);
            roadListFragment.refreshRoads();
        }
    }

    @Override
    public void refresh() {
        refreshRoads();
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

    public FolderModel getFolder() {
        return folder;
    }
}
