package com.softteco.roadlabpro.util;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

import java.util.UUID;

/**
 * Created by Aleksey on 08.04.2015.
 */
public class DeviceUtil {

    public static String findDeviceID(final Context context) {
        String deviceID = PreferencesUtil.getInstance(context).getDeviceId();
        if (TextUtils.isEmpty(deviceID)) {
            deviceID = getDeviceId(context);
            PreferencesUtil.getInstance(context).setDeviceId(deviceID);
        }
        return deviceID;
    }

    public static String getDeviceId(Context context) {
        String deviceID = null;
        // MAC
        WifiManager wifi = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        WifiInfo info;
        if (wifi != null && (info = wifi.getConnectionInfo()) != null) {
            deviceID = info.getMacAddress();
        }
        // TelephonyManager.DeviceId
        String tmSerial = "null";
        if (TextUtils.isEmpty(deviceID)) {
            final TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            if (tm != null) {
                tmSerial = "" + tm.getSimSerialNumber();
                deviceID = tm.getDeviceId();
            }
        }
        // Settings.Secure.ANDROID_ID + TelephonyManager.SimSerialNumber
        if (TextUtils.isEmpty(deviceID)) {
            deviceID = generateId(context, tmSerial);
        }
        return deviceID;
    }

    public static String getUID(Context context) {
        String deviceID = null;
        final TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        if (tm != null) {
            deviceID = tm.getDeviceId();
        }
        return deviceID;
    }

    private static String generateId(Context context, String tmSerial) {
        final String androidId = "" + Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
        UUID deviceUuid = new UUID(androidId.hashCode(), (long) tmSerial.hashCode());
        return deviceUuid.toString();
    }

    public static String findDeviceName(Context context) {
        String deviceName = PreferencesUtil.getInstance(context).getDeviceName();
        if (TextUtils.isEmpty(deviceName)) {
            deviceName = getDeviceName();
        }
        deviceName = capitalize(deviceName);
        PreferencesUtil.getInstance(context).setDeviceName(deviceName);
        return deviceName;
    }

    public static String getDeviceName() {
        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;
        if (model.startsWith(manufacturer)) {
            return capitalize(model);
        } else {
            return capitalize(manufacturer) + "_" + model;
        }
    }


    private static String capitalize(String name) {
        if (TextUtils.isEmpty(name)) {
            return "";
        }
        name = name.replaceAll(" ", "_");
        char first = name.charAt(0);
        if (Character.isUpperCase(first)) {
            return name;
        } else {
            return Character.toUpperCase(first) + name.substring(1);
        }
    }

}
