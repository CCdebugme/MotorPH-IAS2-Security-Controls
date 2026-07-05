package com.group33.cp2.motorph.dao;

import com.group33.cp2.motorph.model.PayrollLog;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

/**
 * Contract for all payroll data access operations.
 *
 * Original implementation notes (from PayrollLogReader):
 * - Owns read/write access to the payroll table.
 * - No CSV access remains — all data comes from MySQL via JDBC.
 * - PayrollLog is a record (immutable model) with fields:
 *   empNum, month, year, grossSalary, totalAllowance,
 *   totalDeductions, netMonthlySalary, periodStart, periodEnd.
 * - periodStart and periodEnd are stored as VARCHAR in the DB
 *   and parsed back to LocalDate on read.
 * - getAllLogs() returns an unmodifiable list.
 */
public interface IPayrollDAO {

    /** Returns true if a payroll record already exists for the given employee, month, and year. */
    boolean isAlreadyLogged(String empNum, int month, int year) throws SQLException;

    /** Returns true if a payroll record already exists for the given period range. */
    boolean isPeriodAlreadyLogged(String empNum, LocalDate start, LocalDate end) throws SQLException;

    /** Saves a new payroll log entry to the database. */
    void savePayrollLog(PayrollLog log) throws SQLException;

    /** Returns all payroll log records as an unmodifiable list. */
    List<PayrollLog> getAllLogs() throws SQLException;

    /** Returns payroll logs for a specific employee and period range. */
    List<PayrollLog> getLogByPeriod(String empNum, LocalDate start, LocalDate end) throws SQLException;
}
