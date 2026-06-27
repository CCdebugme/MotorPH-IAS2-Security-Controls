package com.group33.cp2.motorph.model;

import java.time.LocalDate;
import java.util.UUID;

// Holds payslip data for one employee and period. payslipID is auto-generated and read-only.
public class Payslip {

    // auto-generated at construction, no setter
    private final String payslipID;
    private String employeeID;
    private String payrollID;
    private String birthday;
    private LocalDate periodStartDate;
    private LocalDate periodEndDate;
    private double totalRegularHours;
    private double totalOvertimeHours;
    private CompensationDetails compensationDetails;
    private LocalDate issueDate;

    // Creates a Payslip; payslipID is auto-generated via UUID.
    public Payslip(String employeeID, String payrollID, String birthday,
                   LocalDate periodStartDate, LocalDate periodEndDate,
                   double totalRegularHours, double totalOvertimeHours,
                   LocalDate issueDate, CompensationDetails compensationDetails) {
        this.payslipID = UUID.randomUUID().toString();
        this.employeeID = employeeID;
        this.payrollID = payrollID;
        this.birthday = birthday;
        this.periodStartDate = periodStartDate;
        this.periodEndDate = periodEndDate;
        this.totalRegularHours = totalRegularHours;
        this.totalOvertimeHours = totalOvertimeHours;
        this.compensationDetails = compensationDetails;
        this.issueDate = issueDate;
    }

    // no-arg constructor not available; payslipID must be assigned on construction
    public String getPayslipID() { return payslipID; }
    // setPayslipID() intentionally not provided

    public String getEmployeeID() { return employeeID; }
    public void setEmployeeID(String employeeID) { this.employeeID = employeeID; }

    public String getPayrollID() { return payrollID; }
    public void setPayrollID(String payrollID) { this.payrollID = payrollID; }

    public String getBirthday() { return birthday; }
    public void setBirthday(String birthday) { this.birthday = birthday; }

    public LocalDate getPeriodStartDate() { return periodStartDate; }
    public void setPeriodStartDate(LocalDate periodStartDate) { this.periodStartDate = periodStartDate; }

    public LocalDate getPeriodEndDate() { return periodEndDate; }
    public void setPeriodEndDate(LocalDate periodEndDate) { this.periodEndDate = periodEndDate; }

    public double getTotalRegularHours() { return totalRegularHours; }
    public void setTotalRegularHours(double totalRegularHours) { this.totalRegularHours = totalRegularHours; }

    public double getTotalOvertimeHours() { return totalOvertimeHours; }
    public void setTotalOvertimeHours(double totalOvertimeHours) { this.totalOvertimeHours = totalOvertimeHours; }

    public LocalDate getIssueDate() { return issueDate; }
    public void setIssueDate(LocalDate issueDate) { this.issueDate = issueDate; }

    public CompensationDetails getCompensationDetails() { return compensationDetails; }
    public void setCompensationDetails(CompensationDetails compensationDetails) {
        this.compensationDetails = compensationDetails;
    }

    @Override
    public String toString() {
        return "\n  payslipID: '" + payslipID.substring(payslipID.length() - 5) + '\''
                + "\n  employeeID: '" + employeeID + '\''
                + "\n  birthday: " + birthday
                + "\n"
                + "\n  period: " + periodStartDate + " - " + periodEndDate
                + "\n  totalRegularHours: " + totalRegularHours
                + "\n  totalOvertimeHours: " + totalOvertimeHours
                + "\n  totalRegularPay: " + compensationDetails.getRegularPay()
                + "\n  totalOvertimePay: " + compensationDetails.getOvertimePay()
                + "\n  grossSalary: " + compensationDetails.getGrossSalary()
                + "\n"
                + "\n  deductions: " + compensationDetails.getDeductions().getTotal()
                + "\n  allowance: " + compensationDetails.getAllowance().getTotal()
                + "\n  netSalary: " + compensationDetails.getNetSalary()
                + "\n";
    }
}
