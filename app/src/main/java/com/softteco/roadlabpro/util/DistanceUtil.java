package com.softteco.roadlabpro.util;

import android.content.Context;
import android.location.Location;

import com.softteco.roadlabpro.R;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class DistanceUtil {
    private static final int COEF_E3 = 1000;
    private static final int LIMIT = 999;

    private DistanceUtil() {
    }

    public static double calculateDistance(double startLatitude, double startLongitude, double endLatitude, double endLongitude) {
        float[] results = new float[1];
        results[0] = 0;
        Location.distanceBetween(startLatitude, startLongitude, endLatitude, endLongitude, results);
        return Math.abs(results[0]);
    }

    public static String getDistanceString(Context context, double distance) {
        double km = getKilometers(distance);
        String kmStr = "km";
        String mStr = "m";
        if (context != null) {
            kmStr = context.getString(R.string.kilometers_str);
            mStr = context.getString(R.string.meters_str);
        }
        if (km > LIMIT) {
            return ">999" + kmStr;
        }
        int m = getMeters(distance);
        return km >= 1 ?
               km > 100 ? (int) round(km, 0) + " " + kmStr :
                        km + " " + kmStr:
               m + " " + mStr;
    }

    private static double getKilometers(double distance) {
        return round((distance / COEF_E3), 1);
    }

    public static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();
        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    private static int getMeters(double distance) {
        return (int) ((Math.abs((distance / COEF_E3) - (int) getKilometers(distance))) * COEF_E3);
    }

}
