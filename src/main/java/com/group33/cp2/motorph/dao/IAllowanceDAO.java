package com.group33.cp2.motorph.dao;

import com.group33.cp2.motorph.model.Allowance;
import java.sql.SQLException;
import java.util.Map;

/**
 * Contract for all allowance data access operations.
 *
 * Original implementation notes (from AllowanceDetailsReader):
 * - Owns read/write access to the allowance table.
 * - No CSV access remains — all data comes from MySQL via JDBC.
 * - Allowance model contains: employeeNum, riceSubsidy,
 *   phoneAllowance, clothingAllowance.
 * - getAllAllowances() returns an unmodifiable map keyed by employeeNum.
 * - allowanceExists() must be checked before addAllowance() to prevent
 *   duplicate records for the same employee.
 */
public interface IAllowanceDAO {

    /** Returns the Allowance record for a given employee, or null if not found. */
    Allowance getAllowance(String empId);

    /** Returns all allowance records as an unmodifiable map keyed by employeeNum. */
    Map<String, Allowance> getAllAllowances();

    /**
     * Returns true if an allowance record already exists for the given employee.
     * Always call this before addAllowance() to avoid duplicate records.
     */
    boolean allowanceExists(String empId) throws SQLException;

    /** Inserts a new allowance record for the given employee. */
    void addAllowance(String empId, Allowance allowance) throws SQLException;

    /** Updates the allowance record for the given employee. */
    void updateAllowance(String empId, Allowance allowance) throws SQLException;

    /** Deletes the allowance record for the given employee. */
    void deleteAllowance(String empId) throws SQLException;
}
