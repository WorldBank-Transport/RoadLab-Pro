package com.softteco.roadlabpro.util;

import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.graphics.Typeface;
import android.widget.TextView;
/**
 * Util class for fonts
 */
public class FontsStorage {

    private static Map<String, Typeface> storage = new HashMap<String, Typeface>();
    
    public static Typeface getFont(Context context, String fullName) {
        if (storage == null) {
            storage = new HashMap<String, Typeface>();
        }
        synchronized (storage) {
            Typeface font = storage.get(fullName);
            if (font == null) {
                font = Typeface.createFromAsset(context.getAssets(), fullName);
                if (font != null) {
                    storage.put(fullName, font);
                    return font;
                }
            } else {
                return font;
            }
        }
        return Typeface.DEFAULT;
    }
    
    public static String getFontNameByResId(Context context, int resId) {
    	if (context != null) {
    		return context.getString(resId);
    	}
    	return null;
    }
    
    public static void applyFont(Context context, String fullName, TextView text) {
        Typeface tf = getFont(context, fullName);
        if (tf != null && text != null) {
            text.setTypeface(tf);
        }
    }
    
    public static void clear() {
        if (storage != null) {
            storage.clear();
        }
    }
}
