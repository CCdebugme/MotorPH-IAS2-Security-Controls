package com.group33.cp2.motorph.dao;

import com.group33.cp2.motorph.model.PayrollLog;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PayrollLogReader implements IPayrollDAO {

    public boolean isAlreadyLogged(String empNum, int month, int year) throws SQLException {
        String sql = "SELECT COUNT(*) FROM payroll WHERE empNum=? AND month=? AND year=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, empNum);
            stmt.setInt(2, month);
            stmt.setInt(3, year);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    public boolean isPeriodAlreadyLogged(String empNum, LocalDate start, LocalDate end)
            throws SQLException {
        String sql = "SELECT COUNT(*) FROM payroll WHERE empNum=? AND periodStart=? AND periodEnd=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, empNum);
            stmt.setString(2, start != null ? start.toString() : "");
            stmt.setString(3, end != null ? end.toString() : "");
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    public void savePayrollLog(PayrollLog log) throws SQLException {
        String sql = "INSERT INTO payroll (empNum, month, year, grossSalary, totalAllowance, " +
                     "totalDeductions, netMonthlySalary, periodStart, periodEnd) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, log.empNum());
            stmt.setInt(2, log.month());
            stmt.setInt(3, log.year());
            stmt.setDouble(4, log.grossSalary());
            stmt.setDouble(5, log.totalAllowance());
            stmt.setDouble(6, log.totalDeductions());
            stmt.setDouble(7, log.netSalary());
            stmt.setString(8, log.periodStart() != null ? log.periodStart().toString() : "");
            stmt.setString(9, log.periodEnd() != null ? log.periodEnd().toString() : "");
            stmt.executeUpdate();
        }
    }

    public List<PayrollLog> getAllLogs() throws SQLException {
        List<PayrollLog> logs = new ArrayList<>();
        String sql = "SELECT empNum, month, year, grossSalary, totalAllowance, " +
                     "totalDeductions, netMonthlySalary, periodStart, periodEnd FROM payroll";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                String startStr = rs.getString("periodStart");
                String endStr   = rs.getString("periodEnd");
                LocalDate periodStart = (startStr != null && !startStr.isBlank()) ? LocalDate.parse(startStr) : null;
                LocalDate periodEnd   = (endStr != null && !endStr.isBlank()) ? LocalDate.parse(endStr) : null;
                logs.add(new PayrollLog(
                    rs.getString("empNum"),
                    rs.getInt("month"),
                    rs.getInt("year"),
                    rs.getDouble("grossSalary"),
                    rs.getDouble("totalAllowance"),
                    rs.getDouble("totalDeductions"),
                    rs.getDouble("netMonthlySalary"),
                    periodStart,
                    periodEnd
                ));
            }
        }
        return Collections.unmodifiableList(logs);
    }

    public List<PayrollLog> getLogByPeriod(String empNum, LocalDate start, LocalDate end)
            throws SQLException {
        List<PayrollLog> logs = new ArrayList<>();
        String sql = "SELECT empNum, month, year, grossSalary, totalAllowance, " +
                     "totalDeductions, netMonthlySalary, periodStart, periodEnd FROM payroll " +
                     "WHERE empNum=? AND periodStart=? AND periodEnd=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, empNum);
            stmt.setString(2, start != null ? start.toString() : "");
            stmt.setString(3, end != null ? end.toString() : "");
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String startStr = rs.getString("periodStart");
                    String endStr   = rs.getString("periodEnd");
                    LocalDate pStart = (startStr != null && !startStr.isBlank()) ? LocalDate.parse(startStr) : null;
                    LocalDate pEnd   = (endStr != null && !endStr.isBlank()) ? LocalDate.parse(endStr) : null;
                    logs.add(new PayrollLog(
                        rs.getString("empNum"),
                        rs.getInt("month"),
                        rs.getInt("year"),
                        rs.getDouble("grossSalary"),
                        rs.getDouble("totalAllowance"),
                        rs.getDouble("totalDeductions"),
                        rs.getDouble("netMonthlySalary"),
                        pStart, pEnd
                    ));
                }
            }
        }
        return logs;
    }
}