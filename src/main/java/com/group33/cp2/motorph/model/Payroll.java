package com.group33.cp2.motorph.model;

import com.group33.cp2.motorph.service.PayrollCalculator;

import java.time.LocalDate;
import java.util.UUID;

// Holds payroll data for one employee over a date range.
// Calculates hours, pay, deductions, and net salary on demand.
public class Payroll {

    private final String payrollID;
    private String employeeID;
    private LocalDate periodStartDate;
    private LocalDate periodEndDate;
    private double totalRegularHours;
    private double totalOvertimeHours;
    private boolean workHoursCalculated = false;
    private CompensationDetails compensationDetails;
    private PayrollStatus status;
    private final Employee employee;

    // Creates a payroll record; status starts as PENDING and payrollID is auto-generated.
    public Payroll(String employeeID, Employee employee, LocalDate periodStart, LocalDate periodEnd) {
        this.payrollID = UUID.randomUUID().toString();
        this.employeeID = employeeID;
        this.employee = employee;
        this.periodStartDate = periodStart;
        this.periodEndDate = periodEnd;
        this.status = PayrollStatus.PENDING;
        this.compensationDetails = new CompensationDetails(employeeID, this.payrollID);
    }

    public String getPayrollID() { return payrollID; }

    public String getEmployeeID() { return employeeID; }
    public void setEmployeeID(String employeeID) { this.employeeID = employeeID; }

    public LocalDate getPeriodStart() { return periodStartDate; }

    // OOP-4 / NPE-1: null period start causes NPE in isWithinPeriod()
    public void setPeriodStart(LocalDate periodStart) {
        if (periodStart == null) {
            throw new IllegalArgumentException("Period start date must not be null.");
        }
        this.periodStartDate = periodStart;
    }

    public LocalDate getPeriodEnd() { return periodEndDate; }

    // OOP-4 / NPE-1: null period end causes NPE in isWithinPeriod()
    public void setPeriodEnd(LocalDate periodEnd) {
        if (periodEnd == null) {
            throw new IllegalArgumentException("Period end date must not be null.");
        }
        this.periodEndDate = periodEnd;
    }

    public PayrollStatus getStatus() { return status; }
    public void setStatus(PayrollStatus status) { this.status = status; }

    public CompensationDetails getCompensationDetails() { return this.compensationDetails; }
    public void setCompensationDetails(CompensationDetails compensationDetails) {
        this.compensationDetails = compensationDetails;
    }

    // NPE-1: a null attendance date (e.g., CSV parse failure) must not crash the payroll run.
    // Returns false so the record is simply excluded from this period's calculation.
    private boolean isWithinPeriod(LocalDate date) {
        if (date == null) {
            return false;
        }
        return !date.isBefore(this.periodStartDate) && !date.isAfter(this.periodEndDate);
    }

    protected void calculateWorkHours() {
        double regularHours = 0;
        double overtimeHours = 0;

        for (Attendance attendance : this.employee.getAttendanceList()) {
            if (isWithinPeriod(attendance.getDate())) {
                regularHours += attendance.getRoundedRegularHours();
                overtimeHours += attendance.getRoundedOvertimeHours();
            }
        }

        totalRegularHours = regularHours;
        totalOvertimeHours = overtimeHours;
        workHoursCalculated = true;
    }

    public double getTotalRegularHours() {
        if (!workHoursCalculated) {
            calculateWorkHours();
        }
        return (Math.round(this.totalRegularHours * 100.0) / 100.0);
    }

    private void setTotalRegularHours(double totalRegularHours) {
        this.totalRegularHours = totalRegularHours;
    }

    public double getTotalOvertimeHours() {
        if (!workHoursCalculated) {
            calculateWorkHours();
        }
        return (Math.round(this.totalOvertimeHours * 100.0) / 100.0);
    }

    private void setTotalOvertimeHours(double totalOvertimeHours) {
        this.totalOvertimeHours = totalOvertimeHours;
    }

    public void calculateRegularPay() {
        double totalRegularPay = getTotalRegularHours() * this.employee.getHourlyRate();
        compensationDetails.setRegularPay(totalRegularPay);
    }

    public void calculateOvertimePay() {
        double totalOvertimePay = this.getTotalOvertimeHours() * this.employee.getHourlyRate() * 1.25;
        compensationDetails.setOvertimePay(totalOvertimePay);
    }

    public void calculateGrossSalary() {
        calculateWorkHours();
        calculateRegularPay();
        calculateOvertimePay();
        double grossSalary = compensationDetails.getRegularPay() + compensationDetails.getOvertimePay();
        compensationDetails.setGrossSalary(grossSalary);
    }

    // Statutory deductions (SSS, PhilHealth, PagIBIG, withholding tax) are salary-based,
    // not hours-based; they are applied regardless of whether the employee has attendance
    // records in the period.
    public void calculateDeductions(PeriodType periodType) {
        Deductions deductions;
        if (periodType.equals(PeriodType.MONTHLY)) {
            deductions = PayrollCalculator.getDeductionsMonthly(employeeID, employee.getBasicSalary());
        } else {
            deductions = PayrollCalculator.getDeductionsBiWeekly(employeeID, employee.getBasicSalary(), periodStartDate);
        }
        compensationDetails.setDeductions(deductions);
    }

    // pro-rates allowances: WEEKLY /4, BIWEEKLY /2, MONTHLY /1
    // Allowances are fixed monthly benefits (rice, phone, clothing); they are not
    // attendance-dependent and must always be applied regardless of hours worked.
    public void calculateAllowances(PeriodType periodType) {
        if (this.employee.getAllowanceDetails() == null) {
            compensationDetails.setAllowance(new Allowance());
            return;
        }
        int divisor = 1;
        if (periodType == PeriodType.WEEKLY) {
            divisor = 4;
        } else if (periodType == PeriodType.BIWEEKLY) {
            divisor = 2;
        }
        double riceAllowance = this.employee.getAllowanceDetails().getRiceAllowance() / divisor;
        double phoneAllowance = this.employee.getAllowanceDetails().getPhoneAllowance() / divisor;
        double clothingAllowance = this.employee.getAllowanceDetails().getClothingAllowance() / divisor;
        compensationDetails.setAllowance(new Allowance(employeeID, riceAllowance, phoneAllowance, clothingAllowance));
    }

    // net = gross − deductions + allowances; marks payroll as PROCESSED.
    // Delegates to the period-aware overload using MONTHLY as the default.
    // Kept for backward compatibility — all existing call sites continue to work unchanged.
    public void calculateNetSalary() {
        calculateNetSalary(PeriodType.MONTHLY);
    }

    // Period-aware overload: applies deductions and allowances scaled to the given period type.
    // Use PeriodType.BIWEEKLY for half-month payroll runs; PeriodType.MONTHLY for full-month runs.
    public void calculateNetSalary(PeriodType periodType) {
        this.calculateGrossSalary();
        // No attendance data for this period → gross = 0 → no earnings, no deductions, net = 0.
        // Applying deductions against a ₱0 gross produces a negative net (P1-D-3).
        if (compensationDetails.getGrossSalary() == 0.0) {
            compensationDetails.setDeductions(new Deductions());
            compensationDetails.setAllowance(new Allowance());
            compensationDetails.setNetSalary(0.0);
            setStatus(PayrollStatus.PROCESSED);
            return;
        }
        this.calculateDeductions(periodType);
        this.calculateAllowances(periodType);
        double netSalary = (compensationDetails.getGrossSalary()
                - compensationDetails.getDeductions().getTotal())
                + compensationDetails.getAllowance().getTotal();
        compensationDetails.setNetSalary(netSalary);
        setStatus(PayrollStatus.PROCESSED);
    }

    public void process() {
        status = PayrollStatus.PROCESSED;
    }

    public void revertToPending() {
        status = PayrollStatus.PENDING;
    }

    public Payslip generatePayslip() {
        return new Payslip(
                this.employeeID,
                this.payrollID,
                this.employee.getBirthday(),
                this.periodStartDate,
                this.periodEndDate,
                this.getTotalRegularHours(),
                this.getTotalOvertimeHours(),
                LocalDate.now(),
                compensationDetails
        );
    }

    @Override
    public String toString() {
        return "Payroll {"
                + "\n  payrollID='" + payrollID + '\''
                + ",\n  employeeID='" + employeeID + '\''
                + ",\n  periodStartDate=" + periodStartDate
                + ",\n  periodEndDate=" + periodEndDate
                + ",\n  totalRegularHours=" + totalRegularHours
                + ",\n  totalOvertimeHours=" + totalOvertimeHours
                + ",\n  totalRegularPay=" + compensationDetails.getRegularPay()
                + ",\n  totalOvertimePay=" + compensationDetails.getOvertimePay()
                + ",\n  grossSalary=" + compensationDetails.getGrossSalary()
                + ",\n  totalAllowance=" + compensationDetails.getAllowance().getTotal()
                + ",\n  totalDeductions=" + compensationDetails.getDeductions().getTotal()
                + ",\n  netSalary=" + compensationDetails.getNetSalary()
                + ",\n  status=" + status
                + "\n}";
    }
}
