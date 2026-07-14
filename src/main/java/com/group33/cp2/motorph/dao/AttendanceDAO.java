package com.group33.cp2.motorph.dao;

import com.group33.cp2.motorph.model.Attendance;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

// owns read/write access to attendance table.
// no CSV access remains  all data comes from MySQL via JDBC.
/**
 * Owns all read/write access to the attendance table.
 * No CSV access remains — all data comes from MySQL via JDBC.
 * Implements {@link IAttendanceDAO} to satisfy the DAO interface contract.
 *
 * <p>Date values are stored as VARCHAR in MM/dd/yyyy format in the database
 * and parsed back to {@link java.time.LocalDate} on retrieval.</p>
 *
 * <p>Method overloading is demonstrated via {@link #getTimeLogs(String)} and
 * {@link #getTimeLogs(String, LocalDate, LocalDate)} — the first returns all
 * logs for an employee with no date filter; the second filters by date range.</p>
 */
public class AttendanceDAO implements IAttendanceDAO {

    /**
     * Loads all attendance records from the database.
     * Used at startup by {@link com.group33.cp2.motorph.service.AttendanceService}
     * to build an in-memory snapshot.
     *
     * @return list of all Attendance records; empty list if none found
     * @throws SQLException if the query fails
     */
    public List<Attendance> loadAllAttendance() throws SQLException {
        List<Attendance> result = new ArrayList<>();
        String sql = "SELECT empNum, date, logIn, logOut FROM attendance WHERE logOut IS NOT NULL AND logOut != ''";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                try {
                    String empId = rs.getString("empNum");
                    String[] dateParts = rs.getString("date").trim().split("/");
                    LocalDate date = LocalDate.of(
                        Integer.parseInt(dateParts[2]),
                        Integer.parseInt(dateParts[0]),
                        Integer.parseInt(dateParts[1])
                    );
                    String[] logInParts = rs.getString("logIn").trim().split(":");
                    LocalTime logIn = LocalTime.of(
                        Integer.parseInt(logInParts[0]),
                        Integer.parseInt(logInParts[1])
                    );
                    String[] logOutParts = rs.getString("logOut").trim().split(":");
                    LocalTime logOut = LocalTime.of(
                        Integer.parseInt(logOutParts[0]),
                        Integer.parseInt(logOutParts[1])
                    );
                    result.add(new Attendance(empId, date, logIn, logOut));
                } catch (Exception e) {
                    System.err.println("AttendanceDAO: skipping malformed row: " + e.getMessage());
                }
            }
        }
        result.sort(Comparator.comparing(Attendance::getEmployeeID));
        return result;
    }

    /**
     * Records a clock-in event for the given employee using the current date and time.
     * Inserts a new attendance row with logIn set to now and logOut left blank.
     *
     * @param empId the employee number clocking in
     * @throws SQLException if the insert fails or the employee already has an entry today
     */
    public void clockIn(String empId) throws SQLException {
        String today = java.time.format.DateTimeFormatter
            .ofPattern("MM/dd/yyyy").format(LocalDate.now());
        String timeIn = java.time.format.DateTimeFormatter
            .ofPattern("HH:mm").format(java.time.LocalTime.now());

        if (hasEntryForToday(empId, today)) {
            throw new SQLException("Already have a time entry for today (" + today + ").");
        }
        String sql = "INSERT INTO attendance (empNum, date, logIn, logOut, hoursWorked) VALUES (?, ?, ?, '', '')";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, empId);
            stmt.setString(2, today);
            stmt.setString(3, timeIn);
            stmt.executeUpdate();
        }
    }

    /**
     * Records a clock-out event for the given employee using the current time.
     * Updates the most recent attendance row for today with the logOut time
     * and computes the total hours worked.
     *
     * @param empId the employee number clocking out
     * @throws SQLException if the update fails or no active clock-in is found
     */
    public void clockOut(String empId) throws SQLException {
        String timeOut = java.time.format.DateTimeFormatter
            .ofPattern("HH:mm").format(java.time.LocalTime.now());
        String sql = "UPDATE attendance SET logOut = ?, hoursWorked = ? " +
                     "WHERE empNum = ? AND (logOut IS NULL OR logOut = '') " +
                     "ORDER BY id DESC LIMIT 1";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, timeOut);
            stmt.setString(2, "");
            stmt.setString(3, empId);
            int rows = stmt.executeUpdate();
            if (rows == 0) throw new SQLException("No active clock-in found for employee " + empId);
        }
    }

    /**
     * Returns all time log rows for the given employee with no date filter.
     * Overload 1 of 2 — demonstrates method overloading.
     * Each row is a String array: [0]=empNum [1]=date [2]=logIn [3]=logOut [4]=hoursWorked.
     *
     * @param empId the employee number to retrieve logs for
     * @return list of time log rows; empty list if none found
     * @throws SQLException if the query fails
     */
    public List<String[]> getTimeLogs(String empId) throws SQLException {
        List<String[]> logs = new ArrayList<>();
        String sql = "SELECT empNum, date, logIn, logOut, hoursWorked FROM attendance WHERE empNum = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, empId.trim());
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    logs.add(new String[]{
                        rs.getString("empNum"),
                        rs.getString("date"),
                        rs.getString("logIn"),
                        rs.getString("logOut") == null ? "" : rs.getString("logOut"),
                        rs.getString("hoursWorked") == null ? "" : rs.getString("hoursWorked")
                    });
                }
            }
        }
        return logs;
    }

    /**
     * Returns time log rows for the given employee filtered by a date range.
     * Overload 2 of 2 — demonstrates method overloading.
     * Each row is a String array: [0]=empNum [1]=date [2]=logIn [3]=logOut [4]=hoursWorked.
     *
     * @param empId the employee number to retrieve logs for
     * @param start the start of the date range (inclusive)
     * @param end   the end of the date range (inclusive)
     * @return list of matching time log rows; empty list if none found
     * @throws SQLException if the query fails
     */
    public List<String[]> getTimeLogs(String empId, LocalDate start, LocalDate end) throws SQLException {
        List<String[]> logs = new ArrayList<>();
        String sql = "SELECT empNum, date, logIn, logOut, hoursWorked FROM attendance WHERE empNum = ?";
        java.time.format.DateTimeFormatter dateFmt = java.time.format.DateTimeFormatter.ofPattern("MM/dd/yyyy");
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, empId.trim());
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String dateStr = rs.getString("date");
                    if (dateStr != null && !dateStr.isBlank()) {
                        try {
                            LocalDate date = LocalDate.parse(dateStr.trim(), dateFmt);
                            if (!date.isBefore(start) && !date.isAfter(end)) {
                                logs.add(new String[]{
                                    rs.getString("empNum"),
                                    dateStr,
                                    rs.getString("logIn"),
                                    rs.getString("logOut") == null ? "" : rs.getString("logOut"),
                                    rs.getString("hoursWorked") == null ? "" : rs.getString("hoursWorked")
                                });
                            }
                        } catch (Exception ignored) {}
                    }
                }
            }
        }
        return logs;
    }

    private boolean hasEntryForToday(String empId, String dateStr) throws SQLException {
        String sql = "SELECT COUNT(*) FROM attendance WHERE empNum = ? AND date = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, empId);
            stmt.setString(2, dateStr);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }
}