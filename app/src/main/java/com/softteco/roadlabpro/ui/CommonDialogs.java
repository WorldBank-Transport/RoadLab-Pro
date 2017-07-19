package com.softteco.roadlabpro.ui;

import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.provider.Settings;

import com.softteco.roadlabpro.R;

public final class CommonDialogs {

    private CommonDialogs() {
    }

    public interface OnOkListener {
        void onDlgOkPressed();
    }

    public static Dialog buildAlertMessageDlg(final Activity context, String msgStr, final OnOkListener listener) {
        return getCommonStructure(context, msgStr, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                if (listener != null) {
                    listener.onDlgOkPressed();
                }
            }
        });
    }

    public static Dialog buildAlertMessageNoGps(final Activity context, final int gpsEnabled) {
        return getCommonStructure(context, R.string.allow_gps_text, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                context.startActivityForResult(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS), gpsEnabled);
                dialog.dismiss();
            }
        });
    }

    public static Dialog getCommonStructure(final Context context, String messageStr,
        DialogInterface.OnClickListener positiveListener) {
        Builder builder = new Builder(context);
        builder.setMessage(messageStr);
        builder.setPositiveButton(R.string.yes_button_txt, positiveListener)
            .setNegativeButton(R.string.no_button_txt, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
        return builder.create();
    }

    public static Dialog getCommonStructure(final Context context, int messageId,
        DialogInterface.OnClickListener positiveListener) {
        String messageStr = context.getString(messageId);
        return getCommonStructure(context, messageStr, positiveListener);
    }
}
