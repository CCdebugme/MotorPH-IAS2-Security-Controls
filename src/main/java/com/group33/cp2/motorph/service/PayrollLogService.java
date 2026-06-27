package com.group33.cp2.motorph.service;

import com.group33.cp2.motorph.dao.PayrollLogReader;
import com.group33.cp2.motorph.model.CompensationDetails;
import com.group33.cp2.motorph.model.PayrollLog;
import com.group33.cp2.motorph.model.SalaryDetails;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

public class PayrollLogService {

    private final PayrollLogReader reader;

    public PayrollLogService() {
        this.reader = new PayrollLogReader();
    }

    public boolean isAlreadyLogged(String empNum, int month, int year) {
        try {
            return reader.isAlreadyLogged(empNum, month, year);
        } catch (SQLException e) {
            System.err.println("PayrollLogService.isAlreadyLogged: " + e.getMessage());
            return false;
        }
    }

    public boolean isPeriodAlreadyLogged(String empNum, LocalDate start, LocalDate end) {
        try {
            return reader.isPeriodAlreadyLogged(empNum, start, end);
        } catch (SQLException e) {
            System.err.println("PayrollLogService.isPeriodAlreadyLogged: " + e.getMessage());
            return false;
        }
    }

    public void savePayrollRun(String empNum, int month, int year, SalaryDetails details) throws SQLException {
        PayrollLog log = new PayrollLog(
                empNum, month, year,
                details.grossSalary(),
                details.totalAllowances(),
                details.totalDeductions(),
                details.netSalary(),
                null, null);
        reader.savePayrollLog(log);
    }

    public void savePayrollRun(String empNum, int month, int year, CompensationDetails cd) throws SQLException {
        PayrollLog log = new PayrollLog(
                empNum, month, year,
                cd.getGrossSalary(),
                cd.getAllowance().getTotal(),
                cd.getDeductions().getTotal(),
                cd.getNetSalary(),
                null, null);
        reader.savePayrollLog(log);
    }

    public void savePayrollRun(String empNum, int month, int year,
                               LocalDate start, LocalDate end,
                               CompensationDetails cd) throws SQLException {
        PayrollLog log = new PayrollLog(
                empNum, month, year,
                cd.getGrossSalary(),
                cd.getAllowance().getTotal(),
                cd.getDeductions().getTotal(),
                cd.getNetSalary(),
                start, end);
        reader.savePayrollLog(log);
    }

    public List<PayrollLog> getAllLogs() {
        try {
            return reader.getAllLogs();
        } catch (SQLException e) {
            System.err.println("PayrollLogService.getAllLogs: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    public List<PayrollLog> getLogsByPeriod(int month, int year) {
        List<PayrollLog> all = getAllLogs();
        List<PayrollLog> filtered = new java.util.ArrayList<>();
        for (PayrollLog log : all) {
            if (log.month() == month && log.year() == year) {
                filtered.add(log);
            }
        }
        return filtered;
    }
    public List<PayrollLog> getLogByPeriod(String empNum, LocalDate start, LocalDate end) {
    try {
        return reader.getLogByPeriod(empNum, start, end);
        
    } catch (SQLException e) {
        System.err.println("PayrollLogService.getLogByPeriod: " + e.getMessage());
        return Collections.emptyList();
    }
    }
    public PayrollLog getSingleLogByPeriod(String empNum, LocalDate start, LocalDate end) {
    try {
        List<PayrollLog> logs = reader.getLogByPeriod(empNum, start, end);
        return logs.isEmpty() ? null : logs.get(0);
    } catch (SQLException e) {
        System.err.println("PayrollLogService.getSingleLogByPeriod: " + e.getMessage());
        return null;
    }
}    
       
    
}
