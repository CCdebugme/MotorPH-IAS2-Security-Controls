package com.group33.cp2.motorph.model;

import com.group33.cp2.motorph.service.PasswordResetException;
import com.group33.cp2.motorph.service.PasswordResetService;
import java.sql.SQLException;
import java.io.IOException;


// IT department employee: full payroll treatment plus system access-management operations.
// Overtime at 1.25x; all four deductions apply.
public class IT extends Employee implements PayrollCalculable, ITOperations {

    public IT(String employeeID, String lastName, String firstName, String birthday,
              String address, String phoneNumber, double basicSalary, double hourlyRate,
              double grossSemiMonthlyRate, String status, String position,
              String immediateSupervisor, Allowance allowance,
              GovernmentDetails governmentDetails) {
        super(employeeID, lastName, firstName, birthday, address, phoneNumber,
              basicSalary, hourlyRate, grossSemiMonthlyRate, status, position,
              immediateSupervisor, allowance, governmentDetails);
    }

    public String getDepartment() { return "IT Department"; }

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
        if (overtimeHours < 0) {
            throw new IllegalArgumentException("Overtime hours must be >= 0. Received: " + overtimeHours);
        }
        return salary + (overtimeHours * getHourlyRate() * 1.25);
    }

    @Override
    public double calculateOvertimePay(double overtimeHours) {
        return overtimeHours * getHourlyRate() * 1.25;
    }

    @Override
    public double calculateDeductions() {
        return computeFullDeductions();
    }

    @Override
    public double calculateNetSalary() {
        return calculateGrossSalary() - calculateDeductions();
    }

    @Override
    public boolean manageSystemAccess(int userId, String accessLevel) {
        if ("reset".equalsIgnoreCase(accessLevel)) {
            return resetPassword(String.valueOf(userId));
        }
        return false;
    }

    public boolean resetPassword(String employeeId) {
        try {
            PasswordResetService service = new PasswordResetService();
            // resetPassword() now returns the plaintext temp password on success;
            // non-null return indicates success — temp password is not used here
            // because the GUI path (ITDashboard → ResetPasswordProcessor) is
            // the display-capable caller. This path is used for programmatic/headless resets.
            String tempPassword = service.resetPassword(employeeId, getFullName(), getEmployeeID());
            return tempPassword != null;
        } catch (PasswordResetException e) {
            System.err.println("IT.resetPassword: business rule violation — " + e.getMessage());
            return false;
        }  catch (IOException | SQLException e) {
            System.err.println("IT.resetPassword: I/O error — " + e.getMessage());
            return false;
        }
    }

    @Override
    public String toString() {
        return "IT {" + super.toString() + "}";
    }
}
