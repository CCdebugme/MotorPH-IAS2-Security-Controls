package com.group33.cp2.motorph.dao;

import com.group33.cp2.motorph.model.Salary;
import java.sql.*;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;


public class SalaryDetailsReader {

    public Salary getSalary(String empId) {
        String sql = "SELECT basicSalary, hourlyRate, grossSMRate FROM salary WHERE employeeNum = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, empId.trim());
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new Salary(
                        rs.getDouble("basicSalary"),
                        rs.getDouble("hourlyRate"),
                        rs.getDouble("grossSMRate")
                    );
                }
            }
        } catch (SQLException e) {
            System.err.println("SalaryDetailsReader.getSalary: " + e.getMessage());
        }
        return null;
    }

    public Map<String, Salary> getAllSalaries() {
        Map<String, Salary> result = new LinkedHashMap<>();
        String sql = "SELECT employeeNum, basicSalary, hourlyRate, grossSMRate FROM salary";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                result.put(rs.getString("employeeNum"), new Salary(
                    rs.getDouble("basicSalary"),
                    rs.getDouble("hourlyRate"),
                    rs.getDouble("grossSMRate")
                ));
            }
        } catch (SQLException e) {
            System.err.println("SalaryDetailsReader.getAllSalaries: " + e.getMessage());
        }
        return Collections.unmodifiableMap(result);
    }

    public void addSalary(String empId, Salary salary) throws SQLException {
        String sql = "INSERT INTO salary VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, empId.trim());
            stmt.setDouble(2, salary.getBasicSalary());
            stmt.setDouble(3, salary.getHourlyRate());
            stmt.setDouble(4, salary.getGrossSMRate());
            stmt.executeUpdate();
        }
    }

    public void updateSalary(String empId, Salary salary) throws SQLException {
        String sql = "UPDATE salary SET basicSalary=?, hourlyRate=?, grossSMRate=? WHERE employeeNum=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setDouble(1, salary.getBasicSalary());
            stmt.setDouble(2, salary.getHourlyRate());
            stmt.setDouble(3, salary.getGrossSMRate());
            stmt.setString(4, empId.trim());
            stmt.executeUpdate();
        }
    }

    public void deleteSalary(String empId) throws SQLException {
        String sql = "DELETE FROM salary WHERE employeeNum = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, empId.trim());
            stmt.executeUpdate();
        }
    }
}