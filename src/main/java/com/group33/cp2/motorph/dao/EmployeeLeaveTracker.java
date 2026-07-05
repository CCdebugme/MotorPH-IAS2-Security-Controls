package com.group33.cp2.motorph.dao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.sql.*;

/**
 * Owns all read/write access to the leave_balances table.
 * No CSV access remains — all data comes from MySQL via JDBC.
 * Implements ILeaveBalanceDAO to satisfy the DAO interface contract.
 */
public class EmployeeLeaveTracker implements ILeaveBalanceDAO {

    private static final Logger log = LoggerFactory.getLogger(EmployeeLeaveTracker.class);

    private static final String SICK_LEAVE     = "Sick Leave";
    private static final String VACATION_LEAVE = "Vacation Leave";
    private static final String BIRTHDAY_LEAVE = "Birthday Leave";

    /**
     * Returns the remaining leave balance for the given employee and leave type.
     * Returns 0 if the employee has no record or the leave type is unrecognised.
     */
    @Override
    public int getLeaveBalance(String employeeId, String leaveType) throws SQLException {
        String col = columnFor(leaveType);
        if (col == null) {
            log.warn("EmployeeLeaveTracker.getLeaveBalance: unknown leaveType '{}'", leaveType);
            return 0;
        }
        String sql = "SELECT " + col + " FROM leave_balances WHERE empNum = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, employeeId.trim());
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    /**
     * Deducts the given number of days from the employee's leave balance.
     * Does nothing if the leave type is unrecognised.
     */
    @Override
    public void updateLeaveBalance(String employeeId, String leaveType, int daysToDeduct)
            throws SQLException {
        String col = columnFor(leaveType);
        if (col == null) {
            log.warn("EmployeeLeaveTracker.updateLeaveBalance: unknown leaveType '{}'", leaveType);
            return;
        }
        String sql = "UPDATE leave_balances SET " + col + " = " + col + " - ? WHERE empNum = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, daysToDeduct);
            stmt.setString(2, employeeId.trim());
            stmt.executeUpdate();
        }
    }

    /** Maps a leave type name to the corresponding database column name. */
    private static String columnFor(String leaveType) {
        return switch (leaveType) {
            case SICK_LEAVE     -> "sickLeave";
            case VACATION_LEAVE -> "vacationLeave";
            case BIRTHDAY_LEAVE -> "birthdayLeave";
            default             -> null;
        };
    }
}
