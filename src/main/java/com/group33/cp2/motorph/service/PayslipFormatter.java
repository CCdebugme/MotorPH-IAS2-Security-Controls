package com.group33.cp2.motorph.service;

import com.group33.cp2.motorph.model.PayslipReport;

// Formats a PayslipReport into readable text output.
// No calculations, no database access — reads only from the report object.
public class PayslipFormatter {

    public static String format(PayslipReport report) {
        return String.format(
            "========================================%n" +
            "         MOTORPH PAYROLL SYSTEM         %n" +
            "              PAYSLIP                   %n" +
            "========================================%n" +
            "Employee ID   : %s%n" +
            "Employee Name : %s%n" +
            "Position      : %s%n" +
            "Pay Period    : %s to %s%n" +
            "----------------------------------------%n" +
            "HOURS WORKED%n" +
            "  Regular Hours   : %.2f%n" +
            "  Overtime Hours  : %.2f%n" +
            "----------------------------------------%n" +
            "EARNINGS%n" +
            "  Basic Salary    : PHP %.2f%n" +
            "  Regular Pay     : PHP %.2f%n" +
            "  Overtime Pay    : PHP %.2f%n" +
            "  Rice Allowance  : PHP %.2f%n" +
            "  Phone Allowance : PHP %.2f%n" +
            "  Clothing Allow. : PHP %.2f%n" +
            "  Total Allowances: PHP %.2f%n" +
            "  Gross Salary    : PHP %.2f%n" +
            "----------------------------------------%n" +
            "DEDUCTIONS%n" +
            "  SSS             : PHP %.2f%n" +
            "  PhilHealth      : PHP %.2f%n" +
            "  Pag-IBIG        : PHP %.2f%n" +
            "  Withholding Tax : PHP %.2f%n" +
            "  Total Deductions: PHP %.2f%n" +
            "----------------------------------------%n" +
            "NET SALARY      : PHP %.2f%n" +
            "========================================%n",
            report.getEmployeeId(),
            report.getEmployeeName(),
            report.getPosition(),
            report.getPeriodStart(),
            report.getPeriodEnd(),
            report.getRegularHours(),
            report.getOvertimeHours(),
            report.getBasicSalary(),
            report.getRegularPay(),
            report.getOvertimePay(),
            report.getRiceAllowance(),
            report.getPhoneAllowance(),
            report.getClothingAllowance(),
            report.getTotalAllowances(),
            report.getGrossSalary(),
            report.getSss(),
            report.getPhilHealth(),
            report.getPagibig(),
            report.getWithholdingTax(),
            report.getTotalDeductions(),
            report.getNetSalary()
        );
    }
}