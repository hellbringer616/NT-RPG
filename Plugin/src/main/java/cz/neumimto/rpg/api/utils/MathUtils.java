package cz.neumimto.rpg.api.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MathUtils {

    public static Pattern REGEXP_NUMBER = Pattern.compile("[-+]?\\d+([\\.,]\\d+)?");

    public static double getPercentage(double n, double total) {
        return (n / total) * 100;
    }

    public static boolean isMoreThanPercentage(double a, double b, double percentage) {
        return ((a / b) * 100 - 100) >= percentage;
    }

    public static double round(double value, int precision) {
        int scale = (int) Math.pow(10, precision);
        return (double) Math.round(value * scale) / scale;
    }

    public static double round(float value, int precision) {
        int scale = (int) Math.pow(10, precision);
        return Math.round(value * scale) / scale;
    }

    public static boolean isNumeric(String str) {
        return str.matches("-?\\d+(\\.\\d+)?");
    }

    public static String extractNumber(String string) {
        Matcher matcher = REGEXP_NUMBER.matcher(string);
        if (matcher.find()) {
            return matcher.group();
        }
        return null;
    }

}
