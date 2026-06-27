package com.group33.cp2.motorph.model;

import java.time.LocalDate;

// Interface for Admin-specific operations: user management and system reports.
public interface AdminOperations {

    boolean manageUsers(int userId, String action);
    Report generateSystemReport(String reportType);

    // Date-range overload — polymorphism / method overloading demonstration.
    Report generateSystemReport(String reportType, LocalDate start, LocalDate end);

    // Deletes the employee with the given ID from all CSV stores.
    boolean deleteEmployee(String empId);
}
