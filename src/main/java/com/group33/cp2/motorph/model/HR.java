package com.group33.cp2.motorph.model;

import com.group33.cp2.motorph.service.EmployeeService;
import com.group33.cp2.motorph.service.LeaveService;

import java.io.IOException;
import java.sql.SQLException;

// HR department employee: full payroll treatment plus leave-management operations.
// Overtime at 1.25x; all four deductions apply.
public class HR extends Employee implements PayrollCalculable, HROperations {

    // Lazily initialized to avoid circular instantiation with EmployeeService.reloadEmployees().
    // Never create EmployeeService inside the HR constructor — use getEmployeeService() instead.
    private EmployeeService employeeService;

    // Returns the shared EmployeeService, creating it on first access.
    // Prevents infinite recursion: reloadEmployees() creates HR, HR must not re-create the service.
    private EmployeeService getEmployeeService() {
        if (employeeService == null) {
            employeeService = new EmployeeService();
        }
        return employeeService;
    }

    // Lazily initialized — LeaveService is not needed at construction time.
    // Keeping it lazy is consistent with the EmployeeService pattern and avoids
    // unnecessary CSV I/O during object creation.
    private LeaveService leaveService;

    // Returns the shared LeaveService, creating it on first access.
    private LeaveService getLeaveService() {
        if (leaveService == null) {
            leaveService = new LeaveService();
        }
        return leaveService;
    }

    public HR(String employeeID, String lastName, String firstName, String birthday,
              String address, String phoneNumber, double basicSalary, double hourlyRate,
              double grossSemiMonthlyRate, String status, String position,
              String immediateSupervisor, Allowance allowance,
              GovernmentDetails governmentDetails) {
        super(employeeID, lastName, firstName, birthday, address, phoneNumber,
              basicSalary, hourlyRate, grossSemiMonthlyRate, status, position,
              immediateSupervisor, allowance, governmentDetails);
    }

    public String getDepartment() { return "HR Department"; }

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

    // Delegates to LeaveService so this model class has no direct dao/ imports.
    // LeaveService owns the DAO orchestration: fetch request, approve, persist, deduct balance.
    @Override
    public boolean approveLeave(String leaveId, String remark) {
        try {
            return getLeaveService().approveLeave(leaveId, getFullName(), remark);
        } catch (IOException | SQLException e) {
            System.err.println("HR.approveLeave failed for leaveId=" + leaveId + ": " + e.getMessage());
            return false;
        }
    }

    // Delegates to LeaveService so this model class has no direct dao/ imports.
    // LeaveService owns the DAO orchestration: fetch request, reject, persist.
    @Override
    public boolean rejectLeave(String leaveId, String remark) {
        try {
            return getLeaveService().rejectLeave(leaveId, getFullName(), remark);
        } catch (IOException | SQLException e) {
            System.err.println("HR.rejectLeave failed for leaveId=" + leaveId + ": " + e.getMessage());
            return false;
        }
    }

    @Override
    public Employee viewEmployeeRecords(String employeeId) {
        return getEmployeeService().getEmployeeById(employeeId);
    }

    // Delegates delete to EmployeeService via the lazy accessor to avoid circular construction.
    @Override
    public boolean deleteEmployee(String empId) {
        return getEmployeeService().deleteEmployee(empId);
    }

    @Override
    public String toString() {
        return "HR {" + super.toString() + "}";
    }
}
