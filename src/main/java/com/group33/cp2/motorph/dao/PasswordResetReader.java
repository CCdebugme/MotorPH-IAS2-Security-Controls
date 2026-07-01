package com.group33.cp2.motorph.dao;

import com.group33.cp2.motorph.model.PasswordResetRequest;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

// owns read/write access to password_reset_requests table.
// No CSV access remains — all data comes from MySQL via JDBC.
public class PasswordResetReader {

    public static void addRequest(PasswordResetRequest request) throws SQLException {
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

    public static List<PasswordResetRequest> getAllRequests() {
        List<PasswordResetRequest> list = new ArrayList<>();
        String sql = "SELECT empNum, empName, dateOfRequest, status, adminName, adminEmpNum, dateOfReset " +
                     "FROM password_reset_requests";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                list.add(new PasswordResetRequest(
                    rs.getString("empNum"),
                    rs.getString("empName"),
                    rs.getString("dateOfRequest"),
                    rs.getString("status"),
                    rs.getString("adminName")    != null ? rs.getString("adminName")    : "",
                    rs.getString("adminEmpNum")  != null ? rs.getString("adminEmpNum")  : "",
                    rs.getString("dateOfReset")  != null ? rs.getString("dateOfReset")  : ""
                ));
            }
        } catch (SQLException e) {
            System.err.println("PasswordResetReader.getAllRequests: " + e.getMessage());
        }
        return list;
    }

    public static boolean approveRequest(String employeeNumber, String adminName, String adminEmpNum)
            throws SQLException {
        String currentDate = new SimpleDateFormat("MM/dd/yyyy - hh:mm:ss a").format(new Date());
        String sql = "UPDATE password_reset_requests SET status='Approved', adminName=?, " +
                     "adminEmpNum=?, dateOfReset=? " +
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