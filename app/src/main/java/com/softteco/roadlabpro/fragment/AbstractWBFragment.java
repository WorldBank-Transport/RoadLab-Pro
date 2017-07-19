package com.softteco.roadlabpro.fragment;


import android.app.ProgressDialog;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Fragment;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;

import com.softteco.roadlabpro.RAApplication;
import com.softteco.roadlabpro.sensors.GPSDetector;
import com.softteco.roadlabpro.sensors.IntervalsRecordHelper;
import com.softteco.roadlabpro.sync.SyncDataManager;
import com.softteco.roadlabpro.util.PreferencesUtil;

/**
 * A simple {@link Fragment} subclass.
 */
public abstract class AbstractWBFragment extends AbstractFragment {

    protected ProgressDialog dialog;

    public AbstractWBFragment() {
        // Required empty public constructor
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        showRecordControls(false);
    }

    protected void setTitle(String title) {
        if (getMainActivity() != null) {
            getMainActivity().updateActionBarTitle(title);
        }
    }

    protected void updateTitle() {
    }

    @Override
    public void onResume() {
        super.onResume();
        updateTitle();
    }

    public void replaceFragment(android.support.v4.app.Fragment fragment, boolean addToBackStack) {
        if (getMainActivity() != null) {
            getMainActivity().replaceFragment(fragment, addToBackStack);
        }
    }

    public void showRecordControls(boolean show) {
        if (getMainActivity() != null) {
            getMainActivity().showRecordControls(show);
        }
    }

    public IntervalsRecordHelper getIntervalsRecordHelper() {
        if (getMainActivity() != null) {
            return getMainActivity().getIntervalsRecordHelper();
        }
        return null;
    }

    public GPSDetector getGpsDetector() {
        if (RAApplication.getInstance() != null) {
            return RAApplication.getInstance().getGpsDetector();
        }
        return null;
    }

    public int getDeviceState() {
        if (getIntervalsRecordHelper() != null) {
            return getIntervalsRecordHelper().getDeviceState();
        }
        return -1;
    }

    public boolean isAutoMode() {
        return PreferencesUtil.getInstance(
                RAApplication.getInstance()).autoRoughnessDetectionEnabled();
    }

    public boolean isRecordEnabled() {
        boolean isRecordEnabled = getIntervalsRecordHelper() != null
                && getIntervalsRecordHelper().isRecordEnabled();
        //Log.i(TAG, "isRecordEnabled(): " + isRecordEnabled);
        return isRecordEnabled;
    }

    public void updateActionBarTitle(CharSequence title) {
        if (getMainActivity() != null) {
            getMainActivity().updateActionBarTitle(title);
        }
    }

    public Resources getAppResouces() {
        return RAApplication.getInstance().getResources();
    }

    public void refresh() {
    }

    protected void showProgress(final boolean show) {
        if (dialog == null) {
            dialog = new ProgressDialog(getContext());
            dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            dialog.setCancelable(false);
            dialog.setMessage("Uploading");
            dialog.show();
        }
        if (show) {
            if (!dialog.isShowing()) {
                dialog.show();
            }
        } else if (dialog.isShowing()) {
            dialog.dismiss();
        }
    }

    public void initSyncDataManager() {
        if (getMainActivity() != null) {
            getMainActivity().initSyncDataManager();
        }
    }

    public SyncDataManager getDataSyncManager() {
        if (getMainActivity() != null) {
            return getMainActivity().getSyncDataManager();
        }
        return new SyncDataManager(getActivity());
    }
}
