package com.group33.cp2.motorph.dao;

import java.sql.*;

// owns read/write access to leave_balances table.
// no CSV access remains  all data comes from MySQL via JDBC.
public class EmployeeLeaveTracker {

    private static final String SICK_LEAVE     = "Sick Leave";
    private static final String VACATION_LEAVE = "Vacation Leave";
    private static final String BIRTHDAY_LEAVE = "Birthday Leave";

    public static int getLeaveBalance(String employeeId, String leaveType) throws SQLException {
        String col = columnFor(leaveType);
        if (col == null) return 0;
        String sql = "SELECT " + col + " FROM leave_balances WHERE empNum = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, employeeId.trim());
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    public static void updateLeaveBalance(String employeeId, String leaveType, int daysToDeduct)
            throws SQLException {
        String col = columnFor(leaveType);
        if (col == null) return;
        String sql = "UPDATE leave_balances SET " + col + " = " + col + " - ? WHERE empNum = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, daysToDeduct);
            stmt.setString(2, employeeId.trim());
            stmt.executeUpdate();
        }
    }

    private static String columnFor(String leaveType) {
        return switch (leaveType) {
            case SICK_LEAVE     -> "sickLeave";
            case VACATION_LEAVE -> "vacationLeave";
            case BIRTHDAY_LEAVE -> "birthdayLeave";
            default             -> null;
        };
    }
}