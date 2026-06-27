package com.group33.cp2.motorph.service;

import com.group33.cp2.motorph.dao.EmployeeDetailsReader;
import com.group33.cp2.motorph.dao.PasswordResetReader;
import com.group33.cp2.motorph.model.PasswordResetRequest;
import org.mindrot.jbcrypt.BCrypt;
import java.sql.SQLException;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Random;

// Generates a temp password, BCrypt-hashes it, writes to Login.csv (changePassword=YES),
// and marks the reset request as Approved in Password_Reset_Requests.csv.
public class PasswordResetService {

    private static final String SPECIAL_CHARS = "!@#$%^&*";

    // Named employeeReader because it handles both Employee.csv (via isEmployeeValid)
    // and Login.csv (via changeUserPassword) — EmployeeDetailsReader owns both files.
    private final EmployeeDetailsReader employeeReader = new EmployeeDetailsReader();

    // Resets the employee's password and marks the pending request as Approved.
    // Returns the plaintext temporary password so the caller can display it to IT staff.
    // Throws PasswordResetException if the employee is not found or no pending request exists.
    public String resetPassword(String employeeNumber, String adminName, String adminEmpNum)
            throws IOException, SQLException, PasswordResetException {

        String tempPassword = generateTemporaryPassword(employeeNumber);
        // BCrypt-hash the temp password; changePassword=YES forces a change on next login
        String hashed = BCrypt.hashpw(tempPassword, BCrypt.gensalt());

        if (!employeeReader.changeUserPassword(employeeNumber, hashed, "YES")) {
            throw new PasswordResetException("Employee not found in login data: " + employeeNumber);
        }

        if (!PasswordResetReader.approveRequest(employeeNumber, adminName, adminEmpNum)) {
            throw new PasswordResetException(
                    "Password reset request not found or already approved for: " + employeeNumber);
        }

        // Return plaintext so the caller (ResetPasswordProcessor) can relay it to IT staff.
        return tempPassword;
    }

    // Returns all password reset requests from the CSV.
    // Delegates to PasswordResetReader so forms/ never import from dao/.
    public List<PasswordResetRequest> getAllRequests() {
        return PasswordResetReader.getAllRequests();
    }

    // Validates the employee (empNum + empName in "FirstName LastName" order) and submits
    // a new Pending reset request to Password_Reset_Requests.csv.
    // Throws PasswordResetException if the combination is not found in Employee.csv.
    public void submitResetRequest(String empNum, String empName)
        throws PasswordResetException, IOException, SQLException {

        if (!employeeReader.isEmployeeValid(empNum, empName)) {
            throw new PasswordResetException(
                    "No employee found matching number '" + empNum + "' and name '" + empName + "'.");
        }

        String dateOfRequest = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        PasswordResetRequest request = new PasswordResetRequest(empNum, empName, dateOfRequest);
        PasswordResetReader.addRequest(request);
    }

    // Generates a temporary password: "Default" + empNum + randomSpecialChar + twoRandomDigits
    private String generateTemporaryPassword(String employeeNumber) {
        Random random = new Random();
        char   specialChar   = SPECIAL_CHARS.charAt(random.nextInt(SPECIAL_CHARS.length()));
        String twoDigitNum   = String.format("%02d", random.nextInt(100));
        return "Default" + employeeNumber + specialChar + twoDigitNum;
    }
}
