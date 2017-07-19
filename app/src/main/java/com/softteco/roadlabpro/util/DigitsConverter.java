package com.softteco.roadlabpro.util;

import android.util.Log;

public class DigitsConverter {
	
	public static String TAG = DigitsConverter.class.getSimpleName();
	
	public static double toDouble(String value) {
		try {
			return Double.valueOf(value);
		} catch (NumberFormatException e) {
			Log.e(TAG, e.getMessage());
			return 0;
		}
	}
	
	public static long toLong(String value) {
		try {
			return Long.valueOf(value);
		} catch (NumberFormatException e) {
			Log.e(TAG, e.getMessage());
			return 0;
		}
	}

	public static int toInteger(String value) {
		try {
			return Integer.valueOf(value);
		} catch (NumberFormatException e) {
			Log.e(TAG, e.getMessage());
			return 0;
		}
	}
	
	public static double toDouble(Double value) {
		return value == null ? 0 : value;
	}
	
	public static long toLong(Long value) {
		return value == null ? 0 : value;
	}

	public static int toInteger(Integer value) {
		return value == null ? 0 : value;
	}
	
	public static boolean toBoolean(Boolean value) {
		return value == null ? false : value;
	}


}
