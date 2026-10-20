
package com.hirehub.util;

import com.hirehub.exception.DatabaseException;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public final class DBConnection {

    private static final String PROPERTIES_FILE = "db.properties";
    private static final String DRIVER_CLASS_NAME =
            "com.mysql.cj.jdbc.Driver";
    private static final Object LOCK = new Object();

    private static Properties properties;
    private static volatile boolean driverLoaded = false;

    private DBConnection() {
    }

    public static Connection getConnection() {
        loadDriver();

        String url = System.getenv("DB_URL");
        String username = System.getenv("DB_USER");
        String password = System.getenv("DB_PASSWORD");

        // Use individual database environment variables if DB_URL is absent.
        if (url == null || url.isBlank()) {
            String host = System.getenv("DB_HOST");
            String port = System.getenv("DB_PORT");
            String database = System.getenv("DB_NAME");

            if (host != null && !host.isBlank()
                    && port != null && !port.isBlank()
                    && database != null && !database.isBlank()) {

                String sslMode = System.getenv("DB_SSL_MODE");

                if (sslMode == null || sslMode.isBlank()) {
                    sslMode = "REQUIRED";
                }

                url = "jdbc:mysql://" + host + ":" + port + "/"
                        + database + "?sslMode=" + sslMode;
            }
        }

        // Fall back to db.properties for local development.
        if (url == null || url.isBlank()
                || username == null || username.isBlank()
                || password == null) {

            Properties config = loadProperties();

            if (url == null || url.isBlank()) {
                url = required(config, "db.url");
            }

            if (username == null || username.isBlank()) {
                username = required(config, "db.username");
            }

            if (password == null) {
                password = config.getProperty("db.password", "");
            }
        }

        try {
            return DriverManager.getConnection(url, username, password);

        } catch (SQLException e) {
            // Log the real SQL error in Render application logs.
            System.err.println(
                    "HireHub database connection failed: "
                            + e.getClass().getSimpleName()
                            + ": " + e.getMessage()
            );

            e.printStackTrace(System.err);

            throw new DatabaseException(
                    "Unable to connect to the HireHub database. "
                            + "Check the database host, port, database name, "
                            + "credentials, and SSL configuration.",
                    e
            );
        }
    }

    private static void loadDriver() {
        if (!driverLoaded) {
            synchronized (LOCK) {
                if (!driverLoaded) {
                    try {
                        Class.forName(DRIVER_CLASS_NAME);
                        driverLoaded = true;

                    } catch (ClassNotFoundException e) {
                        throw new DatabaseException(
                                "MySQL JDBC Driver not found: "
                                        + DRIVER_CLASS_NAME,
                                e
                        );
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

            ClassLoader cl =
                    Thread.currentThread().getContextClassLoader();

            InputStream input = cl != null
                    ? cl.getResourceAsStream(PROPERTIES_FILE)
                    : null;

            if (input == null) {
                input = DBConnection.class.getClassLoader()
                        .getResourceAsStream(PROPERTIES_FILE);
            }

            if (input == null) {
                throw new DatabaseException(
                        "Missing db.properties and database environment "
                                + "variables. Configure DB_HOST, DB_PORT, "
                                + "DB_NAME, DB_USER, and DB_PASSWORD "
                                + "on the hosting platform."
                );
            }

            try (InputStream in = input) {
                Properties loaded = new Properties();
                loaded.load(in);

                properties = loaded;
                return properties;

            } catch (IOException e) {
                throw new DatabaseException(
                        "Failed to read db.properties: " + e.getMessage(),
                        e
                );
            }
        }
    }

    private static String required(Properties config, String key) {
        String value = config.getProperty(key);

        if (value == null || value.isBlank()) {
            throw new DatabaseException(
                    "db.properties is missing required key: " + key
            );
        }

        return value.trim();
    }
}
