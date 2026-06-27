package com.group33.cp2.motorph.model;

// Holds pay breakdown for one payroll record: regular pay, overtime, gross/net salary,
// allowances, and deductions.
public class CompensationDetails {

    private String employeeID;
    private String payrollID;
    private double regularPay;
    private double overtimePay;
    private double grossSalary;
    private double netSalary;
    private Allowance allowance;
    private Deductions deductions;

    // Creates a CompensationDetails with IDs only; allowance and deductions default to zero.
    public CompensationDetails(String employeeID, String payrollID) {
        this.employeeID = employeeID;
        this.payrollID = payrollID;
        this.allowance = new Allowance();
        this.deductions = new Deductions();
    }

    public CompensationDetails(String employeeID, double regularPay, double overtimePay,
            double grossSalary, double netSalary,
            Allowance allowance, Deductions deductions) {
        this.employeeID = employeeID;
        this.regularPay = regularPay;
        this.overtimePay = overtimePay;
        this.grossSalary = grossSalary;
        this.netSalary = netSalary;
        this.allowance = allowance;
        this.deductions = deductions;
    }

    public String getEmployeeID() { return employeeID; }
    public void setEmployeeID(String employeeID) { this.employeeID = employeeID; }

    public String getPayrollID() { return payrollID; }
    public void setPayrollID(String payrollID) { this.payrollID = payrollID; }

    public double getRegularPay() { return regularPay; }
    public void setRegularPay(double regularPay) { this.regularPay = regularPay; }

    public double getOvertimePay() { return overtimePay; }
    public void setOvertimePay(double overtimePay) { this.overtimePay = overtimePay; }

    public double getGrossSalary() { return grossSalary; }
    public void setGrossSalary(double grossSalary) { this.grossSalary = grossSalary; }

    public double getNetSalary() { return netSalary; }
    public void setNetSalary(double netSalary) { this.netSalary = netSalary; }

    public Allowance getAllowance() { return allowance; }
    public void setAllowance(Allowance allowance) { this.allowance = allowance; }

    public Deductions getDeductions() { return deductions; }
    public void setDeductions(Deductions deductions) { this.deductions = deductions; }
}
