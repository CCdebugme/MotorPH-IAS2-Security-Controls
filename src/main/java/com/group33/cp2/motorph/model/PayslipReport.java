package com.group33.cp2.motorph.model;

import java.time.LocalDate;

// Read-only report data object for payslip generation.
// Contains already-computed values — no persistence logic, no UI logic.
public class PayslipReport {

    private final String employeeName;
    private final String employeeId;
    private final String position;
    private final LocalDate periodStart;
    private final LocalDate periodEnd;
    private final double regularHours;
    private final double overtimeHours;
    private final double basicSalary;
    private final double riceAllowance;
    private final double phoneAllowance;
    private final double clothingAllowance;
    private final double totalAllowances;
    private final double regularPay;
    private final double overtimePay;
    private final double grossSalary;
    private final double sss;
    private final double philHealth;
    private final double pagibig;
    private final double withholdingTax;
    private final double totalDeductions;
    private final double netSalary;

    public PayslipReport(Employee emp, Payslip payslip) {
        CompensationDetails cd = payslip.getCompensationDetails();
        Deductions ded = cd.getDeductions();
        Allowance allow = cd.getAllowance();

        this.employeeName     = emp.getFullName();
        this.employeeId       = emp.getEmployeeID();
        this.position         = emp.getPosition();
        this.periodStart      = payslip.getPeriodStartDate();
        this.periodEnd        = payslip.getPeriodEndDate();
        this.regularHours     = payslip.getTotalRegularHours();
        this.overtimeHours    = payslip.getTotalOvertimeHours();
        this.basicSalary      = emp.getBasicSalary();
        this.riceAllowance    = allow.getRiceAllowance();
        this.phoneAllowance   = allow.getPhoneAllowance();
        this.clothingAllowance = allow.getClothingAllowance();
        this.totalAllowances  = allow.getTotal();
        this.regularPay       = cd.getRegularPay();
        this.overtimePay      = cd.getOvertimePay();
        this.grossSalary      = cd.getGrossSalary();
        this.sss              = ded.getSss();
        this.philHealth       = ded.getPhilHealth();
        this.pagibig          = ded.getPagIbig();
        this.withholdingTax   = ded.getTax();
        this.totalDeductions  = ded.getTotal();
        this.netSalary        = cd.getNetSalary();
    }

    public String getEmployeeName()      { return employeeName; }
    public String getEmployeeId()        { return employeeId; }
    public String getPosition()          { return position; }
    public LocalDate getPeriodStart()    { return periodStart; }
    public LocalDate getPeriodEnd()      { return periodEnd; }
    public double getRegularHours()      { return regularHours; }
    public double getOvertimeHours()     { return overtimeHours; }
    public double getBasicSalary()       { return basicSalary; }
    public double getRiceAllowance()     { return riceAllowance; }
    public double getPhoneAllowance()    { return phoneAllowance; }
    public double getClothingAllowance() { return clothingAllowance; }
    public double getTotalAllowances()   { return totalAllowances; }
    public double getRegularPay()        { return regularPay; }
    public double getOvertimePay()       { return overtimePay; }
    public double getGrossSalary()       { return grossSalary; }
    public double getSss()               { return sss; }
    public double getPhilHealth()        { return philHealth; }
    public double getPagibig()           { return pagibig; }
    public double getWithholdingTax()    { return withholdingTax; }
    public double getTotalDeductions()   { return totalDeductions; }
    public double getNetSalary()         { return netSalary; }
}