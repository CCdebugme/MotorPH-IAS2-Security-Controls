package com.group33.cp2.motorph.dao;

import com.group33.cp2.motorph.model.PasswordResetRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Owns all read/write access to the password_reset_requests table.
 * No CSV access remains — all data comes from MySQL via JDBC.
 * Implements IPasswordResetDAO to satisfy the DAO interface contract.
 */
public class PasswordResetReader implements IPasswordResetDAO {

    private static final Logger logger = LoggerFactory.getLogger(PasswordResetReader.class);

    /**
     * Inserts a new password reset request record into the database.
     *
     * @param request the reset request to insert
     * @throws SQLException if the insert fails
     */
    @Override
    public void addRequest(PasswordResetRequest request) throws SQLException {
        String sql = "INSERT INTO password_reset_requests " +
                     "(empNum, empName, dateOfRequest, status, adminName, adminEmpNum, dateOfReset) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            String[] arr = request.toArray();
            stmt.setString(1, arr[0]);
            stmt.setString(2, arr[1]);
            stmt.setString(3, arr[2]);
            stmt.setString(4, arr[3]);
            stmt.setString(5, arr[4]);
            stmt.setString(6, arr[5]);
            stmt.setString(7, arr[6]);
            stmt.executeUpdate();
        }
    }

    /**
     * Returns all password reset request records from the database.
     * Logs an error if the query fails rather than propagating the exception,
     * so the IT dashboard can still load even if this call fails.
     *
     * @return list of all requests; empty list on error
     */
    @Override
    public List<PasswordResetRequest> getAllRequests() {
        List<PasswordResetRequest> list = new ArrayList<>();
        String sql = "SELECT empNum, empName, dateOfRequest, status, " +
                     "adminName, adminEmpNum, dateOfReset FROM password_reset_requests";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                list.add(new PasswordResetRequest(
                    rs.getString("empNum"),
                    rs.getString("empName"),
                    rs.getString("dateOfRequest"),
                    rs.getString("status"),
                    rs.getString("adminName")   != null ? rs.getString("adminName")   : "",
                    rs.getString("adminEmpNum") != null ? rs.getString("adminEmpNum") : "",
                    rs.getString("dateOfReset") != null ? rs.getString("dateOfReset") : ""
                ));
            }
        } catch (SQLException e) {
            logger.error("PasswordResetReader.getAllRequests failed: {}", e.getMessage(), e);
        }
        return list;
    }

    /**
     * Marks the most recent pending reset request for the employee as approved.
     *
     * @param employeeNumber the employee whose request is being approved
     * @param adminName      full name of the IT admin approving the request
     * @param adminEmpNum    employee number of the IT admin
     * @return true if a pending record was found and updated
     * @throws SQLException if the update fails
     */
    @Override
    public boolean approveRequest(String employeeNumber, String adminName, String adminEmpNum)
            throws SQLException {
        String currentDate = new SimpleDateFormat("MM/dd/yyyy - hh:mm:ss a").format(new Date());
        String sql = "UPDATE password_reset_requests " +
                     "SET status='Approved', adminName=?, adminEmpNum=?, dateOfReset=? " +
                     "WHERE empNum=? AND status='Pending'";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, adminName);
            stmt.setString(2, adminEmpNum);
            stmt.setString(3, currentDate);
            stmt.setString(4, employeeNumber);
            return stmt.executeUpdate() > 0;
        }
    }
}
