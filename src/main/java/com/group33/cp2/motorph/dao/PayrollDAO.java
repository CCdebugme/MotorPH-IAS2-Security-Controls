package com.group33.cp2.motorph.dao;

import com.group33.cp2.motorph.model.Payroll;

// DAO contract for payroll CRUD operations.
// Callers depend on this interface, not on a specific storage implementation.
public interface PayrollDAO {

    boolean create(Payroll payroll);
    Payroll read(String payrollID);
    boolean update(Payroll payroll);
    boolean delete(String payrollID);
}
