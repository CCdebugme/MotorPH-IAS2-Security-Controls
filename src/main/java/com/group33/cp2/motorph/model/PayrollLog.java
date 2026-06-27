package com.group33.cp2.motorph.model;

import java.time.LocalDate;

// Immutable record representing one completed payroll run for a single employee.
// Mirrors the columns of Payroll.csv: empNum, month, year, grossSalary,
// totalAllowance, totalDeductions, netMonthlySalary, periodStart, periodEnd.
// periodStart and periodEnd may be null for rows written before this schema change.
public record PayrollLog(
    String empNum,
    int month,
    int year,
    double grossSalary,
    double totalAllowance,
    double totalDeductions,
    double netSalary,
    LocalDate periodStart,
    LocalDate periodEnd
) {}
