package com.softteco.roadlabpro.fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import com.softteco.roadlabpro.R;
import com.softteco.roadlabpro.RAApplication;
import com.softteco.roadlabpro.algorithm.DeviceStateDetector;
import com.softteco.roadlabpro.menu.ScreenItems;
import com.softteco.roadlabpro.sqlite.MeasurementsDataHelper;
import com.softteco.roadlabpro.sqlite.model.FolderModel;
import com.softteco.roadlabpro.sqlite.model.RoadModel;
import com.softteco.roadlabpro.util.Constants;

public abstract class BaseStartMeasurementFragment
    extends AbstractWBFragmentWithProjectTitle implements View.OnClickListener {

    protected static final String TAG = StartMeasurementFragment.class.getSimpleName();

    protected static final int REFRESH_INTERVAL = 2000;
    protected static final int REFRESH_GRAPH_INTERVAL = 100;
    protected static final float INTERVAL_LENGTH = 100f;
    protected static final int ROAD_ID_BAD = 0;
    protected static final int ROAD_ID_NORMAL = 1;
    protected static final int ROAD_ID_GOOD = 2;
    protected static final int ROAD_ID_PERFECT = 3;
    protected static final String FLOAT_FORMAT = "%.2f";
    protected static final String DASH_TEXT = "-";

    private AlertDialog alertToShow;

    protected void setControlVisibility(final View view, final boolean visible) {
        int visibility = View.GONE;
        if (visible) {
            visibility = View.VISIBLE;
        }
        if (view != null) {
            view.setVisibility(visibility);
        }
    }

    protected void hideInfoDialog() {
        if (alertToShow != null && alertToShow.isShowing()) {
            alertToShow.dismiss();
        }
    }

    protected void showInfoDialog(final int type) {
        hideInfoDialog();
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.info_dialog_view, null);
        final ImageView image = (ImageView) view.findViewById(R.id.dialogImage);
        final TextView text = (TextView) view.findViewById(R.id.dialogText);
        final Button okButton = (Button) view.findViewById(R.id.okButton);
        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideInfoDialog();
            }
        });
        String message = "";
        switch (type) {
            case Constants.DIALOG_TYPE_DEVICE_WARNING:
                message = getString(R.string.summary_bad_device_rotation);
                image.setImageResource(R.drawable.ic_device_dialog);
                break;
            case Constants.DIALOG_TYPE_GPS_WARNING:
                message = getString(R.string.summary_bad_gps_signal);
                image.setImageResource(R.drawable.ic_gps_dialog);
                break;
        }
        text.setText(message);
        alertToShow = new AlertDialog.Builder(getMainActivity())
                .setView(view).create();
        alertToShow.show();
    }

//    @Override
//    public void onResume() {
//        super.onResume();
//        updateTitle();
//    }

    protected void setDeviceIndicatorAngleBtn(final ImageView btn, final boolean isRecordEnabled, final boolean rightState, final int deviceState) {
        //Log.i(TAG, "setDeviceIndicatorAngleBtn: isRecordEnabled=" + isRecordEnabled);
        btn.setImageResource(0);
        if (!isRecordEnabled) {
            btn.setVisibility(View.GONE);
        } else if (deviceState == DeviceStateDetector.DEVICE_STATE_VERTICAL  && rightState) {
            btn.setVisibility(View.VISIBLE);
            btn.setImageResource(R.drawable.btn_device_bg_green_normal);
            btn.setOnClickListener(null);
        } else if (deviceState == DeviceStateDetector.DEVICE_STATE_HORIZONTAL && rightState) {
            btn.setVisibility(View.VISIBLE);
            btn.setImageResource(R.drawable.btn_device_bg_blue);
            btn.setOnClickListener(null);
        } else if (!rightState) {
            btn.setVisibility(View.VISIBLE);
            btn.setImageResource(R.drawable.btn_device_selector);
            btn.setOnClickListener(this);
        }
    }

    protected void setGpsIndicatorBtn(final ImageView btn, final boolean isRecordEnabled, final boolean rightState) {
        btn.setImageResource(0);
        if (rightState) {
            btn.setVisibility(View.VISIBLE);
            btn.setImageResource(R.drawable.btn_gps_bg_green_normal);
            btn.setOnClickListener(null);
        } else {
            btn.setVisibility(View.VISIBLE);
            btn.setImageResource(R.drawable.btn_gps_selector);
            btn.setOnClickListener(this);
        }
        if (!isRecordEnabled) {
            btn.setVisibility(View.GONE);
        }
    }

    @Override
    public int getMenuFragmentResources() {
        return -1;
    }

    @Override
    public int getTypeFragment() {
        return ScreenItems.SCREEN_MEASUREMENT;
    }

    @Override
    public boolean isHomeIndicatorMenu() {
        return true;
    }
}
