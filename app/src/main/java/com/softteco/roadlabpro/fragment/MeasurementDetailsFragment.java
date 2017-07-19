package com.softteco.roadlabpro.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTabHost;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import com.softteco.roadlabpro.R;
import com.softteco.roadlabpro.menu.ScreenItems;
import com.softteco.roadlabpro.sqlite.model.MeasurementModel;
import com.softteco.roadlabpro.ui.MeasurementsFilterDialog;
import com.softteco.roadlabpro.ui.SettingsDialog;
import com.softteco.roadlabpro.util.DateUtil;
import com.softteco.roadlabpro.util.FileUtils;

import java.util.Date;

public class MeasurementDetailsFragment extends BaseTabFragment implements View.OnClickListener {

    public static final String ARG_MEASUREMENT_MODEL = "ARG_MEASUREMENT_MODEL";
    private final String TAG = MeasurementDetailsFragment.class.getName();

    private MeasurementModel measurementModel;
    private MeasurementsFilterDialog filtersDialog;
    private ImageButton btnFilter;

    public static MeasurementDetailsFragment newInstance(Bundle args) {
        MeasurementDetailsFragment fragment = new MeasurementDetailsFragment();
        if (args != null) {
            fragment.setArguments(args);
        }
        return fragment;
    }

    public MeasurementDetailsFragment() {
        /**/
    }

    protected void checkArgs() {
        if (getArguments() != null && getArguments().getSerializable(ARG_MEASUREMENT_MODEL) != null) {
            measurementModel = (MeasurementModel) getArguments().getSerializable(ARG_MEASUREMENT_MODEL);
        }
    }

    @Override
    protected View createView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(getLayoutFragmentResources(), container, false);
        btnFilter = (ImageButton) view.findViewById(R.id.btn_filter);
        btnFilter.setOnClickListener(this);
        return view;
    }

    public void showFilterBtn(boolean show) {
        if (btnFilter != null) {
            btnFilter.setVisibility(show ? View.VISIBLE : View.GONE);
        }
    }

    protected FragmentTabHost getTabHost(View view) {
        FragmentTabHost mTabHost = (FragmentTabHost) view.findViewById(android.R.id.tabhost);
        mTabHost.setup(getActivity(), getChildFragmentManager(), R.id.tab_content);
        return mTabHost;
    }

    @Override
    protected void initTabs(FragmentTabHost mTabHost) {
        Bundle measurementBundle = new Bundle();
        measurementBundle.putSerializable(MeasurementDetailsListFragment.ARG_MEASUREMENT, measurementModel);
        mTabHost.addTab(mTabHost.newTabSpec(getResources().getString(R.string.road_list_tab_name))
           .setIndicator(getResources().getString(R.string.road_list_tab_name)), MeasurementDetailsListFragment.class, measurementBundle);
        mTabHost.addTab(mTabHost.newTabSpec(getResources().getString(R.string.road_map_tab_name))
           .setIndicator(getResources().getString(R.string.road_map_tab_name)), MeasurementDetailsMapFragment.class, measurementBundle);
        mTabHost.setCurrentTab(0);
    }

    @Override
    protected void updateTitle() {
        String title = FileUtils.getExportItemName(getContext(),
        measurementModel.getId(), measurementModel.getDate());
        setTitle(title);
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
                openFilterDialog();
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
        return ScreenItems.SCREEN_MEASUREMENT_DETAILS;
    }

    @Override
    public boolean isHomeIndicatorMenu() {
        return false;
    }
}
