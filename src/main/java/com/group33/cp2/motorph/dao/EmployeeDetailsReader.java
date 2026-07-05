package com.group33.cp2.motorph.dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EmployeeDetailsReader implements IEmployeeDAO {

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

    public boolean changeUserPassword(String empId, String newHashedPassword)
            throws SQLException {
        return changeUserPassword(empId, newHashedPassword, "NO");
    }
}