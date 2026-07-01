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
public class AttendanceDAO {

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