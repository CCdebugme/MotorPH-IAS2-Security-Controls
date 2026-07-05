package com.group33.cp2.motorph.dao;

import java.sql.SQLException;

/**
 * Contract for all leave balance data access operations.
 *
 * Original implementation notes (from EmployeeLeaveTracker):
 * - Owns read/write access to the leave_balances table.
 * - No CSV access remains — all data comes from MySQL via JDBC.
 * - Supported leave types: "Sick Leave", "Vacation Leave", "Birthday Leave".
 * - columnFor() maps leave type strings to DB column names:
 *   "Sick Leave" -> sickLeave, "Vacation Leave" -> vacationLeave,
 *   "Birthday Leave" -> birthdayLeave.
 * - Returns 0 and logs a warning for unrecognised leave types.
 */
public interface ILeaveBalanceDAO {

    /** Returns the remaining leave balance for the given employee and leave type. */
    int getLeaveBalance(String employeeId, String leaveType) throws SQLException;

    /** Deducts the given number of days from the employee's leave balance. */
    void updateLeaveBalance(String employeeId, String leaveType, int daysToDeduct) throws SQLException;
}
