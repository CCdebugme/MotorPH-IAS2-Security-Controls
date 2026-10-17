package com.group33.cp2.motorph.service;

import com.group33.cp2.motorph.dao.DatabaseConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// Lockout after repeated failed logins, plus audit logging of every attempt.
public class LoginAttemptService {

    public static final int MAX_FAILED_ATTEMPTS = 3;
    public static final int COOLDOWN_MINUTES = 2; // short for testing; raise for real use

    private static final Logger AUDIT = LoggerFactory.getLogger("AUDIT");

    // Seconds left on the lock, or 0 if the account is not locked.
    // An expired lock is cleared here (the "unlock" half of the control).
    public long getRemainingLockSeconds(String username) throws SQLException {
        String sql = "SELECT is_locked, TIMESTAMPDIFF(SECOND, NOW(), "
                + "DATE_ADD(lockout_timestamp, INTERVAL ? MINUTE)) AS remaining "
                + "FROM login WHERE username = ?";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, COOLDOWN_MINUTES);
            ps.setString(2, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next() || !rs.getBoolean("is_locked")) {
                    return 0;
                }
                long remaining = rs.getLong("remaining");
                if (remaining > 0) {
                    AUDIT.warn("BLOCKED_ATTEMPT user={} remainingSeconds={}", clean(username), remaining);
                    return remaining;
                }
            }
        }
        resetCounter(username);
        AUDIT.info("LOCKOUT_EXPIRED user={}", clean(username));
        return 0;
    }

    // Called on a wrong password for an existing user.
    // Best effort: if the DB write fails, log the error and keep going.
    public void recordFailure(String username) {
        // MySQL evaluates SET left to right, so the increment must come last.
        String update = "UPDATE login SET "
                + "is_locked = CASE WHEN failed_attempts + 1 >= ? THEN 1 ELSE is_locked END, "
                + "lockout_timestamp = CASE WHEN failed_attempts + 1 >= ? THEN NOW() ELSE lockout_timestamp END, "
                + "failed_attempts = failed_attempts + 1 WHERE username = ?";
        String read = "SELECT failed_attempts FROM login WHERE username = ?";
        try (Connection c = DatabaseConnection.getConnection()) {
            try (PreparedStatement ps = c.prepareStatement(update)) {
                ps.setInt(1, MAX_FAILED_ATTEMPTS);
                ps.setInt(2, MAX_FAILED_ATTEMPTS);
                ps.setString(3, username);
                ps.executeUpdate();
            }
            try (PreparedStatement ps = c.prepareStatement(read)) {
                ps.setString(1, username);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        int attempts = rs.getInt(1);
                        AUDIT.warn("LOGIN_FAILED user={} attempts={}/{}", clean(username), attempts, MAX_FAILED_ATTEMPTS);
                        if (attempts >= MAX_FAILED_ATTEMPTS) {
                            AUDIT.warn("LOCKOUT_TRIGGERED user={} cooldownMinutes={}", clean(username), COOLDOWN_MINUTES);
                        }
                    }
                }
            }
        } catch (SQLException e) {
            AUDIT.error("Could not record failed login for user={}", clean(username), e);
        }
    }

    // Called when the password is correct: reset the counter and log it.
    public void recordSuccess(String username) {
        try {
            resetCounter(username);
        } catch (SQLException e) {
            AUDIT.error("Could not reset login counter for user={}", clean(username), e);
        }
        AUDIT.info("LOGIN_SUCCESS user={}", clean(username));
    }

    // Username does not exist: nothing to lock, but the attempt is still logged.
    public void recordUnknownUser(String username) {
        AUDIT.warn("LOGIN_FAILED user={} reason=unknown_user", clean(username));
    }

    private void resetCounter(String username) throws SQLException {
        String sql = "UPDATE login SET failed_attempts = 0, is_locked = 0, lockout_timestamp = NULL WHERE username = ?";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.executeUpdate();
        }
    }

    // Strip line breaks so a typed username can't forge extra log lines.
    private static String clean(String s) {
        return s == null ? "" : s.replaceAll("[\\r\\n]", "_");
    }
}