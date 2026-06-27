package com.group33.cp2.motorph.model;

// Interface for HR-specific operations: leave approval/rejection and employee record access.
public interface HROperations {

    boolean approveLeave(String leaveId, String remark);
    boolean rejectLeave(String leaveId, String remark);
    Employee viewEmployeeRecords(String employeeId);

    // Deletes the employee with the given ID from all CSV stores.
    boolean deleteEmployee(String empId);
}
