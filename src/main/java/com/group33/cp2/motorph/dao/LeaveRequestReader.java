package com.group33.cp2.motorph.dao;

import com.group33.cp2.motorph.model.LeaveRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Owns all read/write access to the leave_requests table.
 * No CSV access remains — all data comes from MySQL via JDBC.
 * Implements ILeaveRequestDAO to satisfy the DAO interface contract.
 */
/**
 * Owns all read/write access to the leave_requests table.
 * No CSV access remains — all data comes from MySQL via JDBC.
 * Implements {@link ILeaveRequestDAO} to satisfy the DAO interface contract.
 *
 * <p>Previously owned read/write access to LeaveRequests.csv — fully migrated
 * to MySQL. Date fields are stored as VARCHAR and parsed to
 * {@link java.time.LocalDate} on retrieval. {@link #getAllLeaveRequests()}
 * skips malformed rows and logs a warning rather than throwing, so the
 * HR dashboard can still load even if individual records are corrupt.</p>
 */
public class LeaveRequestReader implements ILeaveRequestDAO {

    private static final Logger log = LoggerFactory.getLogger(LeaveRequestReader.class);

    // ── ILeaveRequestDAO implementation ──────────────────────────────────────

    /**
     * Inserts a new leave request record into the database.
     *
     * @param leaveRequest the leave request to insert
     * @throws SQLException if the insert fails
     */
    @Override
    public void addLeaveRequest(LeaveRequest leaveRequest) throws SQLException {
        String sql = "INSERT INTO leave_requests " +
                     "(leaveID, empNum, leaveType, dateRequest, startDate, endDate, " +
                     "reason, status, approver, dateResponded, remark) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1,  leaveRequest.getLeaveID());
            stmt.setString(2,  leaveRequest.getEmployeeID());
            stmt.setString(3,  leaveRequest.getLeaveType());
            stmt.setString(4,  leaveRequest.getDateRequest().toString());
            stmt.setString(5,  leaveRequest.getStartDate().toString());
            stmt.setString(6,  leaveRequest.getEndDate().toString());
            stmt.setString(7,  leaveRequest.getReason());
            stmt.setString(8,  leaveRequest.getStatus());
            stmt.setString(9,  leaveRequest.getApprover() != null ? leaveRequest.getApprover() : "");
            stmt.setString(10, leaveRequest.getDateResponded() != null
                               ? leaveRequest.getDateResponded().toString() : "");
            stmt.setString(11, leaveRequest.getRemark() != null ? leaveRequest.getRemark() : "");
            stmt.executeUpdate();
        }
    }

    /**
     * Returns a single leave request by leaveID, or null if no record is found.
     *
     * @param leaveID the unique leave request identifier
     * @return matching LeaveRequest, or null
     * @throws SQLException if the query fails
     */
    @Override
    public LeaveRequest getLeaveById(String leaveID) throws SQLException {
        String sql = "SELECT leaveID, empNum, leaveType, dateRequest, startDate, endDate, " +
                     "reason, status, approver, dateResponded, remark " +
                     "FROM leave_requests WHERE leaveID = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, leaveID.trim());
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        }
        return null;
    }

    /**
     * Updates an existing leave request record in the database.
     *
     * @param updatedLeave the leave request containing updated values
     * @throws SQLException if the update fails
     */
    @Override
    public void updateLeaveRequest(LeaveRequest updatedLeave) throws SQLException {
        String sql = "UPDATE leave_requests SET empNum=?, leaveType=?, dateRequest=?, " +
                     "startDate=?, endDate=?, reason=?, status=?, approver=?, " +
                     "dateResponded=?, remark=? WHERE leaveID=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1,  updatedLeave.getEmployeeID());
            stmt.setString(2,  updatedLeave.getLeaveType());
            stmt.setString(3,  updatedLeave.getDateRequest().toString());
            stmt.setString(4,  updatedLeave.getStartDate().toString());
            stmt.setString(5,  updatedLeave.getEndDate().toString());
            stmt.setString(6,  updatedLeave.getReason());
            stmt.setString(7,  updatedLeave.getStatus());
            stmt.setString(8,  updatedLeave.getApprover() != null ? updatedLeave.getApprover() : "");
            stmt.setString(9,  updatedLeave.getDateResponded() != null
                               ? updatedLeave.getDateResponded().toString() : "");
            stmt.setString(10, updatedLeave.getRemark() != null ? updatedLeave.getRemark() : "");
            stmt.setString(11, updatedLeave.getLeaveID());
            stmt.executeUpdate();
        }
    }

    /**
     * Returns all leave request records from the database.
     * Logs a warning if any row cannot be parsed rather than crashing.
     *
     * @return list of all leave requests; empty list if none found or on error
     */
    @Override
    public List<LeaveRequest> getAllLeaveRequests() {
        List<LeaveRequest> leaveRequests = new ArrayList<>();
        String sql = "SELECT leaveID, empNum, leaveType, dateRequest, startDate, endDate, " +
                     "reason, status, approver, dateResponded, remark FROM leave_requests";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                try {
                    leaveRequests.add(mapRow(rs));
                } catch (Exception e) {
                    log.warn("LeaveRequestReader: skipping malformed row — {}", e.getMessage());
                }
            }
        } catch (SQLException e) {
            log.error("LeaveRequestReader.getAllLeaveRequests failed: {}", e.getMessage(), e);
        }
        return leaveRequests;
    }

    // ── Private helper ────────────────────────────────────────────────────────

    /**
     * Maps a ResultSet row to a LeaveRequest model object.
     * Date fields stored as VARCHAR are parsed to LocalDate where non-blank.
     */
    private LeaveRequest mapRow(ResultSet rs) throws SQLException {
        String dateRespondedStr = rs.getString("dateResponded");
        LocalDate dateResponded = (dateRespondedStr != null && !dateRespondedStr.isBlank())
                                  ? LocalDate.parse(dateRespondedStr.trim()) : null;
        return new LeaveRequest(
            rs.getString("leaveID"),
            rs.getString("empNum"),
            rs.getString("leaveType"),
            LocalDate.parse(rs.getString("dateRequest").trim()),
            LocalDate.parse(rs.getString("startDate").trim()),
            LocalDate.parse(rs.getString("endDate").trim()),
            rs.getString("reason"),
            rs.getString("status"),
            rs.getString("approver") != null ? rs.getString("approver") : "",
            dateResponded,
            rs.getString("remark") != null ? rs.getString("remark") : ""
        );
    }
}
