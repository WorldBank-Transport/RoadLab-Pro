package com.softteco.roadlabpro.util;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * Created by Aleksey on 04.05.2015.
 */
public class ConnectivityUtils {

    private ConnectivityInterface connectListener;
    private ConnectivityReceiver connectReceiver;

    public ConnectivityUtils(ConnectivityInterface connectListener) {
        this.connectListener = connectListener;
    }

    public ConnectivityReceiver getConnectReceiver() {
        if (connectReceiver == null) {
            connectReceiver = new ConnectivityReceiver();
        }
        return connectReceiver;
    }

    public static boolean isWifiConnected(Context context) {
        NetworkInfo connection  = getConnectedNetworkInfo(context);
        return connection != null && connection.getTypeName().equalsIgnoreCase("WIFI");
    }

    public static boolean isNetworkConnected(Context context) {
        return getConnectedNetworkInfo(context) != null;
    }

    public static NetworkInfo getConnectedNetworkInfo(Context context) {
        NetworkInfo ifConnected = null;
        try {
            ConnectivityManager connectivityManager = (ConnectivityManager) context.getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo[] info = connectivityManager.getAllNetworkInfo();

            for (int i = 0; i < info.length; i++) {
                NetworkInfo connection = info[i];
                if (connection.getState().equals(NetworkInfo.State.CONNECTED)) {
                    ifConnected = connection;
                    break;
                }
            }
        } catch (Exception ex) {
            return null;
        }
        return ifConnected;
    }

    public class ConnectivityReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (connectListener != null && isNetworkConnected(context)) {
                connectListener.networkWasConnected();
            }
        }
    }

    public interface ConnectivityInterface {
        void networkWasConnected();
    }
}
