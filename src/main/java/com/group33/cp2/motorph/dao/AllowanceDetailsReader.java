package com.group33.cp2.motorph.dao;

import com.group33.cp2.motorph.model.Allowance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.sql.*;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

// Owns read/write access to the allowance table.
// No CSV access remains — all data comes from MySQL via JDBC.
// Implements IAllowanceDAO to satisfy the DAO interface contract.
public class AllowanceDetailsReader implements IAllowanceDAO {

    private static final Logger log = LoggerFactory.getLogger(AllowanceDetailsReader.class);
    
    public boolean allowanceExists(String empId) throws SQLException {
    String sql = "SELECT COUNT(*) FROM allowance WHERE employeeNum = ?";
    try (Connection conn = DatabaseConnection.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {
        stmt.setString(1, empId.trim());
        try (ResultSet rs = stmt.executeQuery()) {
            return rs.next() && rs.getInt(1) > 0;
        }
    }
}

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
            log.error("AllowanceDetailsReader.getAllowance failed for emp {}: {}", empId, e.getMessage(), e);
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
            log.error("AllowanceDetailsReader.getAllAllowances failed: {}", e.getMessage(), e);
        }
        return Collections.unmodifiableMap(result);
    }

    /**
     * Inserts a new allowance record for the given employee.
     * Checks for an existing record first to prevent duplicate rows.
     * If a record already exists, updates it instead of inserting.
     *
     * @throws SQLException if the database operation fails
     */
    public void addAllowance(String empId, Allowance allowance) throws SQLException {
        if (allowanceExists(empId)) {
            // Record already exists — update instead of inserting a duplicate
            log.warn("AllowanceDetailsReader.addAllowance: record already exists for emp {}, updating instead.", empId);
            updateAllowance(empId, allowance);
            return;
        }
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