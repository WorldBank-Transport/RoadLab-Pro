package com.softteco.roadlabpro.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

/**
 * Created by ppp on 08.04.2015.
 */
public class TimeUtil {

    public static String DATE_TIMESTAMP = "HH:mm:ss yyyy-MMMM-dd";
    public static String DATE_FILENAME_FORMAT = "yyyyMMddHHmmSSS";
    public static String DATE_SIMPLE_FILENAME_FORMAT = "dd-MMMM-yyyy HH-mm-ss";
    public static String DATE_DROPBOX_FOLDER_NAME_FORMAT = "yyyyMMMdd_HHmma";
    public static final long NANOSECONDS_VALUE = 1000000L;

    public static String formatMillis(long timeInMillis) {
        String sign = "";
        if (timeInMillis < 0) {
            sign = "-";
            timeInMillis = Math.abs(timeInMillis);
        }
        long[] time = getSeparatedTime(timeInMillis);
        final StringBuilder formatted = new StringBuilder(20);
        formatted.append(sign);
        formatted.append(String.format("%02d", time[3], Locale.US));
        formatted.append(String.format(":%02d", time[2], Locale.US));
        formatted.append(String.format(":%02d", time[1], Locale.US));
        //formatted.append(String.format(".%03d", time[0]));
        return formatted.toString();
    }

    private static long[] getSeparatedTime(long timeInMillis) {
        long hours = timeInMillis / TimeUnit.HOURS.toMillis(1);
        long minutes = timeInMillis / TimeUnit.MINUTES.toMillis(1);
        long seconds = timeInMillis % TimeUnit.MINUTES.toMillis(1) / TimeUnit.SECONDS.toMillis(1);
        long millis = timeInMillis % TimeUnit.SECONDS.toMillis(1);
        long[] sepratedTime = new long[4];
        sepratedTime[0] = millis;
        sepratedTime[1] = seconds;
        sepratedTime[2] = minutes;
        sepratedTime[3] = hours;
        return sepratedTime;
    }

    public static String formatMillisMinSecFmt(long timeInMillis) {
        long[] time = getSeparatedTime(timeInMillis);
        return String.format("%2d:%02d", time[2], time[1], Locale.US);
    }

    public static long getUnixTimestamp() {
        return System.currentTimeMillis() / 1000L;
    }

    public static long getSystemTimeMillisFromUnixTimestamp(long timestamp) {
        return timestamp * 1000;
    }

    public static long getCurrentTimeMillis() {
        return (long) ((double) System.nanoTime() / (double) NANOSECONDS_VALUE);
    }


    public static String getFormattedDate(final String template) {
        Date date = new Date();
        return new SimpleDateFormat(template).format(date);
    }
}
