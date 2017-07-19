package com.softteco.roadlabpro.util;

public class ValidateUtil {

    public static boolean isNull(Object obj) {
        return obj == null;
    }

    public static boolean isValidNumber(final double value) {
        return !(Double.isInfinite(value) || Double.isNaN(value));
    }

    public static boolean isValidMoreThanNumber(final double number, final double value) {
        return number >= value;
    }

    public static boolean isValidNotEqualsNumber(final double number, final double value) {
        return number != value;
    }
}