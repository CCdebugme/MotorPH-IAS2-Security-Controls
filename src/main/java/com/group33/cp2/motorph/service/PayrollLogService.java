package com.group33.cp2.motorph.service;

import com.group33.cp2.motorph.dao.PayrollLogReader;
import com.group33.cp2.motorph.model.CompensationDetails;
import com.group33.cp2.motorph.model.PayrollLog;
import com.group33.cp2.motorph.model.SalaryDetails;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

public class PayrollLogService {

    private static final Logger logger = LoggerFactory.getLogger(PayrollLogService.class);

    private final PayrollLogReader reader;

    public PayrollLogService() {
        this.reader = new PayrollLogReader();
    }

    public boolean isAlreadyLogged(String empNum, int month, int year) {
        try {
            return reader.isAlreadyLogged(empNum, month, year);
        } catch (SQLException e) {
            logger.error("PayrollLogService.isAlreadyLogged failed: {}", e.getMessage(), e);
            return false;
        }
    }

    public boolean isPeriodAlreadyLogged(String empNum, LocalDate start, LocalDate end) {
        try {
            return reader.isPeriodAlreadyLogged(empNum, start, end);
        } catch (SQLException e) {
            logger.error("PayrollLogService.isPeriodAlreadyLogged failed: {}", e.getMessage(), e);
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
            logger.error("PayrollLogService.getAllLogs failed: {}", e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    public List<PayrollLog> getLogsByPeriod(int month, int year) {
        List<PayrollLog> all = getAllLogs();
        List<PayrollLog> filtered = new java.util.ArrayList<>();
        for (PayrollLog entry : all) {
            if (entry.month() == month && entry.year() == year) {
                filtered.add(entry);
            }
        }
        return filtered;
    }

    public List<PayrollLog> getLogByPeriod(String empNum, LocalDate start, LocalDate end) {
        try {
            return reader.getLogByPeriod(empNum, start, end);
        } catch (SQLException e) {
            logger.error("PayrollLogService.getLogByPeriod failed: {}", e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    public PayrollLog getSingleLogByPeriod(String empNum, LocalDate start, LocalDate end) {
        try {
            List<PayrollLog> logs = reader.getLogByPeriod(empNum, start, end);
            return logs.isEmpty() ? null : logs.get(0);
        } catch (SQLException e) {
            logger.error("PayrollLogService.getSingleLogByPeriod failed: {}", e.getMessage(), e);
            return null;
        }
    }
}
