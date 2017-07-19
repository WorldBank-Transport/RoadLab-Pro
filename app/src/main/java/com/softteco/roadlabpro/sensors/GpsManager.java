package com.softteco.roadlabpro.sensors;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.provider.Settings;

import com.softteco.roadlabpro.R;
import com.softteco.roadlabpro.ui.CommonDialogs;
import com.softteco.roadlabpro.util.AppUtil;

public final class GpsManager {

    public static final int GPS_ENABLED_CODE = 1111;
    public static final int GPS_ENABLED_CODE_AUTO = 2222;
    private Boolean gpsAvailable;

    public boolean isGpsAvailable(final Context context) {
        if (gpsAvailable == null) {
            final PackageManager packageManager = context.getPackageManager();
            gpsAvailable = packageManager.hasSystemFeature(PackageManager.FEATURE_LOCATION_GPS);
        }
        return gpsAvailable;
    }

    private void changeGpsStatus(final Context context, final boolean status) {
        if (AppUtil.sdkVersion() < 19 && isGpsAvailable(context)) {
            Intent intent = new Intent("android.location.GPS_ENABLED_CHANGE");
            intent.putExtra("enabled", status);
            context.sendBroadcast(intent);
        }
    }

    public void enableGPS(final Context context) {
        changeGpsStatus(context, true);
    }

    public void disableGPS(final Context context) {
        changeGpsStatus(context, false);
    }

    public boolean dispatchProviders(final Activity activity, final int code) {
        return checkGPSStatus(activity, code);
    }

    public boolean dispatchProviders(final Activity activity) {
        return checkGPSStatus(activity, GPS_ENABLED_CODE);
    }

    private Dialog buildAlertMessageNoGps(final Activity activity, final int code) {
        return CommonDialogs.getCommonStructure(activity, R.string.allow_gps_text, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                activity.startActivityForResult(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS), code);
                dialog.dismiss();
            }
        });
    }

    private boolean checkGPSStatus(final Activity activity, final int code) {
        final LocationManager locationManager = (LocationManager) activity.getSystemService(Context.LOCATION_SERVICE);
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        /*&& !locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)*/) {
            final Dialog dialog = buildAlertMessageNoGps(activity, code);
            dialog.show();
            return true;
        }
        return false;
    }
}
