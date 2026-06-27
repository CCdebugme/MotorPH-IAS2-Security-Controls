package com.group33.cp2.motorph.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

// Shared formatting and date helpers used across the app.
public class Utility {

    // private because DecimalFormat is mutable and shared across calls
    private static final DecimalFormat twoDecimalFormat = new DecimalFormat("#,##0.00");
    private static final DecimalFormat threeDecimalFormat = new DecimalFormat("0.000");
    private static final DecimalFormat phpFormat = new DecimalFormat("Php #,##0.00");

    // e.g. 1234.5 → "1,234.50"
    public static synchronized String formatTwoDecimal(double value) {
        return twoDecimalFormat.format(value);
    }

    // e.g. 1.2345 → "1.235"
    public static synchronized String formatThreeDecimal(double value) {
        return threeDecimalFormat.format(value);
    }

    // e.g. 1234.5 → "Php 1,234.50"
    public static synchronized String formatPhp(double value) {
        return phpFormat.format(value);
    }

    public static double round(double value, int places) {
        if (places < 0) {
            throw new IllegalArgumentException("Decimal places must be non-negative");
        }
        BigDecimal bigDecimal = BigDecimal.valueOf(value);
        bigDecimal = bigDecimal.setScale(places, RoundingMode.HALF_UP);
        return bigDecimal.doubleValue();
    }

    public static boolean isEndOfWeek(LocalDate date) {
        return date.getDayOfWeek() == DayOfWeek.FRIDAY;
    }

    public static DateTimeFormatter getDateFormatter(String pattern) {
        return DateTimeFormatter.ofPattern(pattern);
    }
}
