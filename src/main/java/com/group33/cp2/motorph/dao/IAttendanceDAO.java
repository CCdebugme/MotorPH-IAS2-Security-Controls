package com.group33.cp2.motorph.dao;

import com.group33.cp2.motorph.model.Attendance;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

/**
 * Contract for all attendance data access operations.
 *
 * Original implementation notes (from AttendanceDAO):
 * - Owns read/write access to the attendance table.
 * - No CSV access remains — all data comes from MySQL via JDBC.
 * - Date values are stored as VARCHAR in MM/dd/yyyy format in the DB.
 * - getTimeLogs() is overloaded:
 *   Overload 1 — returns all logs for an employee (no date filter).
 *   Overload 2 — returns logs filtered to a LocalDate range.
 * - clockIn() throws SQLException if the employee already has an entry today.
 * - clockOut() throws SQLException if no active clock-in is found.
 */
public interface IAttendanceDAO {

    /** Returns all attendance records from the database. */
    List<Attendance> loadAllAttendance() throws SQLException;

    /** Records a clock-in event for the given employee using the current time. */
    void clockIn(String empId) throws SQLException;

    /** Records a clock-out event for the given employee using the current time. */
    void clockOut(String empId) throws SQLException;

    /**
     * Returns all time log rows for the given employee.
     * Overload 1 — no date filter applied.
     */
    List<String[]> getTimeLogs(String empId) throws SQLException;

    /**
     * Returns time log rows for the given employee within a date range.
     * Overload 2 — filters records between start and end dates inclusive.
     */
    List<String[]> getTimeLogs(String empId, LocalDate start, LocalDate end) throws SQLException;
}
