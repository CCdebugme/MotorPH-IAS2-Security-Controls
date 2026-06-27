package com.group33.cp2.motorph.util;

// Centralized utility for validating Philippine government ID number formats.
// All methods do a null check and regex match; return true only on exact format match.
public class ValidationUtil {

    private ValidationUtil() {
        // Utility class — no instances
    }

    // Validates SSS number format: XX-XXXXXXX-X (e.g., 44-4506057-3).
    public static boolean isValidSSS(String sss) {
        return sss != null && sss.matches("\\d{2}-\\d{7}-\\d{1}");
    }

    // Validates TIN number format: XXX-XXX-XXX-XXX (e.g., 442-605-657-000).
    public static boolean isValidTIN(String tin) {
        return tin != null && tin.matches("\\d{3}-\\d{3}-\\d{3}-\\d{3}");
    }

    // Validates PhilHealth number as exactly 12 digits (e.g., 820126853951).
    public static boolean isValidPhilHealth(String ph) {
        return ph != null && ph.matches("\\d{12}");
    }

    // Validates Pag-IBIG number as exactly 12 digits (e.g., 691295330870).
    public static boolean isValidPagIBIG(String pag) {
        return pag != null && pag.matches("\\d{12}");
    }

    // Validates phone number format: XXX-XXX-XXXX (e.g., 966-860-2700).
    public static boolean isValidPhoneNumber(String phone) {
        return phone != null && phone.matches("\\d{3}-\\d{3}-\\d{4}");
    }
}
