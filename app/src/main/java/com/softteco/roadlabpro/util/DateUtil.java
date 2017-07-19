package com.softteco.roadlabpro.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Utility class for work {@link java.util.Date}.
 */
public final class DateUtil {

    private DateUtil() {
        /**/
    }

    /**
     * Format pattern.
     */
    public enum Format {

        /**
         * Format like 26-02-2015.
         */
        DDMMYYY("dd-MM-yyyy"),

        /**
         * Format like 2010-05-24T00:00:00Z
         */
        SERVER_DATE("yyyy-MM-dd'T'HH:mm:ss'Z'"),

        /**
         * Format like 26.02.
         */
        DDMM("dd.MM"),

        /**
         * Format like 20130503002029.
         */
        YYYYMMDD_HHMMSS_S("yyyyMMddHHmmss"),
        /**
         * Format like 26 jan.
         */
        DDMMM("dd MMM"),

        /**
         * Format like 02.2015.
         */
        MMYYYY("MM.yyyy"),
        /**
         * Format like 26.02.2015 18:37.
         */
        DDMMYYYHHMM("dd.MM.yyyy HH:mm"),


        //07-May-2016 TimeIn24h
        DDMMMMYYYYHHMM("dd-MMMM-yyyy HH:mm"),

        /**
         * Format like 18:37 AM/PM.
         */
        HHMMA("HH:mm a"),

        /**
         * Format like 18:37.
         */
        HHMM("HH:mm"),

        /**
         * Format like 18:37:00.
         */
        HHMMSS("HH:mm:ss"),

        /**
         * Format like 02.
         */
        MM("MM"),

        /**
         * Format like 02.
         */

        DD("dd");

        private final String pattern;

        Format(final String pattern) {
            this.pattern = pattern;
        }

        public String getPattern() {
            return pattern;
        }
    }

    /**
     * The method is an object of type Date to the appropriate format by Format.DDMMYYY.
     *
     * @param date parameter type Date to represent it in the appropriate format by default
     * @return string in a specified format by default
     * @see DateUtil.Format
     */
    public static String format(final Date date) {
        return format(date, Format.DDMMYYY);
    }

    /**
     * The method is an object of type Date to the appropriate format.
     *
     * @param date   date are formatted
     * @param format {@link DateUtil.Format}
     * @return string in a specified format
     * @see DateUtil.Format
     */
    public static String format(final Date date, final Format format) {
        return date == null ? "" : new SimpleDateFormat(format.getPattern(),
                Locale.ENGLISH).format(date);
    }

    /**
     * The method converts an object of type String to an object of type Date.
     *
     * @param value date in string format
     * @return instance Date
     */
    public static Date parse(final String value) {
        try {
            final SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
            format.setTimeZone(TimeZone.getTimeZone("GMT"));
            return format.parse(value);
        } catch (ParseException e) {
            return null;
        }
    }
}
