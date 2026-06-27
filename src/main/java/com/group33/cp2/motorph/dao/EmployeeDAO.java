package com.group33.cp2.motorph.dao;

import com.group33.cp2.motorph.model.Employee;

// DAO contract for employee CRUD operations.
// Callers depend on this interface, not on a specific storage implementation.
public interface EmployeeDAO {

    boolean create(Employee employee);
    Employee read(String employeeID);
    boolean update(Employee employee);
    boolean delete(String employeeID);
}
