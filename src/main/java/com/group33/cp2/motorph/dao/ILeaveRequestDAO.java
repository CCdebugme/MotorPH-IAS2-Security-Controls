package com.group33.cp2.motorph.dao;

import com.group33.cp2.motorph.model.LeaveRequest;
import java.sql.SQLException;
import java.util.List;

/**
 * Contract for all leave request data access operations.
 *
 * Original implementation notes (from LeaveRequestReader):
 * - Previously owned read/write access to LeaveRequests.csv.
 * - Now fully migrated to MySQL via JDBC — no CSV access remains.
 * - LeaveRequest model fields: leaveID, employeeID, leaveType,
 *   dateRequest, startDate, endDate, reason, status, approver,
 *   dateResponded, remark.
 * - getAllLeaveRequests() skips malformed rows and logs a warning
 *   rather than throwing, so the HR dashboard can still load.
 */
public interface ILeaveRequestDAO {

    /** Inserts a new leave request record. */
    void addLeaveRequest(LeaveRequest leaveRequest) throws SQLException;

    /** Returns a single leave request by leaveID, or null if not found. */
    LeaveRequest getLeaveById(String leaveID) throws SQLException;

    /** Updates an existing leave request record. */
    void updateLeaveRequest(LeaveRequest updatedLeave) throws SQLException;

    /** Returns all leave request records. Returns an empty list on error. */
    List<LeaveRequest> getAllLeaveRequests();
}
