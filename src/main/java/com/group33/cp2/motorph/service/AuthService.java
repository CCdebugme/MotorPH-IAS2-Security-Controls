package com.group33.cp2.motorph.service;

import com.group33.cp2.motorph.dao.EmployeeDetailsReader;

import java.io.IOException;
import java.sql.SQLException;

// Service facade for authentication: wraps EmployeeDetailsReader so that forms/
// never import from dao/. BCrypt hashing is the caller's responsibility.
public class AuthService {

    private final EmployeeDetailsReader employeeDetailsReader;

    public AuthService() {
        this.employeeDetailsReader = new EmployeeDetailsReader();
    }

    // Looks up a Login.csv row by username (case-insensitive).
    // Returned array: [0]=empNum [1]=username [2]=roleName [3]=bcryptHash [4]=changePassword
    public String[] getLoginDataByUsername(String username) {
        return employeeDetailsReader.getLoginDataByUsername(username);
    }

    // Updates the BCrypt hash for the employee and sets changePassword=NO.
    // Used by the forced-change flow when the employee sets their own new password.
    public boolean changeUserPassword(String empId, String newHashedPassword) throws IOException, SQLException {
        return employeeDetailsReader.changeUserPassword(empId, newHashedPassword);
    }
}
