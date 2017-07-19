package com.softteco.roadlabpro.util;

import android.content.Context;

import com.softteco.roadlabpro.R;

/**
 * Created by Vadim Alenin on 15.04.2015.
 */
public class StringUtil {

    private static int MAX_COLOR_SYMBOLS = 6;
    private static String SHARP_SYMBOL = "#";
    private final static String REGEX_NEW_LINE_CHARACTER = "[\\t\\n\\r]+";

    public static String getStringFromCoords(double[] coords) {
        StringBuilder builder = new StringBuilder();
        builder.append(coords[0]);
        builder.append(";");
        builder.append(coords[1]);
        builder.append(";");
        builder.append(coords[2]);
        return builder.toString();
    }

    public static String setTextHtmlColor(Context context, int textColor, String str) {
        String color = Integer.toHexString(textColor);
        if (color.length() > MAX_COLOR_SYMBOLS) {
            color = color.substring(color.length() - MAX_COLOR_SYMBOLS, color.length());
        }
        color = SHARP_SYMBOL + color;
        return context.getString(R.string.html_text_color_str, color, str);
    }

    public static String replaceAllNewLineCharacter(final String str) {
        final String result = str.replaceAll(REGEX_NEW_LINE_CHARACTER, " ");
        return result;
    }

}
