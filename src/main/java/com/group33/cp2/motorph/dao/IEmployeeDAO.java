package com.group33.cp2.motorph.dao;

import java.sql.SQLException;
import java.util.List;

/**
 * Contract for all employee data access operations.
 *
 * Original implementation notes (from EmployeeDetailsReader):
 * - Owns read/write access to the employees and login tables.
 * - No CSV access remains — all data comes from MySQL via JDBC.
 * - Government IDs (sssNumber, philhealthNumber, tinNumber, pagibigNumber)
 *   are stored directly in the employees table at row indexes 6, 7, 8, 9.
 * - changeUserPassword() is overloaded: one variant accepts a changePassword
 *   flag, the other defaults it to "NO".
 */
public interface IEmployeeDAO {

    /** Returns all employee rows. Each row maps to the employees table columns. */
    List<String[]> getAllEmployees();

    /** Returns a single employee row by employeeNum, or null if not found. */
    String[] getEmployeeDetails(String empId);

    /** Returns true if the given empNum and empName combination exists. */
    boolean isEmployeeValid(String empNum, String empName);

    /** Inserts a new employee record. */
    void addEmployee(String[] employeeRow) throws SQLException;

    /** Updates an existing employee record. Returns true if a row was updated. */
    boolean updateEmployee(String[] updatedRow) throws SQLException;

    /** Deletes an employee by employeeNum. Returns true if a row was deleted. */
    boolean deleteEmployee(String empId) throws SQLException;

    /** Returns login data for the given username, or null if not found. */
    String[] getLoginDataByUsername(String username);

    /** Returns login data for the given employeeNum, or null if not found. */
    String[] getLoginDataByEmpNum(String empNum);

    /**
     * Updates the hashed password for the given employee.
     * Overload 1 — also sets the changePassword flag.
     */
    boolean changeUserPassword(String empId, String newHashedPassword,
                               String changePassword) throws SQLException;

    /**
     * Updates the hashed password for the given employee.
     * Overload 2 — defaults changePassword flag to "NO".
     */
    boolean changeUserPassword(String empId, String newHashedPassword) throws SQLException;
}
