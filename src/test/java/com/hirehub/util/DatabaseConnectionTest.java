package com.hirehub.util;

import com.hirehub.exception.DatabaseException;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

class DatabaseConnectionTest {

    @Test
    void getConnection_establishesOpenConnectionThatCanBeClosed() {
        Connection connection = null;
        try {
            connection = DBConnection.getConnection();
            assertNotNull(connection, "JDBC connection must not be null.");
            assertFalse(connection.isClosed(), "A newly opened connection must still be open.");

            connection.close();
            assertTrue(connection.isClosed(), "Connection must close without error.");
        } catch (DatabaseException e) {
            fail(missingDatabaseMessage(e));
        } catch (SQLException e) {
            fail("Connected, but checking or closing the connection failed: " + e.getMessage());
        } finally {
            closeQuietly(connection);
        }
    }

    private static String missingDatabaseMessage(DatabaseException e) {
        return "HireHub JDBC test could not reach MySQL. "
                + "Start MySQL, run docs/schema.sql, copy db.properties.example to "
                + "src/main/resources/db.properties, and set db.password. "
                + "Details: " + e.getMessage();
    }

    private static void closeQuietly(Connection connection) {
        if (connection == null) {
            return;
        }
        try {
            if (!connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException ignored) {
            // Best-effort cleanup after assertions.
        }
    }
}
