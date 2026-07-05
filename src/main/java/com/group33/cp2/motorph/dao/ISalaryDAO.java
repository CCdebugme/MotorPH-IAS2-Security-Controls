package com.group33.cp2.motorph.dao;

import com.group33.cp2.motorph.model.Salary;
import java.sql.SQLException;
import java.util.Map;

/**
 * Contract for all salary data access operations.
 *
 * Original implementation notes (from SalaryDetailsReader):
 * - Owns read/write access to the salary table.
 * - No CSV access remains — all data comes from MySQL via JDBC.
 * - Salary model contains: basicSalary, hourlyRate, grossSMRate.
 * - getAllSalaries() returns an unmodifiable map keyed by employeeNum.
 */
public interface ISalaryDAO {

    /** Returns the Salary record for a given employee, or null if not found. */
    Salary getSalary(String empId);

    /** Returns all salary records as an unmodifiable map keyed by employeeNum. */
    Map<String, Salary> getAllSalaries();

    /** Inserts a new salary record for the given employee. */
    void addSalary(String empId, Salary salary) throws SQLException;

    /** Updates the salary record for the given employee. */
    void updateSalary(String empId, Salary salary) throws SQLException;

    /** Deletes the salary record for the given employee. */
    void deleteSalary(String empId) throws SQLException;
}
