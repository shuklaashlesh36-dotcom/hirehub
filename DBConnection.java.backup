package com.hirehub.util;

import com.hirehub.exception.DatabaseException;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Loads JDBC settings from {@code db.properties} on the classpath and opens
 * MySQL connections. Callers must close the returned connection (try-with-resources).
 */
public final class DBConnection {

    private static final String PROPERTIES_FILE = "db.properties";
    private static final String DRIVER_CLASS_NAME = "com.mysql.cj.jdbc.Driver";
    private static final Object LOCK = new Object();
    private static Properties properties;
    private static volatile boolean driverLoaded = false;

    private DBConnection() {
    }

    /**
     * Opens a new JDBC connection using DriverManager.
     *
     * @return an open connection; never null when the call succeeds
     * @throws DatabaseException if the properties file is missing, driver is missing, or MySQL cannot be reached
     */
    public static Connection getConnection() {
        loadDriver();
        Properties config = loadProperties();
        String url = required(config, "db.url");
        String username = required(config, "db.username");
        String password = config.getProperty("db.password", "");

        try {
            return DriverManager.getConnection(url, username, password);
        } catch (SQLException e) {
            throw new DatabaseException(
                    "Unable to connect to the HireHub database. "
                            + "Confirm MySQL is running, database 'hirehub' exists, "
                            + "and src/main/resources/db.properties has the correct URL, username, and password.",
                    e);
        }
    }

    private static void loadDriver() {
        if (!driverLoaded) {
            synchronized (LOCK) {
                if (!driverLoaded) {
                    try {
                        try {
                            Class.forName(DRIVER_CLASS_NAME);
                        } catch (ClassNotFoundException e) {
                            ClassLoader cl = Thread.currentThread().getContextClassLoader();
                            if (cl != null) {
                                Class.forName(DRIVER_CLASS_NAME, true, cl);
                            } else {
                                throw e;
                            }
                        }
                        driverLoaded = true;
                    } catch (ClassNotFoundException e) {
                        throw new DatabaseException(
                                "MySQL JDBC Driver class not found: " + DRIVER_CLASS_NAME
                                        + ". Ensure mysql-connector-j is included in the application classpath (WEB-INF/lib).",
                                e);
                    }
                }
            }
        }
    }

    private static Properties loadProperties() {
        synchronized (LOCK) {
            if (properties != null) {
                return properties;
            }

            ClassLoader cl = Thread.currentThread().getContextClassLoader();
            InputStream input = cl != null ? cl.getResourceAsStream(PROPERTIES_FILE) : null;
            if (input == null) {
                input = DBConnection.class.getClassLoader().getResourceAsStream(PROPERTIES_FILE);
            }

            if (input == null) {
                throw new DatabaseException(
                        "Missing classpath file db.properties. "
                                + "Copy src/main/resources/db.properties.example to "
                                + "src/main/resources/db.properties and set your local MySQL password.");
            }

            try (InputStream in = input) {
                Properties loaded = new Properties();
                loaded.load(in);
                properties = loaded;
                return properties;
            } catch (IOException e) {
                throw new DatabaseException("Failed to read db.properties from the classpath: " + e.getMessage(), e);
            }
        }
    }

    private static String required(Properties config, String key) {
        String value = config.getProperty(key);
        if (value == null || value.isBlank()) {
            throw new DatabaseException("db.properties is missing required key: " + key);
        }
        return value.trim();
    }
}
