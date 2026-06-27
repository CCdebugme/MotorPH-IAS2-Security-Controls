package com.group33.cp2.motorph.service;

import com.group33.cp2.motorph.dao.AllowanceDetailsReader;
import com.group33.cp2.motorph.dao.SalaryDetailsReader;
import com.group33.cp2.motorph.model.Allowance;
import com.group33.cp2.motorph.model.Salary;
import com.group33.cp2.motorph.model.SalaryDetails;

import java.io.IOException;

// Service that assembles a complete SalaryDetails record for a single employee.
// Reads CSVs, delegates deduction math to PayrollCalculator, returns one immutable result.
// Callers have no knowledge of CSV layouts or deduction formulas.
public class PayrollCalculatorService {

    private final SalaryDetailsReader salaryReader;
    private final AllowanceDetailsReader allowanceReader;

    public PayrollCalculatorService() {
        this.salaryReader   = new SalaryDetailsReader();
        this.allowanceReader = new AllowanceDetailsReader();
    }

    // Returns a full payroll breakdown for the employee, or null if salary/allowance not found.
    public SalaryDetails getSalaryDetails(String employeeId) throws IOException {
        Salary salary      = salaryReader.getSalary(employeeId);
        Allowance allowance = allowanceReader.getAllowance(employeeId);

        if (salary == null || allowance == null) {
            return null;
        }

        double basicSalary    = salary.getBasicSalary();
        double hourlyRate     = salary.getHourlyRate();

        double riceSubsidy    = allowance.getRiceAllowance();
        double phoneAllow     = allowance.getPhoneAllowance();
        double clothingAllow  = allowance.getClothingAllowance();
        double totalAllowances = allowance.getTotal();

        double grossSalary    = basicSalary + totalAllowances;

        double sss            = PayrollCalculator.computeSSSDeduction(basicSalary);
        double philHealth     = PayrollCalculator.computePhilhealthDeduction(basicSalary);
        double pagibig        = PayrollCalculator.computePagibigDeduction(basicSalary);
        double tax            = PayrollCalculator.computeWithholdingTax(basicSalary);

        double totalDeductions = sss + philHealth + pagibig + tax;
        double netSalary       = grossSalary - totalDeductions;

        return new SalaryDetails(
            grossSalary, netSalary, hourlyRate,
            riceSubsidy, phoneAllow, clothingAllow, totalAllowances,
            pagibig, philHealth, sss, tax, totalDeductions
        );
    }

    // Same deduction math as getSalaryDetails(empId); month and year are metadata for logging only.
    // Validates that month is 1–12 and year is a positive 4-digit value.
    // Returns null if salary or allowance data is not found.
    public SalaryDetails getSalaryDetailsForPeriod(String employeeId, int month, int year) throws IOException {
        if (month < 1 || month > 12) {
            throw new IllegalArgumentException("month must be 1–12, got: " + month);
        }
        if (year < 1000 || year > 9999) {
            throw new IllegalArgumentException("year must be a 4-digit value, got: " + year);
        }
        // Deduction math is identical to getSalaryDetails; period metadata is carried by the caller.
        return getSalaryDetails(employeeId);
    }
}
