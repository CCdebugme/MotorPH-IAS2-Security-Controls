package com.group33.cp2.motorph.model;

// Immutable record holding a complete payroll computation result for one employee.
// Returned by PayrollCalculatorService.getSalaryDetails() — callers see only this record,
// not the underlying CSV reads or deduction formulas.
public record SalaryDetails(
    double grossSalary, double netSalary, double hourlyRate,
    double riceSubsidy, double phoneAllowance, double clothingAllowance, double totalAllowances,
    double pagibigDeduction, double philHealthDeduction, double sssDeduction,
    double withholdingTax, double totalDeductions
) {}
