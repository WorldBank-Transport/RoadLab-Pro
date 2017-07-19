package com.softteco.roadlabpro.util;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.softteco.roadlabpro.activity.HelpActivity;

/**
 * Created by ppp on 15.04.2015.
 */
public class AppUtil {

    public static int sdkVersion() {
        return android.os.Build.VERSION.SDK_INT;
    }

    public static void showTutorial(Context context) {
        showTutorial(context, true);
    }

    public static void showTutorial(Context context, boolean openMainScreen) {
        Intent intent = new Intent(context, HelpActivity.class);
        intent.putExtra(HelpActivity.OPEN_MAIN_SCREEN, openMainScreen);
        context.startActivity(intent);
    }
}
