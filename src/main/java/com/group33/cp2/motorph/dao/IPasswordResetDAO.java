package com.group33.cp2.motorph.dao;

import com.group33.cp2.motorph.model.PasswordResetRequest;
import java.sql.SQLException;
import java.util.List;

/**
 * Contract for all password reset request data access operations.
 *
 * Original implementation notes (from PasswordResetReader):
 * - Owns read/write access to the password_reset_requests table.
 * - No CSV access remains — all data comes from MySQL via JDBC.
 * - PasswordResetRequest.toArray() is used for insert to keep
 *   field ordering consistent with the table schema.
 * - approveRequest() only updates records with status = 'Pending'.
 * - getAllRequests() returns an empty list rather than throwing
 *   on SQL error, so the IT dashboard can still load.
 * - dateOfReset is formatted as "MM/dd/yyyy - hh:mm:ss a".
 */
public interface IPasswordResetDAO {

    /** Inserts a new password reset request record. */
    void addRequest(PasswordResetRequest request) throws SQLException;

    /** Returns all password reset request records. Returns an empty list on error. */
    List<PasswordResetRequest> getAllRequests();

    /**
     * Marks a pending reset request as approved and records who approved it.
     * Returns true if a pending record was found and updated.
     */
    boolean approveRequest(String employeeNumber, String adminName, String adminEmpNum)
            throws SQLException;
}
