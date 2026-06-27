package com.group33.cp2.motorph.model;

// Interface for payroll calculation: implemented by all concrete employee types.
// Each subtype applies the payroll rules appropriate for that employment category.
public interface PayrollCalculable {

    double calculateGrossSalary();
    double calculateGrossSalary(double salary);
    double calculateGrossSalary(double salary, double overtimeHours);
    double calculateDeductions();
    double calculateNetSalary();
    double calculateOvertimePay(double overtimeHours);
}
