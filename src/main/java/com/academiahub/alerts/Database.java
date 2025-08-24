package com.academiahub.alerts;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.io.File;

public class Database {
    private static final String URL = "jdbc:sqlite:academia.db"; // file in project folder
    private static Connection connection = null;

    public static Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            try {
                // Load SQLite driver
                Class.forName("org.sqlite.JDBC");
                connection = DriverManager.getConnection(URL);
                System.out.println("Connected to SQLite database!");

                // Print full path of DB file
                File dbFile = new File("academia.db");
                System.out.println("SQLite DB will be here: " + dbFile.getAbsolutePath());

            } catch (ClassNotFoundException e) {
                throw new SQLException("SQLite JDBC driver not found!", e);
            }
        }
        return connection;
    }
}
