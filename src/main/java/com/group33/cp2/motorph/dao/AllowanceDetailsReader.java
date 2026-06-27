package com.group33.cp2.motorph.dao;

import com.group33.cp2.motorph.model.Allowance;
import java.sql.*;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

// Owns read/write access to allowance table.
// No CSV access remains — all data comes from MySQL via JDBC.
public class AllowanceDetailsReader {

    public Allowance getAllowance(String empId) {
        String sql = "SELECT employeeNum, riceSubsidy, phoneAllowance, clothingAllowance FROM allowance WHERE employeeNum = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, empId.trim());
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new Allowance(
                        rs.getString("employeeNum"),
                        rs.getDouble("riceSubsidy"),
                        rs.getDouble("phoneAllowance"),
                        rs.getDouble("clothingAllowance")
                    );
                }
            }
        } catch (SQLException e) {
            System.err.println("AllowanceDetailsReader.getAllowance: " + e.getMessage());
        }
        return null;
    }

    public Map<String, Allowance> getAllAllowances() {
        Map<String, Allowance> result = new LinkedHashMap<>();
        String sql = "SELECT employeeNum, riceSubsidy, phoneAllowance, clothingAllowance FROM allowance";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                String empId = rs.getString("employeeNum");
                result.put(empId, new Allowance(
                    empId,
                    rs.getDouble("riceSubsidy"),
                    rs.getDouble("phoneAllowance"),
                    rs.getDouble("clothingAllowance")
                ));
            }
        } catch (SQLException e) {
            System.err.println("AllowanceDetailsReader.getAllAllowances: " + e.getMessage());
        }
        return Collections.unmodifiableMap(result);
    }

    public void addAllowance(String empId, Allowance allowance) throws SQLException {
        String sql = "INSERT INTO allowance VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, empId.trim());
            stmt.setDouble(2, allowance.getRiceAllowance());
            stmt.setDouble(3, allowance.getPhoneAllowance());
            stmt.setDouble(4, allowance.getClothingAllowance());
            stmt.executeUpdate();
        }
    }

    public void updateAllowance(String empId, Allowance allowance) throws SQLException {
        String sql = "UPDATE allowance SET riceSubsidy=?, phoneAllowance=?, clothingAllowance=? WHERE employeeNum=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setDouble(1, allowance.getRiceAllowance());
            stmt.setDouble(2, allowance.getPhoneAllowance());
            stmt.setDouble(3, allowance.getClothingAllowance());
            stmt.setString(4, empId.trim());
            stmt.executeUpdate();
        }
    }

    public void deleteAllowance(String empId) throws SQLException {
        String sql = "DELETE FROM allowance WHERE employeeNum = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, empId.trim());
            stmt.executeUpdate();
        }
    }
}