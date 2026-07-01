package com.group33.cp2.motorph.report;

/**
 * Thrown by ReportService when a report cannot be generated.
 * Wraps JasperReports and data-access errors into a single
 * checked exception so callers don't need to catch raw
 * JRException or SQLException.
 */
public class ReportException extends Exception {

    public ReportException(String message) {
        super(message);
    }

    public ReportException(String message, Throwable cause) {
        super(message, cause);
    }
}
