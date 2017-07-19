package com.softteco.roadlabpro.util;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Created by ppp on 07.04.2015.
 */
public class ActivityUtil {

    public static String getVersionApplication(Context context) {
        try {
            return context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            return "";
        }
    }

    public static boolean isActivityRunning(final Context context,
                                            final String activityClassName) {

        final ActivityManager activityManager = (ActivityManager) context
                .getSystemService(Context.ACTIVITY_SERVICE);
        final List<ActivityManager.RunningTaskInfo> activities = activityManager
                .getRunningTasks(Integer.MAX_VALUE);
        for (int i = 0; i < activities.size(); i++) {
            if (activities.get(i).topActivity.getClassName().equals(
                    activityClassName)) {
                return true;
            }
        }
        return false;
    }

    public static void scheduleOnMainThread(Runnable r) {
        new Handler(Looper.getMainLooper()).post(r);
    }

    public static void scheduleOnMainThread(Runnable r, long delay) {
        new Handler(Looper.getMainLooper()).postDelayed(r, delay);
    }

    public static void runOnMainThread(Runnable r) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            r.run();
        }
        else {
            scheduleOnMainThread(r);
        }
    }

    public static String dumpIntent(Intent i) {
        if (i == null) {
            return "";
        }
        StringBuilder s = new StringBuilder();
        Bundle bundle = i.getExtras();
        if (bundle != null) {
            Set<String> keys = bundle.keySet();
            Iterator<String> it = keys.iterator();
            while (it.hasNext()) {
                String key = it.next();
                s.append("[" + key + "= " + bundle.get(key)+"]").append("\n");
            }
        }
        return s.toString();
    }
}
