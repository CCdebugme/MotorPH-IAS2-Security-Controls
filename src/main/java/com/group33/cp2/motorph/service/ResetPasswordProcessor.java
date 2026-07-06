package com.group33.cp2.motorph.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;
import java.sql.SQLException;

// Orchestrates the password reset workflow: calls PasswordResetService and invokes the
// PasswordResetCallback on success. Uses the callback interface to avoid a circular
// dependency between the service and GUI packages.
// Dialog display is the caller's responsibility — this class has no javax.swing imports.
// After calling resetPassword(), the caller should check isLastResetSuccessful() and
// getLastResultMessage() to display an appropriate dialog.
public class ResetPasswordProcessor {

    private static final Logger log = LoggerFactory.getLogger(ResetPasswordProcessor.class);

    private final PasswordResetService passwordResetService;

    // Stores the human-readable outcome of the most recent resetPassword() call.
    // The calling form reads this to display an appropriate dialog.
    private String lastResultMessage = "";

    // True if the most recent resetPassword() call succeeded; false otherwise.
    private boolean lastResetSuccessful = false;

    // Stores the plaintext temporary password from the most recent successful reset.
    // Empty string if the last call failed or resetPassword() has not been called yet.
    private String lastTempPassword = "";

    public ResetPasswordProcessor(PasswordResetService passwordResetService) {
        this.passwordResetService = passwordResetService;
    }

    // Returns the human-readable result message from the most recent resetPassword() call.
    // Empty string if resetPassword() has not been called yet.
    public String getLastResultMessage() {
        return lastResultMessage;
    }

    // Returns true if the most recent resetPassword() call completed without error.
    public boolean isLastResetSuccessful() {
        return lastResetSuccessful;
    }

    // Returns the plaintext temporary password from the most recent successful reset.
    // Empty string if the last call failed or resetPassword() has not been called yet.
    // The calling form should display this to IT staff so they can communicate it to the employee.
    public String getLastTempPassword() {
        return lastTempPassword;
    }

    // Resets the password and invokes onSuccess if the operation succeeds.
    // Sets lastResultMessage, lastResetSuccessful, and lastTempPassword so the calling form
    // can display an appropriate dialog (including the temp password) without this class
    // importing javax.swing.
    // Returns true on success, false on any error.
    public boolean resetPassword(String employeeNumber, String adminName,
                                  String adminEmpNum, PasswordResetCallback onSuccess) {
        try {
            String tempPassword = passwordResetService.resetPassword(employeeNumber, adminName, adminEmpNum);
            lastTempPassword = tempPassword;
            lastResultMessage = "Password reset successful for employee " + employeeNumber + ".\n\n"
                    + "Temporary password: " + tempPassword + "\n\n"
                    + "Please communicate this to the employee securely.\n"
                    + "The employee will be required to change it on next login.";
            lastResetSuccessful = true;
            onSuccess.onPasswordResetComplete();
            return true;
        } catch (PasswordResetException e) {
            lastTempPassword = "";
            lastResultMessage = "Reset failed: " + e.getMessage();
            lastResetSuccessful = false;
            log.warn("ResetPasswordProcessor: business rule violation — {}", e.getMessage());
            return false;
        } catch (IOException | SQLException e) {
            lastTempPassword = "";
            lastResultMessage = "I/O error during reset: " + e.getMessage();
            lastResetSuccessful = false;
            log.error("ResetPasswordProcessor: I/O error — {}", e.getMessage(), e);
            return false;
        }
    }
}
