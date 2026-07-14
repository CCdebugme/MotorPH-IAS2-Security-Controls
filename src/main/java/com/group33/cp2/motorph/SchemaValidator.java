package com.group33.cp2.motorph.util;

import com.group33.cp2.motorph.dao.DatabaseConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Validates that all required database tables exist before the application starts.
 *
 * <p>Called once during startup from {@code Main.java}. If any required table
 * is missing, the application logs a clear error message listing the missing
 * tables and exits immediately rather than failing later with a cryptic
 * {@code SQLException} during normal operation.</p>
 *
 * <p>This satisfies the professor's requirement to detect configuration issues
 * early and provide meaningful diagnostic output.</p>
 */
public class SchemaValidator {

    private static final Logger log = LoggerFactory.getLogger(SchemaValidator.class);

    /**
     * All tables required by the MotorPH Payroll System.
     * If any of these are missing the application cannot function correctly.
     */
    private static final List<String> REQUIRED_TABLES = Arrays.asList(
        "employees",
        "login",
        "salary",
        "allowance",
        "attendance",
        "payroll",
        "payslips",
        "leave_requests",
        "leave_balances",
        "password_reset_requests"
    );

    // Utility class — no instances needed
    private SchemaValidator() {}

    /**
     * Checks that all required tables exist in the connected database.
     *
     * <p>Logs a warning for each missing table and throws an
     * {@link IllegalStateException} if any tables are absent, which causes
     * the application to exit at startup with a clear diagnostic message.</p>
     *
     * @throws IllegalStateException if one or more required tables are missing
     * @throws SQLException          if the database connection or metadata query fails
     */
    public static void validate() throws SQLException {
        log.info("SchemaValidator: checking required database tables...");

        List<String> missingTables = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection()) {
            DatabaseMetaData meta = conn.getMetaData();

            for (String table : REQUIRED_TABLES) {
                try (ResultSet rs = meta.getTables(null, null, table, new String[]{"TABLE"})) {
                    if (!rs.next()) {
                        missingTables.add(table);
                        log.warn("SchemaValidator: required table '{}' is MISSING.", table);
                    }
                }
            }
        }

        if (!missingTables.isEmpty()) {
            String message = "SchemaValidator: startup aborted — the following required tables "
                + "are missing from the database: " + missingTables + ". "
                + "Please run the SQL setup script and verify the database configuration "
                + "in src/main/resources/db.properties.";
            log.error(message);
            throw new IllegalStateException(message);
        }

        log.info("SchemaValidator: all {} required tables verified successfully.",
                REQUIRED_TABLES.size());
    }
}
