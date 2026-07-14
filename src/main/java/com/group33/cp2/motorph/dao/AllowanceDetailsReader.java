package com.group33.cp2.motorph.dao;

import com.group33.cp2.motorph.model.Allowance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.sql.*;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Owns all read/write access to the allowance table.
 * No CSV access remains — all data comes from MySQL via JDBC.
 * Implements {@link IAllowanceDAO} to satisfy the DAO interface contract.
 *
 * <p>The allowance table stores rice subsidy, phone allowance, and clothing
 * allowance per employee. {@link #getAllAllowances()} returns an unmodifiable
 * map keyed by employeeNum.</p>
 *
 * <p>Always call {@link #allowanceExists(String)} before {@link #addAllowance}
 * to prevent duplicate rows — the method automatically updates instead of
 * inserting when a record already exists.</p>
 */
// Owns read/write access to the allowance table.
// No CSV access remains — all data comes from MySQL via JDBC.
// Implements IAllowanceDAO to satisfy the DAO interface contract.
public class AllowanceDetailsReader implements IAllowanceDAO {

    /**
     * Returns the Allowance record for the given employee, or null if not found.
     * Logs an error if the database query fails.
     *
     * @param empId the employee number to look up
     * @return Allowance model containing rice, phone, and clothing allowances; or null
     */

    private static final Logger log = LoggerFactory.getLogger(AllowanceDetailsReader.class);
    
    /**
     * Returns true if an allowance record already exists for the given employee.
     * Always call this before {@link #addAllowance} to prevent duplicate rows.
     *
     * @param empId the employee number to check
     * @return true if a record exists; false otherwise
     * @throws SQLException if the database query fails
     */
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

    /**
     * Returns all allowance records as an unmodifiable map keyed by employeeNum.
     * Returns an empty map if no records exist or if the query fails.
     *
     * @return unmodifiable map of employeeNum to Allowance
     */
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

    /**
     * Updates the allowance record for the given employee.
     *
     * @param empId     the employee number
     * @param allowance Allowance object containing updated values
     * @throws SQLException if the update fails
     */
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

    /**
     * Deletes the allowance record for the given employee.
     *
     * @param empId the employee number whose allowance record should be removed
     * @throws SQLException if the delete fails
     */
    public void deleteAllowance(String empId) throws SQLException {
        String sql = "DELETE FROM allowance WHERE employeeNum = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, empId.trim());
            stmt.executeUpdate();
        }
    }
}