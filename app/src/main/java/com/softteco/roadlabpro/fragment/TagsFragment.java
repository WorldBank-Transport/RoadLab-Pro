package com.softteco.roadlabpro.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTabHost;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import com.softteco.roadlabpro.R;
import com.softteco.roadlabpro.RAApplication;
import com.softteco.roadlabpro.menu.ScreenItems;
import com.softteco.roadlabpro.sqlite.MeasurementsDataHelper;
import com.softteco.roadlabpro.sqlite.dao.FolderDAO;
import com.softteco.roadlabpro.sqlite.dao.RoadDAO;
import com.softteco.roadlabpro.ui.MeasurementsFilterDialog;
import com.softteco.roadlabpro.ui.SettingsDialog;

public class TagsFragment extends BaseTabFragment implements View.OnClickListener {

    public static final String TAG_FRAGMENT = "TAG_FRAGMENT";
    private final String TAG = TagsFragment.class.getName();

    private MeasurementsFilterDialog filtersDialog;
    private ImageButton btnFilter;

    public static TagsFragment newInstance(Bundle args) {
        TagsFragment fragment = new TagsFragment();
        if (args != null) {
            fragment.setArguments(args);
        }
        return fragment;
    }

    public TagsFragment() {
        /**/
    }

    protected void checkArgs() {
        if (getArguments() != null && getArguments().getSerializable(TAG_FRAGMENT) != null) {
//            measurementModel = (MeasurementModel) getArguments().getSerializable(TAG_FRAGMENT);
        }
    }

    @Override
    protected View createView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(getLayoutFragmentResources(), container, false);
        btnFilter = (ImageButton) view.findViewById(R.id.btn_filter);
        btnFilter.setVisibility(View.GONE);
//        btnFilter.setOnClickListener(this);
        return view;
    }

    protected FragmentTabHost getTabHost(View view) {
        FragmentTabHost mTabHost = (FragmentTabHost) view.findViewById(android.R.id.tabhost);
        mTabHost.setup(getActivity(), getChildFragmentManager(), R.id.tab_content);
        return mTabHost;
    }

    @Override
    protected void initTabs(FragmentTabHost mTabHost) {
//        Bundle measurementBundle = new Bundle();
//        measurementBundle.putSerializable(MeasurementDetailsListFragment.ARG_MEASUREMENT, measurementModel);
        mTabHost.addTab(mTabHost.newTabSpec(getResources().getString(R.string.tags_list_tab_name))
           .setIndicator(getResources().getString(R.string.tags_list_tab_name)), TagsListFragment.class, null);
        mTabHost.addTab(mTabHost.newTabSpec(getResources().getString(R.string.tags_map_tab_name))
           .setIndicator(getResources().getString(R.string.tags_map_tab_name)), TagsMapFragment.class, null);
        mTabHost.setCurrentTab(0);
    }

    protected void openFilterDialog() {
        if (filtersDialog == null) {
            filtersDialog = new MeasurementsFilterDialog(getActivity(), new SettingsDialog.SettingsDialogListener() {
                @Override
                public void onRequestClose(int type) {
                    refreshCurrentScreen();
                }
            });
        }
        filtersDialog.show();
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.btn_filter:
//                openFilterDialog();
                break;
        }
    }

    private void refreshCurrentScreen() {
        final Fragment fragment = getChildFragmentManager().findFragmentById(R.id.tab_content);
        if (fragment != null && fragment instanceof AbstractWBFragment) {
            ((AbstractWBFragment) fragment).refresh();
        }
    }

    @Override
    public int getLayoutFragmentResources() {
        return R.layout.fragment_tabs;
    }

    @Override
    public int getTypeFragment() {
        return ScreenItems.SCREEN_TAGS;
    }

    @Override
    public boolean isHomeIndicatorMenu() {
        return false;
    }

}
