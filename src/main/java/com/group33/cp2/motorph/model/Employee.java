package com.group33.cp2.motorph.model;

import com.group33.cp2.motorph.service.PayrollCalculator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

// Abstract base class for all employee types.
// All fields are private with validated setters. Payroll methods are abstract.
public abstract class Employee {

    private static final double MAX_BASIC_SALARY = 500_000.0;
    private static final double MAX_HOURLY_RATE = 5_000.0;

    private static final List<String> VALID_STATUSES = List.of(
            "Regular", "Probationary", "Active", "Inactive", "On Leave", "Terminated"
    );

    // ---- Personal / identity fields ----
    private String employeeID;
    private String lastName;
    private String firstName;
    private String birthday;
    private String address;
    private String phoneNumber;

    // ---- Compensation fields ----
    private double basicSalary;
    private double hourlyRate;
    private double grossSemiMonthlyRate;

    // ---- HR classification fields ----
    private String status;
    private String position;
    private String immediateSupervisor;

    // ---- Composed objects ----
    private Login login;
    private Allowance allowance;
    private GovernmentDetails governmentDetails;

    // ---- Collections ----
    private List<Attendance> attendanceList;
    private List<Payslip> payslips;

    public Employee(String employeeID, String lastName, String firstName, String birthday,
                    String address, String phoneNumber, double basicSalary, double hourlyRate,
                    double grossSemiMonthlyRate, String status, String position,
                    String immediateSupervisor, Allowance allowance,
                    GovernmentDetails governmentDetails) {
        setEmployeeID(employeeID);
        setLastName(lastName);
        setFirstName(firstName);
        this.birthday = birthday;
        setAddress(address);
        setPhoneNumber(phoneNumber);
        setBasicSalary(basicSalary);
        setHourlyRate(hourlyRate);
        this.grossSemiMonthlyRate = grossSemiMonthlyRate;
        setStatus(status);
        this.position = position;
        this.immediateSupervisor = immediateSupervisor;
        this.allowance = allowance;
        this.governmentDetails = governmentDetails;
        this.attendanceList = new ArrayList<>();
        this.payslips = new ArrayList<>();
    }

    // =========================================================================
    //  Abstract payroll methods
    // =========================================================================

    public abstract double calculateGrossSalary();
    public abstract double calculateDeductions();
    public abstract double calculateNetSalary();

    // Shared deduction helper for all subtypes subject to full statutory deductions
    // (SSS + PhilHealth + Pag-IBIG + withholding tax).
    // RegularEmployee, HR, Finance, IT, and Admin all delegate calculateDeductions() here.
    // ProbationaryEmployee does NOT call this — it omits withholding tax by design.
    protected double computeFullDeductions() {
        return PayrollCalculator.computeSSSDeduction(getBasicSalary())
             + PayrollCalculator.computePhilhealthDeduction(getBasicSalary())
             + PayrollCalculator.computePagibigDeduction(getBasicSalary())
             + PayrollCalculator.computeWithholdingTax(getBasicSalary());
    }

    // =========================================================================
    //  Getters and validated setters
    // =========================================================================

    public String getEmployeeID() { return employeeID; }

    public void setEmployeeID(String employeeID) {
        if (employeeID == null || employeeID.isBlank()) {
            throw new IllegalArgumentException("Employee ID must not be null or blank.");
        }
        this.employeeID = employeeID;
    }

    public String getLastName() { return lastName; }

    public void setLastName(String lastName) {
        if (lastName == null || lastName.isBlank()) {
            throw new IllegalArgumentException("Last name must not be null or blank.");
        }
        this.lastName = lastName;
    }

    public String getFirstName() { return firstName; }

    public void setFirstName(String firstName) {
        if (firstName == null || firstName.isBlank()) {
            throw new IllegalArgumentException("First name must not be null or blank.");
        }
        this.firstName = firstName;
    }

    public String getFullName() { return firstName + " " + lastName; }

    public String getBirthday() { return birthday; }
    public void setBirthday(String birthday) { this.birthday = birthday; }

    public String getAddress() { return address; }

    public void setAddress(String address) {
        if (address == null) {
            throw new IllegalArgumentException("Address must not be null.");
        }
        if (address.length() > 200) {
            throw new IllegalArgumentException("Address must not exceed 200 characters.");
        }
        this.address = address;
    }

    public String getPhoneNumber() { return phoneNumber; }

    public void setPhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.isBlank()) {
            throw new IllegalArgumentException("Phone number must not be null or blank.");
        }
        this.phoneNumber = phoneNumber;
    }

    public double getBasicSalary() { return basicSalary; }

    public void setBasicSalary(double basicSalary) {
        if (basicSalary <= 0 || basicSalary > MAX_BASIC_SALARY) {
            throw new IllegalArgumentException(
                    "Basic salary must be > 0 and <= " + MAX_BASIC_SALARY + ". Received: " + basicSalary);
        }
        this.basicSalary = basicSalary;
    }

    public double getHourlyRate() { return hourlyRate; }

    public void setHourlyRate(double hourlyRate) {
        if (hourlyRate <= 0 || hourlyRate > MAX_HOURLY_RATE) {
            throw new IllegalArgumentException(
                    "Hourly rate must be > 0 and <= " + MAX_HOURLY_RATE + ". Received: " + hourlyRate);
        }
        this.hourlyRate = hourlyRate;
    }

    public double getGrossSemiMonthlyRate() { return grossSemiMonthlyRate; }
    public void setGrossSemiMonthlyRate(double grossSemiMonthlyRate) {
        this.grossSemiMonthlyRate = grossSemiMonthlyRate;
    }

    public String getStatus() { return status; }

    public void setStatus(String status) {
        if (status == null || !VALID_STATUSES.contains(status)) {
            throw new IllegalArgumentException(
                    "Status must be one of " + VALID_STATUSES + ". Received: '" + status + "'");
        }
        this.status = status;
    }

    public String getPosition() { return position; }
    public void setPosition(String position) { this.position = position; }

    public String getImmediateSupervisor() { return immediateSupervisor; }
    public void setImmediateSupervisor(String immediateSupervisor) {
        this.immediateSupervisor = immediateSupervisor;
    }

    public double getAllowance() { return allowance != null ? allowance.getTotal() : 0.0; }
    public Allowance getAllowanceDetails() { return allowance; }

    public void setAllowance(double totalAllowance) {
        if (totalAllowance < 0) {
            throw new IllegalArgumentException("Allowance cannot be negative. Received: " + totalAllowance);
        }
        this.allowance = new Allowance(employeeID, totalAllowance, 0, 0);
    }

    public void setAllowance(Allowance allowance) { this.allowance = allowance; }

    public String getEmployeeNumber() { return employeeID; }

    public GovernmentDetails getGovernmentDetails() { return governmentDetails; }
    public void setGovernmentDetails(GovernmentDetails governmentDetails) {
        this.governmentDetails = governmentDetails;
    }

    public Login getLogin() { return login; }
    public void setLogin(Login login) { this.login = login; }

    public List<Attendance> getAttendanceList() {
        return Collections.unmodifiableList(attendanceList);
    }

    public void addAttendance(Attendance attendance) {
        if (attendance == null) {
            throw new IllegalArgumentException("Attendance record must not be null.");
        }
        this.attendanceList.add(attendance);
    }

    public void setAttendanceList(List<Attendance> attendanceList) {
        if (attendanceList == null) {
            throw new IllegalArgumentException("Attendance list must not be null.");
        }
        this.attendanceList = new ArrayList<>(attendanceList);
    }

    public List<Payslip> getPayslips() {
        return Collections.unmodifiableList(payslips);
    }

    public void addPayslip(Payslip payslip) {
        if (payslip == null) {
            throw new IllegalArgumentException("Payslip must not be null.");
        }
        this.payslips.add(payslip);
    }

    public void setPayslips(List<Payslip> payslips) {
        if (payslips == null) {
            throw new IllegalArgumentException("Payslip list must not be null.");
        }
        this.payslips = new ArrayList<>(payslips);
    }

    @Override
    public String toString() {
        return "employeeID='" + employeeID + '\''
                + "\n lastName='" + lastName + '\''
                + "\n firstName='" + firstName + '\''
                + "\n birthday='" + birthday + '\''
                + "\n basicSalary=" + basicSalary
                + "\n hourlyRate=" + hourlyRate
                + "\n login=" + (login != null ? login.toString() : "null")
                + "\n allowance=" + (allowance != null ? allowance.toString() : "null")
                + "\n attendanceList=" + attendanceList.toString()
                + "\n payslips=" + payslips.toString() + "\n";
    }
}
