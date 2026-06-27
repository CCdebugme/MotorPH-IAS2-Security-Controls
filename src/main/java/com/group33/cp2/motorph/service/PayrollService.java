package com.group33.cp2.motorph.service;

import com.group33.cp2.motorph.dao.PayrollDAO;
import com.group33.cp2.motorph.model.Payroll;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

// In-memory implementation of PayrollDAO. Records exist only for the application session.
// Implements PayrollDAO so the persistence strategy can be swapped without changing callers.
public class PayrollService implements PayrollDAO {

    // In-memory store of all payroll records for the current session.
    private final List<Payroll> payrollRecords = new ArrayList<>();

    @Override
    public boolean create(Payroll payroll) {
        if (payroll == null) {
            return false;
        }
        payrollRecords.add(payroll);
        return true;
    }

    @Override
    public Payroll read(String payrollID) {
        if (payrollID == null || payrollID.isBlank()) {
            return null;
        }
        for (Payroll payroll : payrollRecords) {
            if (payroll.getPayrollID().equals(payrollID)) {
                return payroll;
            }
        }
        return null;
    }

    @Override
    public boolean update(Payroll payroll) {
        if (payroll == null) {
            return false;
        }
        for (int i = 0; i < payrollRecords.size(); i++) {
            if (payrollRecords.get(i).getPayrollID().equals(payroll.getPayrollID())) {
                payrollRecords.set(i, payroll);
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean delete(String payrollID) {
        if (payrollID == null || payrollID.isBlank()) {
            return false;
        }
        return payrollRecords.removeIf(p -> p.getPayrollID().equals(payrollID));
    }

    // Returns a read-only view of all payroll records in the current session.
    public List<Payroll> getAllPayrolls() {
        return Collections.unmodifiableList(payrollRecords);
    }

    // Returns all payroll records for a given employee ID; empty list if none found.
    public List<Payroll> getPayrollsByEmployeeId(String employeeID) {
        if (employeeID == null || employeeID.isBlank()) {
            return Collections.emptyList();
        }
        List<Payroll> result = new ArrayList<>();
        for (Payroll payroll : payrollRecords) {
            if (payroll.getEmployeeID().equals(employeeID)) {
                result.add(payroll);
            }
        }
        return Collections.unmodifiableList(result);
    }
}
