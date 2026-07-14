package com.group33.cp2.motorph.dao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Central database connection utility for the MotorPH Payroll System.
 *
 * <p>Credentials are loaded from {@code db.properties} on the classpath
 * (src/main/resources/db.properties) rather than being hardcoded in source
 * code. This improves security and makes it easier to switch environments
 * without recompiling.</p>
 *
 * <p>Usage — always use try-with-resources to ensure connections are closed:</p>
 * <pre>
 *   try (Connection conn = DatabaseConnection.getConnection()) {
 *       // use conn
 *   }
 * </pre>
 *
 * <p>Required keys in db.properties:</p>
 * <ul>
 *   <li>{@code db.url}      — JDBC connection URL</li>
 *   <li>{@code db.username} — database username</li>
 *   <li>{@code db.password} — database password</li>
 * </ul>
 */
public class DatabaseConnection {

    private static final Logger log = LoggerFactory.getLogger(DatabaseConnection.class);

    private static final String PROPERTIES_FILE = "db.properties";

    // Credentials loaded once at class load time from db.properties
    private static final String URL;
    private static final String USERNAME;
    private static final String PASSWORD;

    static {
        Properties props = new Properties();
        try (InputStream is = DatabaseConnection.class
                .getClassLoader()
                .getResourceAsStream(PROPERTIES_FILE)) {

            if (is == null) {
                throw new ExceptionInInitializerError(
                    "DatabaseConnection: " + PROPERTIES_FILE + " not found on classpath. "
                    + "Place it in src/main/resources/.");
            }

            props.load(is);

        } catch (IOException e) {
            log.error("DatabaseConnection: failed to load {}: {}", PROPERTIES_FILE, e.getMessage(), e);
            throw new ExceptionInInitializerError(e);
        }

        URL      = props.getProperty("db.url");
        USERNAME = props.getProperty("db.username");
        PASSWORD = props.getProperty("db.password");

        if (URL == null || USERNAME == null || PASSWORD == null) {
            throw new ExceptionInInitializerError(
                "DatabaseConnection: db.properties is missing one or more required keys "
                + "(db.url, db.username, db.password).");
        }

        log.info("DatabaseConnection: loaded configuration from {}", PROPERTIES_FILE);
    }

    // Utility class — no instances needed
    private DatabaseConnection() {}

    /**
     * Opens and returns a new JDBC connection using the credentials from db.properties.
     *
     * <p>Always call this inside a try-with-resources block so the connection
     * is automatically closed when the block exits:</p>
     * <pre>
     *   try (Connection conn = DatabaseConnection.getConnection()) {
     *       // use conn
     *   }
     * </pre>
     *
     * @return a new open {@link Connection}
     * @throws SQLException if the connection cannot be established
     */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USERNAME, PASSWORD);
    }
}
