package com.company.project.utilities;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import com.company.project.utilities.LoggerUtil;

public class DBUtils {

    private static Connection connection;
    private static Statement statement;
    private static ResultSet resultSet;

    private DBUtils() {
        // Utility class - prevent instantiation
    }

    public static void createConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                LoggerUtil.info("Starting database connection...");
                String url = ConfigurationReader.getProperty("db_url");
                String user = ConfigurationReader.getProperty("db_username");
                String password = ConfigurationReader.getProperty("db_password");

                // Optional: ensure driver is loaded (JDBC 4+ loads automatically)
                try {
                    Class.forName("org.postgresql.Driver");
                } catch (ClassNotFoundException ignored) {
                }

                connection = DriverManager.getConnection(url, user, password);
                LoggerUtil.info("Successfully connected to database: {}", url);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to create DB connection", e);
        }
    }

    public static List<Map<String, Object>> getQueryResultList(String query) {
        List<Map<String, Object>> rows = new ArrayList<>();
        try {
            createConnection();
            LoggerUtil.info("Executing SQL Query: {}", query);
            statement = connection.createStatement();
            resultSet = statement.executeQuery(query);

            ResultSetMetaData metaData = resultSet.getMetaData();
            int columnCount = metaData.getColumnCount();

            while (resultSet.next()) {
                Map<String, Object> row = new LinkedHashMap<>();
                for (int i = 1; i <= columnCount; i++) {
                    String columnName = metaData.getColumnLabel(i);
                    Object value = resultSet.getObject(i);
                    row.put(columnName, value);
                }
                rows.add(row);
            }

            return rows;
        } catch (SQLException e) {
            throw new RuntimeException("Error executing query: " + query, e);
        }
    }

    public static void destroy() {
        LoggerUtil.info("Closing database resources...");
        
        // Close ResultSet
        if (resultSet != null) {
            try {
                resultSet.close();
            } catch (SQLException e) {
                // log or rethrow as runtime
                throw new RuntimeException("Failed to close ResultSet", e);
            } finally {
                resultSet = null;
            }
        }

        // Close Statement
        if (statement != null) {
            try {
                statement.close();
            } catch (SQLException e) {
                throw new RuntimeException("Failed to close Statement", e);
            } finally {
                statement = null;
            }
        }

        // Close Connection
        if (connection != null) {
            try {
                connection.close();
                LoggerUtil.info("Database connection closed successfully");
            } catch (SQLException e) {
                throw new RuntimeException("Failed to close Connection", e);
            } finally {
                connection = null;
            }
        }
    }

    public static String getString(List<Map<String, Object>> rows, String columnName) {
        if (rows == null || rows.isEmpty()) {
            return null;
        }
        Map<String, Object> firstRow = rows.get(0);

        if (firstRow.containsKey(columnName.toLowerCase())) {
            Object val = firstRow.get(columnName.toLowerCase());
            return val != null ? String.valueOf(val) : null;
        } else if (firstRow.containsKey(columnName.toUpperCase())) {
            Object val = firstRow.get(columnName.toUpperCase());
            return val != null ? String.valueOf(val) : null;
        }
        return null;
    }


    public static Long getLong(List<Map<String, Object>> rows, String columnName) {
        if (rows == null || rows.isEmpty()) {
            return null;
        }
        Map<String, Object> firstRow = rows.get(0);
        String key = columnName.toLowerCase();

        if (!firstRow.containsKey(key)) {
            key = columnName.toUpperCase();
        }

        Object val = firstRow.get(key);
        if (val == null) {
            return null;
        }

        if (val instanceof Number) {
            return ((Number) val).longValue();
        }
        return Long.parseLong(val.toString());
    }
}
