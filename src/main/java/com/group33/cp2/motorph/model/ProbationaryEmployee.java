package com.group33.cp2.motorph.model;

import com.group33.cp2.motorph.service.PayrollCalculator;

// Probationary employee: no overtime pay; deductions are SSS, PhilHealth,
// and Pag-IBIG only (no withholding tax).
public class ProbationaryEmployee extends Employee implements PayrollCalculable {

    public ProbationaryEmployee(String employeeID, String lastName, String firstName, String birthday,
                                String address, String phoneNumber, double basicSalary, double hourlyRate,
                                double grossSemiMonthlyRate, String status, String position,
                                String immediateSupervisor, Allowance allowance,
                                GovernmentDetails governmentDetails) {
        super(employeeID, lastName, firstName, birthday, address, phoneNumber,
              basicSalary, hourlyRate, grossSemiMonthlyRate, status, position,
              immediateSupervisor, allowance, governmentDetails);
    }

    @Override
    public double calculateGrossSalary() {
        return getBasicSalary() + getAllowance();
    }

    @Override
    public double calculateGrossSalary(double salary) {
        if (salary <= 0) {
            throw new IllegalArgumentException("Salary must be > 0. Received: " + salary);
        }
        return salary;
    }

    @Override
    public double calculateGrossSalary(double salary, double overtimeHours) {
        if (salary <= 0) {
            throw new IllegalArgumentException("Salary must be > 0. Received: " + salary);
        }
        return salary; // overtimeHours intentionally not applied
    }

    @Override
    public double calculateOvertimePay(double overtimeHours) {
        return 0.0;
    }

    @Override
    public double calculateDeductions() {
        return PayrollCalculator.computeSSSDeduction(getBasicSalary())
             + PayrollCalculator.computePhilhealthDeduction(getBasicSalary())
             + PayrollCalculator.computePagibigDeduction(getBasicSalary());
    }

    @Override
    public double calculateNetSalary() {
        return calculateGrossSalary() - calculateDeductions();
    }

    @Override
    public String toString() {
        return "ProbationaryEmployee {" + super.toString() + "}";
    }
}
