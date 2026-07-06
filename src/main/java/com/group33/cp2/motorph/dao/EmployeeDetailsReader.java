package com.group33.cp2.motorph.dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Owns all read/write access to the employees and login tables.
 * No CSV access remains — all data comes from MySQL via JDBC.
 * Implements IEmployeeDAO to satisfy the DAO interface contract.
 *
 * <p>Row array column order for employees table:</p>
 * <pre>
 *   [0] employeeNum  [1] lastName    [2] firstName   [3] birthday
 *   [4] address      [5] phoneNumber [6] sssNumber    [7] philhealthNumber
 *   [8] tinNumber    [9] pagibigNumber [10] status    [11] position
 *   [12] supervisor
 * </pre>
 *
 * <p>Row array column order for login table:</p>
 * <pre>
 *   [0] employeeNum  [1] username  [2] roleName  [3] password  [4] changePassword
 * </pre>
 *
 * <p>Method overloading is demonstrated via changeUserPassword():
 * one variant accepts an explicit changePassword flag;
 * the other defaults it to "NO".</p>
 */
public class EmployeeDetailsReader implements IEmployeeDAO {

    /**
     * Returns all employee records from the employees table.
     * Each element in the returned list is a String array in the column order
     * documented in the class Javadoc above.
     *
     * @return list of employee rows; empty list if none found or on error
     */
    public List<String[]> getAllEmployees() {
        List<String[]> result = new ArrayList<>();
        String sql = "SELECT employeeNum, lastName, firstName, birthday, address, phoneNumber, sssNumber, philhealthNumber, tinNumber, pagibigNumber, status, position, supervisor FROM employees";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                result.add(new String[]{
                    rs.getString("employeeNum"),
                    rs.getString("lastName"),
                    rs.getString("firstName"),
                    rs.getString("birthday"),
                    rs.getString("address"),
                    rs.getString("phoneNumber"),
                    rs.getString("sssNumber"),
                    rs.getString("philhealthNumber"),
                    rs.getString("tinNumber"),
                    rs.getString("pagibigNumber"),
                    rs.getString("status"),
                    rs.getString("position"),
                    rs.getString("supervisor")
                });
            }
        } catch (SQLException e) {
            System.err.println("EmployeeDetailsReader.getAllEmployees: " + e.getMessage());
        }
        return result;
    }

    /**
     * Returns a single employee row by employeeNum, or null if not found.
     *
     * @param empId the employee number to look up
     * @return employee row array, or null if not found
     */
    public String[] getEmployeeDetails(String empId) {
        String sql = "SELECT employeeNum, lastName, firstName, birthday, address, phoneNumber, sssNumber, philhealthNumber, tinNumber, pagibigNumber, status, position, supervisor FROM employees WHERE employeeNum = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, empId.trim());
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new String[]{
                        rs.getString("employeeNum"),
                        rs.getString("lastName"),
                        rs.getString("firstName"),
                        rs.getString("birthday"),
                        rs.getString("address"),
                        rs.getString("phoneNumber"),
                        rs.getString("sssNumber"),
                        rs.getString("philhealthNumber"),
                        rs.getString("tinNumber"),
                        rs.getString("pagibigNumber"),
                        rs.getString("status"),
                        rs.getString("position"),
                        rs.getString("supervisor")
                    };
                }
            }
        } catch (SQLException e) {
            System.err.println("EmployeeDetailsReader.getEmployeeDetails: " + e.getMessage());
        }
        return null;
    }

    /**
     * Returns true if the given employeeNum and full name match a record in the database.
     * Name comparison is case-insensitive and expects "FirstName LastName" order.
     * Used by the password reset flow to verify the employee's identity before submitting a request.
     *
     * @param empNum  the employee number to verify
     * @param empName the full name in "FirstName LastName" format
     * @return true if a matching record exists
     */
    public boolean isEmployeeValid(String empNum, String empName) {
        String[] row = getEmployeeDetails(empNum);
        if (row == null || row.length < 3) return false;
        String fullName = row[2].trim() + " " + row[1].trim();
        return fullName.equalsIgnoreCase(empName == null ? "" : empName.trim());
    }

    public void addEmployee(String[] employeeRow) throws SQLException {
        String sql = "INSERT INTO employees VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            for (int i = 0; i < employeeRow.length; i++) {
                stmt.setString(i + 1, employeeRow[i]);
            }
            stmt.executeUpdate();
        }
    }

    public boolean updateEmployee(String[] updatedRow) throws SQLException {
        String sql = "UPDATE employees SET lastName=?, firstName=?, birthday=?, address=?, phoneNumber=?, sssNumber=?, philhealthNumber=?, tinNumber=?, pagibigNumber=?, status=?, position=?, supervisor=? WHERE employeeNum=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1,  updatedRow[1]);
            stmt.setString(2,  updatedRow[2]);
            stmt.setString(3,  updatedRow[3]);
            stmt.setString(4,  updatedRow[4]);
            stmt.setString(5,  updatedRow[5]);
            stmt.setString(6,  updatedRow[6]);
            stmt.setString(7,  updatedRow[7]);
            stmt.setString(8,  updatedRow[8]);
            stmt.setString(9,  updatedRow[9]);
            stmt.setString(10, updatedRow[10]);
            stmt.setString(11, updatedRow[11]);
            stmt.setString(12, updatedRow[12]);
            stmt.setString(13, updatedRow[0]);
            return stmt.executeUpdate() > 0;
        }
    }

    public boolean deleteEmployee(String empId) throws SQLException {
        String sql = "DELETE FROM employees WHERE employeeNum = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, empId.trim());
            return stmt.executeUpdate() > 0;
        }
    }

    public String[] getLoginDataByUsername(String username) {
        String sql = "SELECT employeeNum, username, roleName, password, changePassword FROM login WHERE username = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username.trim());
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new String[]{
                        rs.getString("employeeNum"),
                        rs.getString("username"),
                        rs.getString("roleName"),
                        rs.getString("password"),
                        rs.getString("changePassword")
                    };
                }
            }
        } catch (SQLException e) {
            System.err.println("EmployeeDetailsReader.getLoginDataByUsername: " + e.getMessage());
        }
        return null;
    }

    public String[] getLoginDataByEmpNum(String empNum) {
        String sql = "SELECT employeeNum, username, roleName, password, changePassword FROM login WHERE employeeNum = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, empNum.trim());
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new String[]{
                        rs.getString("employeeNum"),
                        rs.getString("username"),
                        rs.getString("roleName"),
                        rs.getString("password"),
                        rs.getString("changePassword")
                    };
                }
            }
        } catch (SQLException e) {
            System.err.println("EmployeeDetailsReader.getLoginDataByEmpNum: " + e.getMessage());
        }
        return null;
    }

    /**
     * Updates the BCrypt-hashed password for the given employee in the login table.
     * Overload 1 — also sets the changePassword flag explicitly.
     * Pass "YES" to force the employee to change their password on next login.
     *
     * @param empId             the employee number
     * @param newHashedPassword BCrypt-hashed new password
     * @param changePassword    "YES" to force change on next login, "NO" otherwise
     * @return true if a row was updated
     * @throws SQLException if the update fails
     */
    public boolean changeUserPassword(String empId, String newHashedPassword,
                                       String changePassword) throws SQLException {
        String sql = "UPDATE login SET password=?, changePassword=? WHERE employeeNum=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, newHashedPassword);
            stmt.setString(2, changePassword != null ? changePassword : "NO");
            stmt.setString(3, empId.trim());
            return stmt.executeUpdate() > 0;
        }
    }

    /**
     * Updates the BCrypt-hashed password for the given employee in the login table.
     * Overload 2 — defaults changePassword flag to "NO".
     * Used by the employee-initiated password change flow.
     *
     * @param empId             the employee number
     * @param newHashedPassword BCrypt-hashed new password
     * @return true if a row was updated
     * @throws SQLException if the update fails
     */
    public boolean changeUserPassword(String empId, String newHashedPassword)
            throws SQLException {
        return changeUserPassword(empId, newHashedPassword, "NO");
    }
}