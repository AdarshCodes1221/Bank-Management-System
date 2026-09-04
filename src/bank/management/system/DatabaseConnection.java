package bank.management.system;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class DatabaseConnection {
    public PreparedStatement statement;
    Connection connection;

    public DatabaseConnection() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");

            String host = System.getenv("DB_HOST");
            String port = System.getenv("DB_PORT");
            String database = System.getenv("DB_NAME");
            String username = System.getenv("DB_USER");
            String password = System.getenv("DB_PASSWORD");

            if (host == null || port == null || database == null ||
                    username == null || password == null) {
                throw new IllegalStateException("Database environment variables are not configured.");
            }

            String url = "jdbc:mysql://" + host + ":" + port + "/" + database;

            connection = DriverManager.getConnection(url, username, password);

            System.out.println("Database Connected Successfully");

        } catch (ClassNotFoundException | SQLException | IllegalStateException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        new DatabaseConnection();
    }
}